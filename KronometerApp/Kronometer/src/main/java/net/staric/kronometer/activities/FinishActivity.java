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
    private int REQUEST_ENABLE_BT = 42;
    private ContestantAdapter contestantsAdapter;
    private ContestantAdapter contestantsOnFinishAdapter;
    private ArrayAdapter<Event> sensorEventsAdapter;
    private ListView sensorEvents;
    private ArrayList<Event> events;
    private KronometerService kronometerService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_finish);

        ListView contestants = (ListView) findViewById(R.id.contestants);
        contestantsAdapter = new ContestantAdapter(this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(Arrays.asList(new Contestant[]{
                        new Contestant(1, "Janez", "Novak"),
                        new Contestant(2, "France", "Prešeren"),
                        new Contestant(3, "France2", "Prešeren"),
                        new Contestant(4, "France3", "Prešeren"),
                        new Contestant(5, "France4", "Prešeren"),
                        new Contestant(6, "France5", "Prešeren"),
                        new Contestant(7, "France6", "Prešeren"),
                        new Contestant(8, "France7", "Prešeren"),
                })));
        contestants.setAdapter(contestantsAdapter);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        contestants,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
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
                        });
        contestants.setOnTouchListener(touchListener);
        contestants.setOnScrollListener(touchListener.makeScrollListener());

        Spinner contestantsOnFinish = (Spinner)findViewById(R.id.contestantsOnFinish);
        contestantsOnFinishAdapter = new ContestantAdapter(this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(contestantsAdapter.getCount()));
        contestantsOnFinish.setAdapter(contestantsOnFinishAdapter);

        sensorEvents = (ListView) findViewById(R.id.sensorEvents);
        sensorEvents.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        events = new ArrayList<Event>();
        sensorEventsAdapter = new ArrayAdapter<Event>(
                this,
                android.R.layout.simple_list_item_1,
                events);
        sensorEvents.setAdapter(sensorEventsAdapter);

        Intent intent = new Intent(this, KronometerService.class);
        startService(intent);
        bindService(new Intent(this, KronometerService.class), connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            KronometerService.LocalBinder binder = (KronometerService.LocalBinder) service;
            kronometerService = binder.getService();
            events.addAll(kronometerService.getEvents());
            sensorEventsAdapter.notifyDataSetChanged();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void generateEvent(View view) {
        sensorEventsAdapter.add(new Event(new Date()));
        sensorEventsAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent intent) {
        events.clear();
        events.addAll(kronometerService.getEvents());
        sensorEventsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(KronometerService.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }
}
