package net.staric.kronometer;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import net.staric.kronometer.models.Biker;
import net.staric.kronometer.models.Event;

import java.util.Date;


public class FinishFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "Kronometer.Finish";

    private static final int CONTESTANTS_LOADER = 0;
    private static final int CONTESTANTS_ON_FINISH_LOADER = 1;
    private static final int SENSOR_EVENTS_LOADER = 2;

    private ListView sensorEventsListView;
    private ListView contestantsListView;
    private Spinner contestantsOnFinishSpinner;

    private ContestantAdapter contestantsAdapter;
    private ContestantAdapter contestantsOnFinishAdapter;
    private EventAdapter sensorEventsAdapter;

    private Long displayFromId = null;

    public FinishFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_finish, container, false);

        contestantsListView = (ListView) view.findViewById(R.id.contestants);
        contestantsAdapter = new StartContestantAdapter(getActivity(), false, false);
        contestantsListView.setAdapter(contestantsAdapter);
        contestantsListView.setKeepScreenOn(true);

        contestantsOnFinishSpinner = (Spinner) view.findViewById(R.id.contestantsOnFinish);
        contestantsOnFinishAdapter = new FinishContestantAdapter(getActivity(), true, true);
        contestantsOnFinishSpinner.setAdapter(contestantsOnFinishAdapter);

        sensorEventsListView = (ListView) view.findViewById(R.id.sensorEvents);
        sensorEventsAdapter = new EventAdapter(getActivity());
        sensorEventsListView.setAdapter(sensorEventsAdapter);
        sensorEventsListView.setOnItemClickListener(new OnContestantClicked());
        sensorEventsAdapter.setOnMergeClickedListener(new OnMergeClicked());

        Button generateEventButton = (Button) view.findViewById(R.id.generateEvent);
        generateEventButton.setOnClickListener(new OnGenerateEventClicked());

        getLoaderManager().initLoader(CONTESTANTS_LOADER, null, this);
        getLoaderManager().initLoader(CONTESTANTS_ON_FINISH_LOADER, null, this);
        getLoaderManager().initLoader(SENSOR_EVENTS_LOADER, null, this);

        return view;
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

    // LoaderManager.LoaderCallback implementation
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = "";
        String[] selectionArgs = new String[0];
        String ordering = "";
        switch (i) {
            case SENSOR_EVENTS_LOADER:
                if (displayFromId != null) {
                    selection = "(" + KronometerContract.SensorEvent._ID + " > ?)";
                    selectionArgs = new String[]{displayFromId.toString()};
                }
                return new CursorLoader(getActivity(), KronometerContract.SensorEvent.CONTENT_URI, null, selection,
                        selectionArgs, null);

            case CONTESTANTS_LOADER:
                selection = "((" + KronometerContract.Bikers.END_TIME + " IS NULL) AND" +
                        "(" + KronometerContract.Bikers.ON_FINISH + " IS NULL))";
                return new CursorLoader(getActivity(), KronometerContract.Bikers.CONTENT_URI, null,
                        selection, selectionArgs,
                        null);
            case CONTESTANTS_ON_FINISH_LOADER:
                selection = "(" + KronometerContract.Bikers.ON_FINISH + " IS NOT NULL)";
                ordering = KronometerContract.Bikers.ON_FINISH + " ASC, " + KronometerContract.Bikers.END_TIME + " ASC";
                return new CursorLoader(getActivity(), KronometerContract.Bikers.CONTENT_URI, null, selection, selectionArgs,
                        ordering);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case SENSOR_EVENTS_LOADER:
                sensorEventsAdapter.swapCursor(cursor);
                break;
            case CONTESTANTS_LOADER:
                contestantsAdapter.swapCursor(cursor);
                break;
            case CONTESTANTS_ON_FINISH_LOADER:
                contestantsOnFinishAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case SENSOR_EVENTS_LOADER:
                sensorEventsAdapter.swapCursor(null);
                break;
            case CONTESTANTS_LOADER:
                contestantsAdapter.swapCursor(null);
                break;
            case CONTESTANTS_ON_FINISH_LOADER:
                contestantsOnFinishAdapter.swapCursor(null);
                break;
        }
    }

    private class OnContestantClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            sensorEventsAdapter.setSelectedId(id);
            sensorEventsAdapter.notifyDataSetChanged();
        }
    }

    private class OnMergeClicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Long contestantId = contestantsOnFinishSpinner.getSelectedItemId();
            Long timestamp = (Long) view.getTag();
            if (contestantId != 0 && timestamp != null) {
                new Biker(getActivity(), contestantId).setEndTime(timestamp);
                hideOldEvents();
                selectNextContestant();
            }
        }

        private void hideOldEvents() {
            displayFromId = sensorEventsAdapter.getSelectedId();
            getLoaderManager().restartLoader(SENSOR_EVENTS_LOADER, null, FinishFragment.this);
        }

        private void selectNextContestant() {
            if (contestantsOnFinishAdapter.getCount() > contestantsOnFinishSpinner.getSelectedItemPosition() + 1)
                contestantsOnFinishSpinner.setSelection(contestantsOnFinishSpinner.getSelectedItemPosition() + 1);
        }


    }

    private class OnGenerateEventClicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Event.create(getActivity(), new Date());
        }
    }
}
