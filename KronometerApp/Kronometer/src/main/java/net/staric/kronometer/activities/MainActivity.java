package net.staric.kronometer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.ContestantAdapter;
import net.staric.kronometer.ContestantBackend;
import net.staric.kronometer.CountdownBackend;
import net.staric.kronometer.R;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{
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
        contestantsAdapter = new ContestantAdapter(this,
                                                   R.layout.listitem_contestant,
                                                   contestantBackend.getContestants());
        contestants.setAdapter(contestantsAdapter);
        countdown = (TextView)findViewById(R.id.countdown);

        timer = new Timer();
        timer.schedule(new updateCountdown(), 0, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        this.syncStatus = menu.findItem(R.id.action_refresh_bikers);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void syncContestants() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SyncContestantListTask().execute();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("No connection")
                    .setMessage("Your internet connections is not available.")
                    .setNeutralButton("Close", null)
                    .show();
        }
    }

    private class updateCountdown extends TimerTask {
        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
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
        @Override
        protected void onPreExecute() {
            syncStatus.setTitle("Downloading");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            contestantBackend.pull();
            for (Contestant contestant : contestantBackend.getPendingContestants()) {
                contestantBackend.pushContestant(contestant);
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
            contestantsAdapter.notifyDataSetChanged();
            updateSyncStatus();
        }
    }

    public void clickStart(View view) {
        if (contestants.getCount() == 0)
            return;

        Date startTime = new Date();
        int index = contestants.getSelectedItemPosition();
        Contestant contestant = (Contestant)contestants.getSelectedItem();

        contestantBackend.updateStartTime(contestant, startTime);
        countdownBackend.resetCountdown();

        contestantsAdapter.notifyDataSetChanged();
        if (index < contestants.getCount() - 1) {
            contestants.setSelection(index+1);
        }

        updateSyncStatus();
    }

    public void updateSyncStatus() {
        int pending = contestantBackend.getNumberOfPendingContestants();
        if (pending == 0)
            syncStatus.setTitle("Synced");
        else
            syncStatus.setTitle(String.format("Pending (%d)", pending));
    }
}
