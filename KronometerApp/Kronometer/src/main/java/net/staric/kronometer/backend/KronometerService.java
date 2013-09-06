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
import android.util.SparseArray;

import net.staric.kronometer.activities.FinishActivity;
import net.staric.kronometer.models.Category;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
                events.add(i+1, newEvent);
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
                .setSmallIcon(android.R.drawable.btn_star)
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

    LinkedBlockingQueue<Update> pendingUpdates = new LinkedBlockingQueue<Update>();
    private void addUpdate(Update update) {
        try {
            pendingUpdates.put(update);
        } catch (InterruptedException e) {}
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
