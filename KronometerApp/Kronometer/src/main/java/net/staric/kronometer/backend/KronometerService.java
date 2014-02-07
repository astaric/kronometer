package net.staric.kronometer.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import net.staric.kronometer.FinishActivity;
import net.staric.kronometer.KronometerContract;
import net.staric.kronometer.MainActivity;
import net.staric.kronometer.R;
import net.staric.kronometer.models.Category;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.staric.kronometer.KronometerContract.SENSOR_EVENT_ACTION;
import static net.staric.kronometer.KronometerContract.SENSOR_STATUS;
import static net.staric.kronometer.KronometerContract.SENSOR_STATUS_CHANGED_ACTION;
import static net.staric.kronometer.KronometerContract.SensorEvent;

public class KronometerService extends Service {
    public static final String TAG = "KronometerService";
    public static final String DATA_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";
    public static final String STATUS_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";
    private final IBinder binder = new LocalBinder();
    int foregroundNotificationId = 47;
    BlockingQueue<Update> pendingUpdates = new LinkedBlockingQueue<Update>();
    private boolean started = false;
    private Thread bluetoothSensorThread;
    private String sensorStatus = "";
    private BroadcastReceiver statusUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(KronometerContract.SENSOR_STATUS)) {
                setSensorStatus(intent.getStringExtra(KronometerContract.SENSOR_STATUS));
            }
        }
    };
    private BroadcastReceiver sensorEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(SensorEvent.TIMESTAMP)) {
                Event.create(KronometerService.this, intent.getLongExtra(SensorEvent.TIMESTAMP, 0));
            }
        }
    };
    private Intent notifyDataChangedIntent;
    private Thread contestantSyncThread;
    private String syncStatus = "";
    private ArrayList<Event> events = new ArrayList<Event>();
    private ArrayList<Contestant> contestants = new ArrayList<Contestant>();

    // Get rid of this
    private SparseArray<Contestant> contestantMap = new SparseArray<Contestant>();
    private ArrayList<Category> categories = new ArrayList<Category>();
    private SparseArray<Category> categoryMap = new SparseArray<Category>();

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(foregroundNotificationId, createNotification());

        // Android sensor
        //bluetoothSensorThread = new BluetoothSensorThread(this, "20:13:08:01:04:98", this);
        // iBall
        bluetoothSensorThread = new BluetoothSensorThread(this, "14:10:9F:D7:9A:74");
        bluetoothSensorThread.setPriority(Thread.MAX_PRIORITY);
        bluetoothSensorThread.start();

        registerReceiver(statusUpdateReceiver, new IntentFilter(KronometerContract.SENSOR_STATUS_CHANGED_ACTION));
        registerReceiver(sensorEventReceiver, new IntentFilter(KronometerContract.SENSOR_EVENT_ACTION));


        // TODO: Get rid of these
        notifyDataChangedIntent = new Intent(DATA_CHANGED_ACTION);

        contestantSyncThread = new ContestantSynchronizationThread("https://kronometer.herokuapp.com/", this);
        contestantSyncThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        started = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (!this.started) {
            startService(new Intent(this, KronometerService.class));
        }
        return binder;
    }

    protected void setSensorStatus(String status) {
        this.sensorStatus = status;
        updateNotification();
    }

    private void updateNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(foregroundNotificationId, createNotification());
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_biker)
                .setContentTitle("Bluetooth sensor")
                .setContentText(sensorStatus)
                .setContentIntent(pendingIntent);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Kronometer");
        inboxStyle.addLine(sensorStatus);
        inboxStyle.addLine(syncStatus);
        mBuilder.setStyle(inboxStyle);
        return mBuilder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(statusUpdateReceiver);

        if (bluetoothSensorThread != null)
            bluetoothSensorThread.interrupt();
        if (contestantSyncThread != null)
            contestantSyncThread.interrupt();
    }

    protected void setSyncStatus(String status) {
        this.syncStatus = status;
        updateNotification();
    }

    protected void notifyDataChanged() {
        sendBroadcast(notifyDataChangedIntent);
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Contestant> getContestants() {
        return contestants;
    }

    protected void updateContestants(ArrayList<Contestant> newContestants) {
        boolean modified = false;
        Collections.sort(newContestants);
        for (Contestant contestant : newContestants) {
            Contestant existingContestant = contestantMap.get(contestant.id);
            if (existingContestant == null) {
                contestants.add(contestant);
                contestantMap.put(contestant.id, contestant);
                modified = true;
            } else {
                if (existingContestant.update(contestant)) {
                    modified = true;
                }
            }
        }
        if (modified) {
            notifyDataChanged();
        }
    }

    public void setEndTime(Contestant contestant, Long timestamp) {
        if (contestant == null || contestant.dummy)
            return;
        if (timestamp == null)
            return;

        Date endTime = new Date(timestamp);
        Update update = contestant.setEndTime(endTime);
        addUpdate(update);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void updateCategories(ArrayList<Category> newCategories) {
        boolean modified = false;
        Collections.sort(newCategories);
        for (Category category : newCategories) {
            Category existingCategory = categoryMap.get(category.id);
            if (existingCategory == null) {
                categories.add(category);
                categoryMap.put(category.id, category);
                modified = true;
            } else {
                if (existingCategory.update(category)) {
                    modified = true;
                }
            }
        }
        if (modified) {
            notifyDataChanged();
        }
    }

    public void addUpdate(Update update) {
        try {
            pendingUpdates.put(update);
        } catch (InterruptedException e) {
        }
        setSyncStatus(String.format("%d updates pending", pendingUpdates.size()));
    }

    public BlockingQueue<Update> getUpdates() {
        return pendingUpdates;
    }

    public class LocalBinder extends Binder {
        public KronometerService getService() {
            return KronometerService.this;
        }
    }
}

class BluetoothSensorThread extends Thread {
    private static final String TAG = "KronometerService.BluetoothSensorThread";
    private final UUID BLUETOOTH_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ArrayBlockingQueue<Long> eventQueue;
    private Context context;
    private String deviceAddress;
    private InputStream inStream;

    public BluetoothSensorThread(Context context, String deviceAddress) {
        this.context = context;
        this.deviceAddress = deviceAddress;

        this.eventQueue = new ArrayBlockingQueue<Long>(100);
    }

    @Override
    public void run() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            setSensorStatus("This device does not support bluetooth");
            return;
        }

        BluetoothDevice btDevice = null;
        BluetoothSocket btSocket = null;
        inStream = null;
        while (!isInterrupted()) {
            if (!bluetoothAdapter.isEnabled()) {
                setSensorStatus("Bluetooth is disabled");
                Intent requestBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                requestBluetooth.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(requestBluetooth);
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            try {
                setSensorStatus("Looking for sensor");
                btDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                btSocket = btDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SERIAL);
                btSocket.connect();
                inStream = btSocket.getInputStream();
                setSensorStatus("Sensor connected");

                byte[] data = new byte[1];
                readLoop:
                while (!this.isInterrupted()) {
                    switch (inStream.read()) {
                        case 'E':
                            processEvent(new Date().getTime());
                            break;
                        case -1:
                            break readLoop;
                        default:
                            Log.d(TAG, "Received unknown command");
                    }
                }
                setSensorStatus("Sensor disconnected");
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
                setSensorStatus("Error connecting to the sensor");
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred while closing BT stream.");
                    }
                }

                if (btSocket != null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred while closing BT socket.");
                    }
                }
            }

            try {
                sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void setSensorStatus(String status) {
        Log.i(TAG, status);
        Intent eventNotification = new Intent(SENSOR_STATUS_CHANGED_ACTION);
        eventNotification.putExtra(SENSOR_STATUS, status);
        context.sendBroadcast(eventNotification);
    }

    public void processEvent(Long timestamp) {
        Log.d(TAG, "Received sensor event");
        Intent eventNotification = new Intent(SENSOR_EVENT_ACTION);
        eventNotification.putExtra(SensorEvent.TIMESTAMP, timestamp);
        context.sendBroadcast(eventNotification);
    }

    @Override
    public void interrupt() {
        closeConnectionStream();

        super.interrupt();
    }

    private void closeConnectionStream() {
        try {
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
