package net.staric.kronometer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import net.staric.kronometer.KronometerContract;
import net.staric.kronometer.R;
import net.staric.kronometer.backend.KronometerService;
import net.staric.kronometer.misc.ContestantAdapter;
import net.staric.kronometer.misc.EventAdapter;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;
import net.staric.kronometer.utils.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static net.staric.kronometer.KronometerContract.SensorEvent;

public class FinishActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    static int selectedContestantId = 0;
    private static List<Contestant> contestants = new ArrayList<Contestant>();
    private static List<Contestant> contestantsOnFinish =
            new ArrayList<Contestant>(Arrays.asList(new Contestant[]{new Contestant()}));
    private static List<Event> events = new ArrayList<Event>();
    private static HashSet<Integer> knownContestants = new HashSet<Integer>();
    private static int lastCopiedEventIdx = 0;
    Event selectedEvent = null;
    private KronometerService kronometerService;
    private Intent kronometerServiceIntent;
    private boolean bound = false;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bound) {
                updateUI(intent);
            }
        }
    };
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            KronometerService.LocalBinder binder = (KronometerService.LocalBinder) service;
            setKronometerService(binder.getService());
            bound = true;
            broadcastReceiver.onReceive(null, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            setKronometerService(null);
        }
    };
    private ListView contestantsListView;
    private ListView sensorEventsListView;
    private ContestantAdapter contestantsAdapter;
    private ContestantAdapter contestantsOnFinishAdapter;
    private EventAdapter sensorEventsAdapter;
    private Spinner contestantsOnFinishSpinner;

    private void setKronometerService(KronometerService service) {
        if (service == null)
            bound = false;
        kronometerService = service;
    }

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
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                sensorEventsAdapter.setSelectedId(id);
                sensorEventsAdapter.notifyDataSetChanged();
            }
        });

        kronometerServiceIntent = new Intent(this, KronometerService.class);
        setUpAdapters();

        findViewById(R.id.contestants).setKeepScreenOn(true);
        getLoaderManager().initLoader(0, null, this);
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
                finish();
                System.exit(0);
                return true;
            case R.id.action_show_all_events:
                lastCopiedEventIdx = 0;
                events.clear();
                if (bound) {
                    updateUI(null);
                }
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

    private void setUpAdapters() {
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

        sensorEventsAdapter = new EventAdapter(this);
        sensorEventsListView.setAdapter(sensorEventsAdapter);
    }

    public void generateEvent(View view) {
        if (kronometerService != null)
            kronometerService.storeEvent(new Date().getTime());
    }

    private void updateUI(Intent intent) {
        for (Contestant contestant : kronometerService.getContestants()) {
            if (!knownContestants.contains(contestant.id)) {
                contestantsAdapter.add(contestant);
                knownContestants.add(contestant.id);
            }
        }

        contestantsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(kronometerServiceIntent, connection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KronometerService.DATA_CHANGED_ACTION));
        selectedContestantId = contestantsOnFinishSpinner.getSelectedItemPosition();
        if (selectedContestantId > contestantsOnFinishSpinner.getCount())
            contestantsOnFinishSpinner.setSelection(selectedContestantId);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        if (kronometerService != null) {
            unbindService(connection);
            setKronometerService(null);
        }
        selectedContestantId = contestantsOnFinishSpinner.getSelectedItemPosition();
    }

    private void toggleSelected(Event event) {
        if (selectedEvent != null)
            selectedEvent.setSelected(false);
        if (event != null)
            event.setSelected(!event.isSelected());
        selectedEvent = event;
    }

    public void addStopTime(View view) {
        Contestant selectedContestant = (Contestant) contestantsOnFinishSpinner.getSelectedItem();
        if (selectedContestant == null) {
            return;
        }
        Long timestamp = getTimestamp();
        if (timestamp == 0) {
            return;
        }
        if (selectedContestant.getEndTime() != null) {
            askForConfirmationForChangingEndTime(selectedContestant, timestamp);
        } else {
            setEndTime(selectedContestant, timestamp);
        }
    }

    private Long getTimestamp() {
        long selectedId = sensorEventsAdapter.getSelectedId();
        if (selectedId == 0) {
            return 0L;
        }

        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(SensorEvent.CONTENT_URI, selectedId),
                new String[]{SensorEvent.TIMESTAMP}, "", new String[]{}, "");
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(SensorEvent.TIMESTAMP));
        }
        return 0L;
    }

    private void askForConfirmationForDuplicatingEvent(final Contestant contestant,
                                                       final Long timestamp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.duplicateEventConfirmation), contestant))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setEndTime(contestant, timestamp);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedEvent != null) {
                            selectedEvent.setSelected(false);
                            selectedEvent = null;
                            sensorEventsAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .create()
                .show();
    }

    private void askForConfirmationForChangingEndTime(final Contestant contestant,
                                                      final Long event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.endTimeChangeConfirmation), contestant))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setEndTime(contestant, event);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedEvent != null) {
                            selectedEvent.setSelected(false);
                            selectedEvent = null;
                            sensorEventsAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .create()
                .show();
    }

    private void setEndTime(Contestant contestant, Long timestamp) {
        kronometerService.setEndTime(contestant, timestamp);
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i) == selectedEvent) {
                events.subList(0, i + 1).clear();
                break;
            }
        }
        sensorEventsAdapter.notifyDataSetChanged();
        if (contestantsOnFinishAdapter.getCount() > contestantsOnFinishSpinner.getSelectedItemPosition() + 1)
            contestantsOnFinishSpinner.setSelection(contestantsOnFinishSpinner.getSelectedItemPosition() + 1);
    }

    // LoaderManager.LoaderCallbacks<Cursor> implementation
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, SensorEvent.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        sensorEventsAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        sensorEventsAdapter.swapCursor(null);
    }
}
