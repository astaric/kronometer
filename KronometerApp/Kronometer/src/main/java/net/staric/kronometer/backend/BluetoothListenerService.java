package net.staric.kronometer.backend;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import net.staric.kronometer.R;
import net.staric.kronometer.activities.FinishActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BluetoothListenerService extends Service {
    private final IBinder binder = new LocalBinder();
    private ArrayList<String> events = new ArrayList<String>();
    private Thread listenerThread;

    public class LocalBinder extends Binder {
        public BluetoothListenerService getService() {
            return BluetoothListenerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            return;
        }

        listenerThread = new Thread() {
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
                    return;
                }

                InputStream inStream;
                try {
                    inStream = btSocket.getInputStream();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e1) {}
                    return;
                }

                BluetoothListenerService.this.moveToForeground();

                while (!this.isInterrupted()) {
                    try
                    {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                if (packetBytes[i] == 'E')
                                    events.add(new Date().toString());
                            }
                        }
                    }
                    catch (IOException ex) {
                        break;
                    }
                }

                try {
                    inStream.close();
                } catch (IOException e) {}

                try {
                    btSocket.close();
                } catch (IOException e) {}

                //BluetoothListenerService.this.stopSelf();
            }
        };

        listenerThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void moveToForeground() {
        Notification notification = new Notification(android.R.drawable.btn_star, getText(R.string.ticker_text),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, FinishActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.notification_message), pendingIntent);
        startForeground(47, notification);
    }

    public List<String> getEvents() {
        return events;
    }
}
