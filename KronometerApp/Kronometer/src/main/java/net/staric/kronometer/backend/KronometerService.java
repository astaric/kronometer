package net.staric.kronometer.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import net.staric.kronometer.activities.FinishActivity;
import net.staric.kronometer.models.Event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class KronometerService extends Service {
    public static final String BROADCAST_ACTION = "net.staric.kronometer.data_changed_broadcast";
    Intent intent;

    private final IBinder binder = new LocalBinder();
    private ArrayList<Event> events = new ArrayList<Event>();

    private String bluetoothStatus = "";
    private void setBluetoothStatus(String status) {
        this.bluetoothStatus = status;
        updateNotification();
    }

    private Thread bluetoothListenerThread;

    int foregroundNotificationId = 47;

    public class LocalBinder extends Binder {
        public KronometerService getService() {
            return KronometerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(BROADCAST_ACTION);

        moveToForeground();

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            setBluetoothStatus("This device does not support bluetooth");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            setBluetoothStatus("Bluetooth is not enabled");
            return;
        }

        bluetoothListenerThread = new Thread() {
            @Override
            public void run() {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice("20:13:08:01:04:98");

                BluetoothSocket btSocket = null;
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    btSocket.connect();
                } catch (IOException e) {
                    try {
                        if (btSocket != null)
                            btSocket.close();
                    } catch (IOException e1) {}
                    setBluetoothStatus("Sensor is not available");
                    return;
                }

                InputStream inStream;
                try {
                    inStream = btSocket.getInputStream();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e1) {}
                    setBluetoothStatus("Error connecting to sensor");
                    return;
                }

                setBluetoothStatus("Sensor connected");

                while (!this.isInterrupted()) {
                    boolean dataChanged = false;
                    try
                    {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                if (packetBytes[i] == 'E') {
                                    events.add(new Event(new Date()));
                                    dataChanged = true;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        this.interrupt();
                    }
                    if (dataChanged) {
                        notifyDataChanged();
                    }
                }

                try {
                    inStream.close();
                } catch (IOException e) {}

                try {
                    btSocket.close();
                } catch (IOException e) {}

                setBluetoothStatus("Sensor disconnected");
            }
        };

        bluetoothListenerThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    protected void notifyDataChanged() {
        sendBroadcast(intent);
    }

    private void moveToForeground() {
        startForeground(foregroundNotificationId, createNotification());
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
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentTitle("Bluetooth sensor")
                .setContentText(bluetoothStatus)
                .setContentIntent(pendingIntent);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Kronometer");
        inboxStyle.addLine(bluetoothStatus);
        mBuilder.setStyle(inboxStyle);
        return mBuilder.build();
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        events.add(event);
        notifyDataChanged();
    }
}
