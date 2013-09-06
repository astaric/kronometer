package net.staric.kronometer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FinishActivity extends Activity {
    private KronometerService kronometerService;

    private void setKronometerService(KronometerService service) {
        if (service == null)
            bound = false;
        kronometerService = service;
    }

    private Intent kronometerServiceIntent;
    private boolean bound = false;

    private static ArrayList<Contestant> contestants = new ArrayList<Contestant>();
    private static ArrayList<Contestant> contestantsOnFinish =
            new ArrayList<Contestant>(Arrays.asList(new Contestant[]{new Contestant()}));
    private static ArrayList<Event> events = new ArrayList<Event>();

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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_finish);


        contestantsListView = (ListView) findViewById(R.id.contestants);
        contestantsOnFinishSpinner = (Spinner) findViewById(R.id.contestantsOnFinish);
        sensorEventsListView = (ListView) findViewById(R.id.sensorEvents);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(contestantsListView, getCallbacks());
        contestantsListView.setOnTouchListener(touchListener);
        contestantsListView.setOnScrollListener(touchListener.makeScrollListener());

        sensorEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                toggleSelected(i);
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
                    contestantsOnFinishAdapter.insert(contestant, contestantsOnFinishAdapter.getCount() - 1);
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
        contestantsAdapter = new ContestantAdapter(
                this,
                R.layout.listitem_contestant,
                contestants);
        this.contestantsListView.setAdapter(contestantsAdapter);

        contestantsOnFinishAdapter = new ContestantAdapter(
                this,
                R.layout.listitem_contestant,
                contestantsOnFinish);
        contestantsOnFinishSpinner.setAdapter(contestantsOnFinishAdapter);

        sensorEventsAdapter = new EventAdapter(
                this,
                R.layout.listitem_event,
                events);
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

    private static HashSet<Integer> knownContestants = new HashSet<Integer>();
    private static int lastCopiedEventIdx = 0;
    private void updateUI(Intent intent) {
        for (Contestant contestant : kronometerService.getContestants()) {
            if (!knownContestants.contains(contestant.id)) {
                contestantsAdapter.add(contestant);
                knownContestants.add(contestant.id);
            }
        }

        List<Event> serviceEvents = kronometerService.getEvents();
        int serviceEventCount = serviceEvents.size();
        if (serviceEventCount > lastCopiedEventIdx) {
            for (int i=lastCopiedEventIdx; i<serviceEventCount; i++) {
                sensorEventsAdapter.add(serviceEvents.get(i));
            }
            lastCopiedEventIdx = serviceEventCount;
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

    int selectedEventIdx = -1;

    private void toggleSelected(int idx) {
        if (idx < -1 || idx >= events.size())
            return;
        if (selectedEventIdx != -1)
            events.get(selectedEventIdx).setSelected(false);
        if (idx != selectedEventIdx) {
            selectedEventIdx = idx;
            events.get(selectedEventIdx).setSelected(true);
        } else {
            selectedEventIdx = -1;
        }
    }


    public void addStopTime(View view) {
        try {
            if (selectedEventIdx != -1) {
                Event selectedEvent = events.get(selectedEventIdx);
                Contestant selectedContestant = (Contestant)contestantsOnFinishSpinner.getSelectedItem();
                if (selectedEvent.getContestant() != null) {
                    askForConfirmationForDuplicatingEvent(selectedContestant, selectedEvent);
                } else if (selectedContestant.getEndTime() != null) {
                    askForConfirmationForChangingEndTime(selectedContestant, selectedEvent);
                } else {
                    setEndTime(selectedContestant, selectedEvent);
                }
            }
        } catch (IllegalArgumentException ex) {
            //TODO display some kind of message
        }
    }

    private void askForConfirmationForDuplicatingEvent(final Contestant contestant, final Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format("Sensor event is already associated with contestant %s\nDo you want to continue?", contestant))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Event newEvent = kronometerService.duplicateEvent(event);
                        setEndTime(contestant, newEvent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        toggleSelected(selectedEventIdx);
                        sensorEventsAdapter.notifyDataSetChanged();
                    }
                })
                .create()
                .show();
    }

    private void askForConfirmationForChangingEndTime(final Contestant contestant, final Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format("Contestant %s already has end time set. Do you want to change it?", contestant))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setEndTime(contestant, event);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        toggleSelected(selectedEventIdx);
                        sensorEventsAdapter.notifyDataSetChanged();
                    }
                })
                .create()
                .show();
    }

    private void setEndTime(Contestant contestant, Event event) {
        kronometerService.setEndTime(contestant, event);
        events.subList(0, selectedEventIdx + 1).clear();
        sensorEventsAdapter.notifyDataSetChanged();
        if (contestantsOnFinishAdapter.getCount() > contestantsOnFinishSpinner.getSelectedItemPosition() + 1)
            contestantsOnFinishSpinner.setSelection(contestantsOnFinishSpinner.getSelectedItemPosition() + 1);
    }
}
