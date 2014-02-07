package net.staric.kronometer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import net.staric.kronometer.activities.ContestantActivity;
import net.staric.kronometer.activities.SettingsActivity;

public class MainActivity extends FragmentActivity {
    private static final String CURRENT_MODE = "current_mode";

    private static final int MODE_SIGNUP = 0;
    private static final int MODE_START = 1;
    private static final int MODE_FINISH = 2;

    private int currentMode = MODE_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Intent kronometerService = new Intent(this, KronometerService.class);
            startService(kronometerService);
            currentMode = MODE_START;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new StartFragment())
                    .commit();
        } else {
            currentMode = savedInstanceState.getInt(CURRENT_MODE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_MODE, currentMode);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_select_mode:
                showSelectModeDialog();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_new_contestant:
                intent = new Intent(this, ContestantActivity.class);
                startActivities(new Intent[]{intent});
                return true;
            case R.id.action_exit:
                stopService(new Intent(this, KronometerService.class));
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSelectModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"Signup", "Start", "Finish"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int mode) {
                        if (mode == currentMode) {
                            return;
                        }
                        switch (mode) {
                            case MODE_SIGNUP:
                                break;
                            case MODE_START:
                                currentMode = MODE_START;
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new StartFragment())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case MODE_FINISH:
                                currentMode = MODE_FINISH;
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new FinishFragment())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                        }
                        getSupportFragmentManager().executePendingTransactions();
                    }
                })
                .create()
                .show();
    }
}
