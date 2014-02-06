package net.staric.kronometer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import net.staric.kronometer.backend.KronometerService;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;
import net.staric.kronometer.utils.SwipeDismissListViewTouchListener;

import java.util.Date;
import java.util.HashSet;

import static net.staric.kronometer.KronometerContract.Bikers;
import static net.staric.kronometer.KronometerContract.SensorEvent;

public class FinishActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "Kronometer.FinishActivity";
    static int selectedContestantId = 0;
    private static HashSet<Integer> knownContestants = new HashSet<Integer>();
    Event selectedEvent = null;
    Long displayFromId = null;
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

        contestantsAdapter = new StartContestantAdapter(this, false, false);
        this.contestantsListView.setAdapter(contestantsAdapter);

        contestantsOnFinishAdapter = new FinishContestantAdapter(this, true, true);
        contestantsOnFinishSpinner.setAdapter(contestantsOnFinishAdapter);

        sensorEventsAdapter = new EventAdapter(this);
        sensorEventsListView.setAdapter(sensorEventsAdapter);

        findViewById(R.id.contestants).setKeepScreenOn(true);
        getLoaderManager().initLoader(0, null, this);
        getLoaderManager().initLoader(1, null, this);
        getLoaderManager().initLoader(2, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_all_events:
                displayFromId = null;
                getLoaderManager().restartLoader(0, null, this);
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
                    Cursor cursor = contestantsAdapter.getCursor();
                    if (cursor != null && cursor.moveToPosition(position)) {
                        long id = cursor.getLong(cursor.getColumnIndex(Bikers._ID));
                        Uri uri = ContentUris.withAppendedId(Bikers.CONTENT_URI, id);

                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(Bikers.ON_FINISH, new Date().getTime());

                        getContentResolver().update(uri, contentValues, null, null);
                    }
                }
            }
        };
    }

    public void generateEvent(View view) {
        if (kronometerService != null)
            kronometerService.storeEvent(new Date().getTime());
    }

    private void updateUI(Intent intent) {
        for (Contestant contestant : kronometerService.getContestants()) {
            if (!knownContestants.contains(contestant.id)) {
                //contestantsAdapter.add(contestant);
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
        Cursor cursor = (Cursor) contestantsOnFinishSpinner.getSelectedItem();
        if (cursor != null) {
            if (cursor.isNull(cursor.getColumnIndex(Bikers._ID))) {
                return;
            }
            Long timestamp = getSelectedTimestamp();
            if (timestamp == null) {
                return;
            }

            long id = cursor.getLong(cursor.getColumnIndex(Bikers._ID));
            if (!cursor.isNull(cursor.getColumnIndex(Bikers.END_TIME))) {
                askForConfirmationForChangingEndTime(id, cursor.getString(cursor.getColumnIndex
                        (Bikers.NAME)), timestamp);
            } else {
                setEndTime(id, timestamp);
            }
        }
    }

    private Long getSelectedTimestamp() {
        Long selectedId = sensorEventsAdapter.getSelectedId();
        if (selectedId == null) {
            return null;
        }

        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(SensorEvent.CONTENT_URI, selectedId),
                new String[]{SensorEvent.TIMESTAMP}, "", new String[]{}, "");
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex(SensorEvent.TIMESTAMP));
        }
        return null;
    }

    private void askForConfirmationForChangingEndTime(final Long contestantId,
                                                      final String contestantName,
                                                      final Long event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.endTimeChangeConfirmation),
                contestantName))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setEndTime(contestantId, event);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sensorEventsAdapter.setSelectedId(null);
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

    private void setEndTime(long id, Long timestamp) {
        Uri uri = ContentUris.withAppendedId(Bikers.CONTENT_URI, id);
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Bikers.END_TIME, timestamp);
        getContentResolver().update(uri, contentValues, null, null);

        displayFromId = sensorEventsAdapter.getSelectedId();
        getLoaderManager().restartLoader(0, null, this);

        if (contestantsOnFinishAdapter.getCount() > contestantsOnFinishSpinner.getSelectedItemPosition() + 1)
            contestantsOnFinishSpinner.setSelection(contestantsOnFinishSpinner.getSelectedItemPosition() + 1);
    }

    // LoaderManager.LoaderCallbacks<Cursor> implementation
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = "";
        String[] selectionArgs = new String[0];
        String ordering = "";
        switch (i) {
            case 0:
                if (displayFromId != null) {
                    selection = "(" + SensorEvent._ID + " > ?)";
                    selectionArgs = new String[]{displayFromId.toString()};
                }
                return new CursorLoader(this, SensorEvent.CONTENT_URI, null, selection,
                        selectionArgs, null);

            case 1:
                selection = "((" + Bikers.END_TIME + " IS NULL) AND" +
                        "(" + Bikers.ON_FINISH + " IS NULL))";
                return new CursorLoader(this, Bikers.CONTENT_URI, null, selection, selectionArgs,
                        null);
            case 2:
                selection = "(" + Bikers.ON_FINISH + " IS NOT NULL)";
                ordering = Bikers.ON_FINISH + " ASC, " + Bikers.END_TIME + " ASC";
                return new CursorLoader(this, Bikers.CONTENT_URI, null, selection, selectionArgs,
                        ordering);

            default:
                throw new UnsupportedOperationException("Invalid loader id");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case 0:
                sensorEventsAdapter.swapCursor(cursor);
                break;
            case 1:
                contestantsAdapter.swapCursor(cursor);
                break;
            case 2:
                contestantsOnFinishAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case 0:
                sensorEventsAdapter.swapCursor(null);
                break;
            case 1:
                contestantsAdapter.swapCursor(null);
                break;
            case 2:
                contestantsOnFinishAdapter.swapCursor(null);
                break;
        }

    }
}
