package net.staric.kronometer.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import net.staric.kronometer.ContestantAdapter;
import net.staric.kronometer.R;
import net.staric.kronometer.backend.KronometerService;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;
import net.staric.kronometer.utils.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class FinishActivity extends Activity {
    private KronometerService kronometerService;
    private void setKronometerService(KronometerService service) {
        if (service == null)
            bound = false;
        kronometerService = service;
    }
    private Intent kronometerServiceIntent;
    private boolean bound = false;

    private ArrayList<Contestant> contestants;
    private ArrayList<Contestant> contestantsOnFinish;
    private ArrayList<Event> events;

    private ListView contestantsListView;
    private ListView sensorEventsListView;

    private ContestantAdapter contestantsAdapter;
    private ContestantAdapter contestantsOnFinishAdapter;
    private ArrayAdapter<Event> sensorEventsAdapter;
    private Spinner contestantsOnFinishListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_finish);


        contestantsListView = (ListView) findViewById(R.id.contestants);
        contestantsOnFinishListView = (Spinner)findViewById(R.id.contestantsOnFinish);
        sensorEventsListView = (ListView) findViewById(R.id.sensorEvents);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(contestantsListView, getCallbacks());
        contestantsListView.setOnTouchListener(touchListener);
        contestantsListView.setOnScrollListener(touchListener.makeScrollListener());

        sensorEventsListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        kronometerServiceIntent = new Intent(this, KronometerService.class);
        startService(kronometerServiceIntent);
    }

    private SwipeDismissListViewTouchListener.DismissCallbacks getCallbacks() {
        return new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    Contestant contestant = contestantsAdapter.getItem(position);
                    contestantsAdapter.remove(contestant);
                    contestantsOnFinishAdapter.add(contestant);

                }
                contestantsAdapter.notifyDataSetChanged();
                contestantsOnFinishAdapter.notifyDataSetChanged();
            }
        };
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            KronometerService.LocalBinder binder = (KronometerService.LocalBinder) service;
            setKronometerService(binder.getService());
            setUpAdapters();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            setKronometerService(null);
        }
    };

    private void setUpAdapters() {
        if (kronometerService == null)
            return;
        contestants = new ArrayList<Contestant>();
        Contestant janez = new Contestant(1, "Janez", "Novak");
        janez.setStartTime(new Date());
        contestantsAdapter = new ContestantAdapter(this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(Arrays.asList(new Contestant[]{
                        janez,
                        new Contestant(2, "France", "Prešeren"),
                        new Contestant(3, "France2", "Prešeren"),
                })));
        this.contestantsListView.setAdapter(contestantsAdapter);

        contestantsOnFinishAdapter = new ContestantAdapter(this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(contestantsAdapter.getCount()));
        contestantsOnFinishListView.setAdapter(contestantsOnFinishAdapter);

        sensorEventsAdapter = new ArrayAdapter<Event>(
                this,
                android.R.layout.simple_list_item_1,
                kronometerService.getEvents());
        sensorEventsListView.setAdapter(sensorEventsAdapter);

        bound = true;
    }

    public void generateEvent(View view) {
        if (kronometerService != null)
            kronometerService.addEvent(new Event(new Date()));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bound) {
                updateUI(intent);
            };
        }
    };

    private void updateUI(Intent intent) {
        sensorEventsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(kronometerServiceIntent, connection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KronometerService.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        if (kronometerService != null) {
            unbindService(connection);
            setKronometerService(null);
        }
    }
}
