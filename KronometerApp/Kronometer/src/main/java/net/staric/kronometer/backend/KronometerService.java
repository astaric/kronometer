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
import android.util.SparseArray;

import net.staric.kronometer.KronometerContract;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class KronometerService extends Service {
    public static final String DATA_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";
    public static final String STATUS_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";

    private Intent notifyDataChangedIntent;

    private final IBinder binder = new LocalBinder();

    private Thread bluetoothSensorThread;
    private String bluetoothStatus = "";

    protected void setBluetoothStatus(String status) {
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

    int foregroundNotificationId = 47;

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

    @Override
    public void onCreate() {
        super.onCreate();

        notifyDataChangedIntent = new Intent(DATA_CHANGED_ACTION);

        startForeground(foregroundNotificationId, createNotification());

        registerReceiver(statusReceiver, new IntentFilter(KronometerService.STATUS_CHANGED_ACTION));

        bluetoothSensorThread = new BluetoothSensorThread("20:13:08:01:04:98", this);
        bluetoothSensorThread.start();
        contestantSyncThread = new ContestantSynchronizationThread("https://kronometer.herokuapp.com/", this);
        contestantSyncThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (!this.started) {
            startService(new Intent(this, KronometerService.class));
        }
        return binder;
    }

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("bluetoothStatus")) {
                setBluetoothStatus(intent.getStringExtra("bluetoothStatus"));
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        started = true;
        return START_STICKY;
    }

    protected void notifyDataChanged() {
        sendBroadcast(notifyDataChangedIntent);
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

    private ArrayList<Event> events = new ArrayList<Event>();

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KronometerContract.SensorEvent.TIMESTAMP, event.getTime().getTime());
        contentResolver.insert(KronometerContract.SensorEvent.CONTENT_URI, contentValues);

        events.add(event);
        notifyDataChanged();
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
    }
}

class BluetoothSensorThread extends Thread {

    private final UUID BLUETOOTH_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String deviceAddress;
    private final KronometerService kronometerService;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket btSocket;
    private InputStream inStream;

    public BluetoothSensorThread(String deviceAddress, KronometerService kronometerService) {
        this.deviceAddress = deviceAddress;
        this.kronometerService = kronometerService;
    }

    @Override
    public void run() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        while (!isInterrupted()) {
            kronometerService.setBluetoothStatus("Looking for sensor");

            if (bluetoothAdapter == null) {
                kronometerService.setBluetoothStatus("This device does not support bluetooth");
                return;
            } else if (!bluetoothAdapter.isEnabled()) {
                kronometerService.setBluetoothStatus("Bluetooth is not enabled");
                //TODO: Ask for bluetooth
            } else if (!openSocket()) {
                kronometerService.setBluetoothStatus("No sensor found");
            } else if (!openStream()) {
                kronometerService.setBluetoothStatus("Error connecting to sensor");
            } else {
                kronometerService.setBluetoothStatus("Sensor connected");
                listenForData();
                kronometerService.setBluetoothStatus("Sensor disconnected");
            }

            try {
                sleep(10000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private boolean openSocket() {
        BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        btSocket = null;
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SERIAL);
            btSocket.connect();
            return true;
        } catch (IOException e) {
            try {
                if (btSocket != null)
                    btSocket.close();
            } catch (IOException e1) {
            }
            return false;
        }
    }

    private boolean openStream() {
        try {
            inStream = btSocket.getInputStream();
            return true;
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e1) {
            }
            return false;
        }
    }

    private void listenForData() {
        try {
            while (!this.isInterrupted()) {
                readData();
            }
        } catch (IOException ex) {
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
            }

            try {
                btSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void readData() throws IOException {
        byte[] data = new byte[1];
        inStream.read(data);
        if (data[0] == 'E') {
            kronometerService.addEvent(new Event(new Date()));
        }
    }
}

