package net.staric.kronometer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{
    ContestantAdapter contestantsAdapter;

    Timer timer;
    Spinner contestants;
    TextView countdown;

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
                                                   R.layout.contestant_listitem,
                                                   contestantBackend.getContestants());
        contestants.setAdapter(contestantsAdapter);
        countdown = (TextView)findViewById(R.id.countdown);

        timer = new Timer();
        timer.schedule(new updateCountdown(), 0, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh_bikers:
                updateContestants();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void updateContestants() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new UpdateContestantListTask().execute();
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

    private class UpdateContestantListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity.this.contestantBackend.updateContestants();
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            MainActivity.this.contestantsAdapter.notifyDataSetChanged();
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
    }
}
