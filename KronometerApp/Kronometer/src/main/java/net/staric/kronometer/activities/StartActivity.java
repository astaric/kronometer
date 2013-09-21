package net.staric.kronometer.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.staric.kronometer.backend.Update;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.ContestantAdapter;
import net.staric.kronometer.backend.ContestantBackend;
import net.staric.kronometer.backend.CountdownBackend;
import net.staric.kronometer.R;
import net.staric.kronometer.sync.KronometerContract;
import net.staric.kronometer.utils.PushUpdates;
import net.staric.kronometer.utils.Utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends Activity{
    ContestantAdapter contestantsAdapter;

    Timer timer;
    Spinner contestants;
    TextView countdown;
    MenuItem syncStatus;

    ContestantBackend contestantBackend;
    CountdownBackend countdownBackend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contestantBackend = ContestantBackend.getInstance();
        countdownBackend = CountdownBackend.getInstance();

        setContentView(R.layout.activity_main);

        contestants = (Spinner)findViewById(R.id.contestants);
        Cursor contentCursor = this.getContentResolver().query(
                KronometerContract.Bikers.CONTENT_URI, null, null, null, null);
        contentCursor.setNotificationUri(
                getContentResolver(),
                KronometerContract.Bikers.CONTENT_URI);
        SpinnerAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.listitem_contestant,
                contentCursor,
                new String[] {
                        KronometerContract.Bikers._ID,
                        KronometerContract.Bikers.NAME
                },
                new int[] { R.id.cid, R.id.name }, 0);

        contestants.setAdapter(adapter);
        countdown = (TextView)findViewById(R.id.countdown);

        timer = new Timer();
        timer.schedule(new updateCountdown(), 0, 500);

        updateSyncStatus();

        findViewById(R.id.contestants).setKeepScreenOn(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        this.syncStatus = menu.findItem(R.id.action_refresh_bikers);
        updateSyncStatus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh_bikers:
                syncContestants();
                return true;
            case R.id.action_new_contestant:
                Intent intent = new Intent(this, ContestantActivity.class);
                startActivities(new Intent[]{intent});
                return true;
            case R.id.action_finish:
                startActivities(new Intent[]{new Intent(this, FinishActivity.class)});
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void syncContestants() {
        if (Utils.hasInternetConnection(this))
            new SyncContestantListTask(this).execute();
    }

    private class updateCountdown extends TimerTask {
        @Override
        public void run() {
            StartActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int countdownValue = countdownBackend.getCountdownValue();
                    countdown.setText(
                            String.format("%02d:%02d", countdownValue / 60, countdownValue % 60));
                }
            });
        }
    }

    private class SyncContestantListTask extends AsyncTask<Void, Void, Void> {
        private final Context context;

        public SyncContestantListTask(Context context) {
            super();
            this.context = context;
        }


        @Override
        protected void onPreExecute() {
            syncStatus.setTitle("Downloading");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            contestantBackend.pull(context);
            for (Update update : contestantBackend.getPendingUpdates()) {
                contestantBackend.push(update);
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            int pending = contestantBackend.getNumberOfPendingContestants();
            syncStatus.setTitle(String.format("Uploading (%d)", pending));
        }

        @Override
        protected void onPostExecute(Void voids) {
            updateSyncStatus();
        }
    }

    private class PushStartTime extends PushUpdates {
        @Override
        protected void onProgressUpdate(Integer... progress) {
            syncStatus.setTitle(String.format("Uploading (%d/%d)", progress[0], progress[1]));
        }

        @Override
        protected void onPostExecute(Void voids) {
            updateSyncStatus();
        }
    }

    public void clickStart(View view) {
        if (contestants.getCount() == 0)
            return;

        Date startTime = new Date();
        int index = contestants.getSelectedItemPosition();
        Contestant contestant = (Contestant)contestants.getSelectedItem();

        Update update = contestant.setStartTime(startTime);
        if (update != null && Utils.hasInternetConnection(this))
            new PushStartTime().execute(update);
        countdownBackend.resetCountdown();

        if (index < contestants.getCount() - 1) {
            contestants.setSelection(index+1);
        }

        updateSyncStatus();
    }

    public void updateSyncStatus() {
        if (syncStatus != null) {
            int pending = contestantBackend.getNumberOfPendingContestants();
            if (pending == 0)
                syncStatus.setTitle("Synced");
            else
                syncStatus.setTitle(String.format("Pending (%d)", pending));
        }
    }
}
