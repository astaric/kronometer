package net.staric.kronometer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.staric.kronometer.models.Event;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import static net.staric.kronometer.KronometerContract.SENSOR_EVENT_ACTION;
import static net.staric.kronometer.KronometerContract.SENSOR_STATUS;
import static net.staric.kronometer.KronometerContract.SENSOR_STATUS_CHANGED_ACTION;
import static net.staric.kronometer.KronometerContract.SensorEvent;

public class KronometerService extends Service {
    public static final String TAG = "KronometerService";

    private boolean started = false;
    private int foregroundNotificationId = 47;

    private Thread bluetoothSensorThread;
    private String sensorStatus = "";

    private final IBinder binder = new LocalBinder();

    private BroadcastReceiver statusUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(KronometerContract.SENSOR_STATUS)) {
                sensorStatus = intent.getStringExtra(KronometerContract.SENSOR_STATUS);
                updateNotification();
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

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        Account account = createDummySyncAccount(this);
        String AUTHORITY = "net.staric.kronometer";
        ContentResolver.requestSync(account, AUTHORITY, settingsBundle);
    }

    public static Account createDummySyncAccount(Context context) {
        // An account type, in the form of a domain name
        String ACCOUNT_TYPE = "kronometer.staric.net";
        // The account name
        String ACCOUNT = "dummyaccount";

        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            return newAccount;
        } else {
            Log.e(TAG, "Account already exists");
            return null;
        }
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
        mBuilder.setStyle(inboxStyle);
        return mBuilder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(statusUpdateReceiver);
        unregisterReceiver(sensorEventReceiver);

        if (bluetoothSensorThread != null)
            bluetoothSensorThread.interrupt();
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
