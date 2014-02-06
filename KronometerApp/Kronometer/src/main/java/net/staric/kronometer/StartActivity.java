package net.staric.kronometer;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static net.staric.kronometer.KronometerContract.Bikers;

public class StartActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String NEXT_START_TIME = "next_start";
    private static final int CONTESTANT_LOADER = 0;

    private Spinner contestants;
    private ContestantAdapter contestantsAdapter;
    private TextView countdown;

    private Date nextStart = new Date();
    private Timer countdownRefreshTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        if (savedInstanceState != null) {
            nextStart = new Date(savedInstanceState.getLong(NEXT_START_TIME));
        }

        contestants = (Spinner) findViewById(R.id.contestants);
        countdown = (TextView) findViewById(R.id.countdown);

        contestantsAdapter = new StartContestantAdapter(this, true, true);
        contestants.setAdapter(contestantsAdapter);
        contestants.setKeepScreenOn(true);
        getLoaderManager().initLoader(CONTESTANT_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startRefreshingCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRefreshingCountdown();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(NEXT_START_TIME, nextStart.getTime());

        super.onSaveInstanceState(outState);
    }


    public void clickStart(View view) {
        if (contestants.getCount() == 0) {
            return;
        }

        long timestamp = new Date().getTime();
        long contestantId = contestants.getSelectedItemId();

        setStartTime(contestantId, timestamp);
        resetCountdown();
        selectNextContestant();
    }

    private void setStartTime(long contestantId, Long startTime) {
        Uri uri = ContentUris.withAppendedId(Bikers.CONTENT_URI, contestantId);
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Bikers.START_TIME, startTime);
        getContentResolver().update(uri, contentValues, null, null);
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
                StartActivity.this.runOnUiThread(new Runnable() {
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
                return new CursorLoader(StartActivity.this, Bikers.CONTENT_URI, null, null, null, null);
            default:
                throw new UnsupportedOperationException("Unknown loader id");
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case CONTESTANT_LOADER:
                contestantsAdapter.swapCursor(cursor);
            default:
                throw new UnsupportedOperationException("Unknown loader id");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case CONTESTANT_LOADER:
                contestantsAdapter.swapCursor(null);
            default:
                throw new UnsupportedOperationException("Unknown loader id");
        }
    }
}
