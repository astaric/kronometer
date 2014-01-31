package net.staric.kronometer.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.staric.kronometer.R;
import net.staric.kronometer.backend.ContestantBackend;
import net.staric.kronometer.backend.CountdownBackend;
import net.staric.kronometer.backend.Update;
import net.staric.kronometer.sync.KronometerContract;
import net.staric.kronometer.utils.PushUpdates;
import net.staric.kronometer.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static net.staric.kronometer.sync.KronometerContract.Bikers;

public class StartActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>  {
    Timer timer;
    Spinner contestants;
    TextView countdown;
    MenuItem syncStatus;

    ContestantBackend contestantBackend;
    CountdownBackend countdownBackend;

    public static final String ACCOUNT_TYPE = "kronometer.staric.net";
    public static final String ACCOUNT = "x";
    private SimpleCursorAdapter contestantsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contestantBackend = ContestantBackend.getInstance();
        countdownBackend = CountdownBackend.getInstance();

        setContentView(R.layout.activity_start);

        contestants = (Spinner)findViewById(R.id.contestants);
        countdown = (TextView)findViewById(R.id.countdown);

        contestantsAdapter = getContestantsAdapter();
        contestants.setAdapter(contestantsAdapter);


        timer = new Timer();
        timer.schedule(new updateCountdown(), 0, 500);

        updateSyncStatus();

        contestants.setKeepScreenOn(true);

        Account account = CreateSyncAccount(this);

        getLoaderManager().initLoader(0, null, this);
    }

    private SimpleCursorAdapter getContestantsAdapter() {
        String[] fields = new String[]{Bikers._ID, Bikers.NAME, Bikers.START_TIME};
        int[] views = new int[] { R.id.cid, R.id.name, R.id.startTime };
        return new SimpleCursorAdapter(this, R.layout.listitem_contestant, null, fields, views, 0)
        {
            @Override
            public void setViewText(TextView v, String text) {
                switch(v.getId())
                {
                    case R.id.startTime:
                        if (!text.isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            Long time = Long.parseLong(text);
                            final Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(time);
                            text = sdf.format(cal.getTime());

                        }
                        break;
                }
                super.setViewText(v, text);
            }
        };
    }

    public static Account CreateSyncAccount(Context context) {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

        AccountManager accountManager =
                (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            return newAccount;
        } else {
            return null;
        }
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh_bikers:
                syncContestants();
                return true;
            case R.id.action_new_contestant:
                intent = new Intent(this, ContestantActivity.class);
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

        try {
            Date startTime = new Date();

            Cursor selectedItem = (Cursor) contestants.getSelectedItem();
            int contestantId = selectedItem.getInt(selectedItem.getColumnIndex(Bikers._ID));
            setStartTime(contestantId, startTime);

            countdownBackend.resetCountdown();

            int index = contestants.getSelectedItemPosition();
            if (index < contestants.getCount() - 1) {
                contestants.setSelection(index+1);
            }

            updateSyncStatus();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setStartTime(int contestantId, Date startTime) {
        Uri uri = ContentUris.withAppendedId(Bikers.CONTENT_URI, contestantId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(Bikers.START_TIME, startTime.getTime());

        getContentResolver().update(uri, contentValues, null, null);
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

    // LoaderManager.LoaderCallbacks<Cursor> implementation
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(StartActivity.this, Bikers.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        contestantsAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        contestantsAdapter.swapCursor(null);
    }
}
