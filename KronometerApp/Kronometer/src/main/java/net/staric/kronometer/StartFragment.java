package net.staric.kronometer;


import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class StartFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String NEXT_START_TIME = "next_start";
    private static final int CONTESTANT_LOADER = 0;
    private static final String TAG = "Kronometer.Start";

    private Spinner contestants;
    private ContestantAdapter contestantsAdapter;
    private TextView countdown;

    private Date nextStart = new Date();
    private Timer countdownRefreshTimer;

    public StartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        contestants = (Spinner) view.findViewById(R.id.contestants);
        countdown = (TextView) view.findViewById(R.id.countdown);

        contestantsAdapter = new StartContestantAdapter(getActivity(), true, true);
        contestants.setAdapter(contestantsAdapter);
        contestants.setKeepScreenOn(true);

        Button startButton = (Button) view.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long timestamp = new Date().getTime();
                long contestantId = contestants.getSelectedItemId();

                setStartTime(contestantId, timestamp);
                resetCountdown();
                selectNextContestant();
            }
        });

        getLoaderManager().initLoader(CONTESTANT_LOADER, null, this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            nextStart = new Date(savedInstanceState.getLong(NEXT_START_TIME));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(NEXT_START_TIME, nextStart.getTime());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        startRefreshingCountdown();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopRefreshingCountdown();
    }

    private void setStartTime(long contestantId, Long startTime) {
        Uri uri = ContentUris.withAppendedId(KronometerContract.Bikers.CONTENT_URI, contestantId);
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(KronometerContract.Bikers.START_TIME, startTime);
        getActivity().getContentResolver().update(uri, contentValues, null, null);
    }

    private void selectNextContestant() {
        int index = contestants.getSelectedItemPosition();
        if (index < contestants.getCount() - 1) {
            contestants.setSelection(index + 1);
        }
    }

    // Countdown related stuff
    private void startRefreshingCountdown() {
        countdownRefreshTimer = new Timer();
        countdownRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCountdown();
                    }
                });
            }

        }, 0, 500);
    }

    private void stopRefreshingCountdown() {
        countdownRefreshTimer.cancel();
        countdownRefreshTimer = null;
    }

    private void updateCountdown() {
        int countdownValue = (int) (nextStart.getTime() - new Date().getTime()) / 1000;
        if (countdownValue < 0) {
            countdownValue = 0;
        }
        countdown.setText(
                String.format("%02d:%02d", countdownValue / 60, countdownValue % 60));
    }

    public void resetCountdown() {
        nextStart = new Date(new Date().getTime() + 30000);
    }

    // LoaderManager.LoaderCallbacks<Cursor> implementation
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case CONTESTANT_LOADER:
                return new CursorLoader(getActivity(), KronometerContract.Bikers.CONTENT_URI,
                        null, null, null, null);
            default:
                throw new UnsupportedOperationException("Unknown loader id");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case CONTESTANT_LOADER:
                contestantsAdapter.swapCursor(cursor);
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case CONTESTANT_LOADER:
                contestantsAdapter.swapCursor(null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id");
        }
    }
}
