package net.staric.kronometer.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import net.staric.kronometer.R;
import net.staric.kronometer.activities.FinishActivity;
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

import static net.staric.kronometer.KronometerContract.*;

public class KronometerService extends Service {
    public static final String TAG = "KronometerService";
    int foregroundNotificationId = 47;

    private final IBinder binder = new LocalBinder();

    ArrayBlockingQueue<Long> eventQueue;
    private Thread eventProcessingThread;
    private Thread bluetoothSensorThread;
    private String bluetoothStatus = "";

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(foregroundNotificationId, createNotification());
        eventQueue = new ArrayBlockingQueue<Long>(100);

        // Android sensor
        //bluetoothSensorThread = new BluetoothSensorThread("20:13:08:01:04:98", this);
        // iBall
        bluetoothSensorThread = new BluetoothSensorThread("14:10:9F:D7:9A:74");
        bluetoothSensorThread.start();

        eventProcessingThread = new EventProcessingThread(eventQueue);
        eventProcessingThread.start();

        // TODO: Get rid of these
        notifyDataChangedIntent = new Intent(DATA_CHANGED_ACTION);

        registerReceiver(statusReceiver, new IntentFilter(KronometerService.STATUS_CHANGED_ACTION));

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

    private void updateNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(foregroundNotificationId, createNotification());
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, FinishActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_biker)
                .setContentTitle("Bluetooth sensor")
                .setContentText(bluetoothStatus)
                .setContentIntent(pendingIntent);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Kronometer");
        inboxStyle.addLine(bluetoothStatus);
        inboxStyle.addLine(syncStatus);
        mBuilder.setStyle(inboxStyle);
        return mBuilder.build();
    }


    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("bluetoothStatus")) {
                setBluetoothStatus(intent.getStringExtra("bluetoothStatus"));
            }
        }
    };

    public static final String DATA_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";
    public static final String STATUS_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";

    private Intent notifyDataChangedIntent;





    protected void setBluetoothStatus(String status) {
        Log.i(TAG, status);
        this.bluetoothStatus = status;
        updateNotification();
    }

    private Thread contestantSyncThread;
    private String syncStatus = "";

    protected void setSyncStatus(String status) {
        this.syncStatus = status;
        updateNotification();
    }

    private boolean started = false;


    public Event duplicateEvent(Event event) {
        Event newEvent = new Event(event.getTime());
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i) == event) {
                events.add(i + 1, newEvent);
                break;
            }
        }
        return newEvent;
    }


    public class LocalBinder extends Binder {
        public KronometerService getService() {
            return KronometerService.this;
        }
    }


    protected void notifyDataChanged() {
        sendBroadcast(notifyDataChangedIntent);
    }


    private ArrayList<Event> events = new ArrayList<Event>();

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Long timestamp) {
        try {
            eventQueue.add(timestamp);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Event queue is full.");
        }
    }

    private ArrayList<Contestant> contestants = new ArrayList<Contestant>();
    private SparseArray<Contestant> contestantMap = new SparseArray<Contestant>();

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

    public void setEndTime(Contestant contestant, Event event) {
        if (contestant == null || contestant.dummy)
            return;
        if (event == null)
            return;

        if (contestant.getEndTime() != null) {
            for (Event event1 : events) {
                if (event1.getContestant() == contestant) {
                    event1.setContestant(null);
                }
            }
        }

        Date endTime = event.getTime();
        Update update = contestant.setEndTime(endTime);
        addUpdate(update);

        event.setContestant(contestant);
    }

    private ArrayList<Category> categories = new ArrayList<Category>();
    private SparseArray<Category> categoryMap = new SparseArray<Category>();

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

    BlockingQueue<Update> pendingUpdates = new LinkedBlockingQueue<Update>();

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(statusReceiver);

        if (bluetoothSensorThread != null)
            bluetoothSensorThread.interrupt();
        if (contestantSyncThread != null)
            contestantSyncThread.interrupt();
        if (eventProcessingThread != null)
            eventProcessingThread.interrupt();
    }


    private class EventProcessingThread extends Thread {
        private BlockingQueue<Long> eventQueue;

        public EventProcessingThread(BlockingQueue<Long> eventQueue) {
            this.eventQueue = eventQueue;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Long timestamp = eventQueue.take();
                    ContentResolver contentResolver = getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(SensorEvent.TIMESTAMP, timestamp);
                    contentResolver.insert(SensorEvent.CONTENT_URI, contentValues);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private class BluetoothSensorThread extends Thread {
        private static final String TAG = "KronometerService";
        private final UUID BLUETOOTH_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private String deviceAddress;
        private InputStream inStream;

        public BluetoothSensorThread(String deviceAddress) {
            this.deviceAddress = deviceAddress;
        }

        @Override
        public void run() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                setBluetoothStatus("This device does not support bluetooth");
                return;
            }

            BluetoothDevice btDevice = null;
            BluetoothSocket btSocket = null;
            inStream = null;
            while (!isInterrupted()) {
                if (!bluetoothAdapter.isEnabled()) {
                    setBluetoothStatus("Bluetooth is disabled");
                }

                setBluetoothStatus("Looking for sensor");
                try {
                    btDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    btSocket = btDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SERIAL);
                    btSocket.connect();
                    inStream = btSocket.getInputStream();
                    setBluetoothStatus("Sensor connected");

                    byte[] data = new byte[1];
                    readLoop:
                    while (!this.isInterrupted()) {
                        Log.d(TAG, "BT Waiting for data");
                        switch(inStream.read()) {
                            case 'E':
                                Log.d(TAG, "BT received event");
                                addEvent(new Date().getTime());
                                break;
                            case -1:
                                break readLoop;
                            default:
                                Log.d(TAG, "BT Received unknown command");
                        }
                    }
                    setBluetoothStatus("Sensor disconnected");
                } catch (IOException e) {
                    Log.i(TAG, e.getMessage());
                    setBluetoothStatus("Error connecting to the sensor");
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

        @Override
        public void interrupt() {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.interrupt();
        }
    }
}



