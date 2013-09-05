package net.staric.kronometer.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import net.staric.kronometer.activities.FinishActivity;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;

import java.util.ArrayList;
import java.util.List;

public class KronometerService extends Service {
    public static final String DATA_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";
    public static final String STATUS_CHANGED_ACTION = "net.staric.kronometer.data_changed_broadcast";

    private Intent notifyDataChangedIntent;

    private final IBinder binder = new LocalBinder();

    private ArrayList<Contestant> contestants = new ArrayList<Contestant>();
    private ArrayList<Event> events = new ArrayList<Event>();

    private String bluetoothStatus = "";
    private boolean started = false;

    private void setBluetoothStatus(String status) {
        this.bluetoothStatus = status;
        updateNotification();
    }

    private Thread bluetoothSensorThread;

    int foregroundNotificationId = 47;

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

    public List<Contestant> getContestants() {
        return contestants;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(statusReceiver);

        if (bluetoothSensorThread != null)
            bluetoothSensorThread.interrupt();
    }

}
