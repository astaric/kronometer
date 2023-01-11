package net.staric.kronometer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends FragmentActivity {
    private static final String CURRENT_MODE = "current_mode";
    private static final String CURRENT_SENSOR = "current_sensor";

    private static final int MODE_SIGNUP = 0;
    private static final int MODE_START = 1;
    private static final int MODE_FINISH = 2;

    private static final int NO_SENSOR = 0;
    private static final String NO_SENSOR_ADDRESS = "";
    private static final int SENSOR_MAC = 1;
    private static final String SENSOR_MAC_ADDRESS = "14:10:9F:D7:9A:74";
    private static final int SENSOR_V = 2;
    private static final String SENSOR_V_ADDRESS = "20:13:08:01:04:98";
    private static final int SENSOR_U = 3;
    private static final String SENSOR_U_ADDRESS = "20:13:08:01:03:11";

    private int selectedMode;
    private String selectedSensorAddress;

    private KronometerService kronometerService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);

        SharedPreferences settings = getPreferences(0);
        selectedMode = settings.getInt(CURRENT_MODE, MODE_START);
        showMode(selectedMode);

        selectedSensorAddress = settings.getString(CURRENT_SENSOR, NO_SENSOR_ADDRESS);
        selectSensor(selectedSensorAddress);

        Intent kronometerService = new Intent(this, KronometerService.class);
        startService(kronometerService);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, KronometerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bound) {
            unbindService(connection);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            kronometerService = ((KronometerService.LocalBinder)iBinder).getService();
            kronometerService.setSensorAddress(selectedSensorAddress);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

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
            case R.id.action_select_sensor:
                showSelectSensorDialog();
                return true;
            case R.id.action_exit:
                stopService(new Intent(this, KronometerService.class));
                finish();
                System.exit(0);
                return true;
            case R.id.action_reset:
                showConfirmResetDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSelectSensorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"No sensor", "MacBook", "Sensor V", "Sensor U"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int sensor) {
                        String address;
                        switch (sensor) {
                            case NO_SENSOR:
                                selectSensor(NO_SENSOR_ADDRESS);
                                break;
                            case SENSOR_MAC:
                                selectSensor(SENSOR_MAC_ADDRESS);
                                break;
                            case SENSOR_V:
                                selectSensor(SENSOR_V_ADDRESS);
                                break;
                            case SENSOR_U:
                                selectSensor(SENSOR_U_ADDRESS);
                                break;
                        }
                    }
                }
        )
                .create()
                .show();
    }

    private void showSelectModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new String[]{"Add Contestant", "Start", "Finish"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int mode) {
                        if (mode == selectedMode) {
                            return;
                        }
                        switch (mode) {
                            case MODE_SIGNUP:
                                String url = "http://kronometer.staric.net/admin/biker/biker/add/";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                return;
                            case MODE_START:
                                selectMode(MODE_START);
                                break;
                            case MODE_FINISH:
                                selectMode(MODE_FINISH);
                                break;
                        }
                    }
                }
        )
                .create()
                .show();
    }

    private void selectMode(int mode) {
        selectedMode = mode;

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(CURRENT_MODE, selectedMode);
        editor.commit();

        showMode(selectedMode);
    }

    private void showMode(int mode) {
        switch (mode) {
            case MODE_START:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new StartFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case MODE_FINISH:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new FinishFragment())
                        .addToBackStack(null)
                        .commit();
                break;
        }
        getSupportFragmentManager().executePendingTransactions();
    }

    private void selectSensor(String address) {
        selectedSensorAddress = address;

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(CURRENT_SENSOR, selectedSensorAddress);
        editor.commit();

        if (bound) {
            kronometerService.setSensorAddress(selectedSensorAddress);
        }
    }

    private void showConfirmResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to remove all local data and" +
                " sync from the server?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContentResolver contentResolver = getContentResolver();
                        contentResolver.delete(KronometerContract.Bikers.CONTENT_URI, null, null);
                        contentResolver.delete(KronometerContract.SensorEvent.CONTENT_URI, null, null);

                        Bundle settingsBundle = new Bundle();
                        settingsBundle.putBoolean(
                                ContentResolver.SYNC_EXTRAS_MANUAL, true);
                        settingsBundle.putBoolean(
                                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                        String AUTHORITY = "net.staric.kronometer";
                        ContentResolver.requestSync(null, AUTHORITY, settingsBundle);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }
}
