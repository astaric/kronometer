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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import net.staric.kronometer.ContestantAdapter;
import net.staric.kronometer.EventAdapter;
import net.staric.kronometer.R;
import net.staric.kronometer.backend.KronometerService;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;
import net.staric.kronometer.utils.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private Spinner contestantsOnFinishSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_finish);


        contestantsListView = (ListView) findViewById(R.id.contestants);
        contestantsOnFinishSpinner = (Spinner)findViewById(R.id.contestantsOnFinish);
        sensorEventsListView = (ListView) findViewById(R.id.sensorEvents);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(contestantsListView, getCallbacks());
        contestantsListView.setOnTouchListener(touchListener);
        contestantsListView.setOnScrollListener(touchListener.makeScrollListener());

        sensorEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                toggleSelected(sensorEventsAdapter.getItem(i), i);
                sensorEventsAdapter.notifyDataSetChanged();
            }
        });

        kronometerServiceIntent = new Intent(this, KronometerService.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                stopService(kronometerServiceIntent);
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                    contestantsOnFinishAdapter.insert(contestant, contestantsOnFinishAdapter.getCount()-1);
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
            broadcastReceiver.onReceive(null, null);
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
        contestantsAdapter = new ContestantAdapter(
                this,
                R.layout.listitem_contestant,
                kronometerService.getContestants());
        this.contestantsListView.setAdapter(contestantsAdapter);

        contestantsOnFinishAdapter = new ContestantAdapter(
                this,
                R.layout.listitem_contestant,
                new ArrayList<Contestant>(contestantsAdapter.getCount()));
        contestantsOnFinishAdapter.add(new Contestant());
        contestantsOnFinishSpinner.setAdapter(contestantsOnFinishAdapter);

        sensorEventsAdapter = new EventAdapter(
                this,
                R.layout.listitem_event,
                new ArrayList<Event>(kronometerService.getEvents()));
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
            }
        }
    };

    private void updateUI(Intent intent) {
        List<Event> serviceEvents = kronometerService.getEvents();
        for (int i=sensorEventsAdapter.getCount(); i<serviceEvents.size(); i++) {
            sensorEventsAdapter.add(serviceEvents.get(i));
        }
        sensorEventsAdapter.notifyDataSetChanged();
        contestantsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(kronometerServiceIntent, connection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KronometerService.DATA_CHANGED_ACTION));
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

    Event selectedEvent = null;
    int selectedEventIdx = -1;
    private void toggleSelected(Event event, int idx) {
        if (event == null)
            return;
        if (selectedEvent != null)
            selectedEvent.setSelected(false);
        if (event != selectedEvent) {
            event.setSelected(true);
            selectedEvent = event;
            selectedEventIdx = idx;
        } else {
            selectedEvent = null;
        }
    }


    public void addStopTime(View view) {
        kronometerService.setEndTime(
                (Contestant)contestantsOnFinishSpinner.getSelectedItem(),
                selectedEvent);
        toggleSelected(selectedEvent, -1);
        sensorEventsAdapter.notifyDataSetChanged();

        if (selectedEventIdx > 0)
            sensorEventsListView.setSelection(selectedEventIdx);
        if (contestantsOnFinishAdapter.getCount() > contestantsOnFinishSpinner.getSelectedItemPosition() + 1)
            contestantsOnFinishSpinner.setSelection(contestantsOnFinishSpinner.getSelectedItemPosition() + 1);
    }
}
