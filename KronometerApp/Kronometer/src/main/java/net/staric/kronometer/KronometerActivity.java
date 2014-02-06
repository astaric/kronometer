package net.staric.kronometer;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import net.staric.kronometer.activities.ContestantActivity;
import net.staric.kronometer.activities.SettingsActivity;
import net.staric.kronometer.backend.KronometerService;


public class KronometerActivity extends Activity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_new_contestant:
                intent = new Intent(this, ContestantActivity.class);
                startActivities(new Intent[]{intent});
                return true;
            case R.id.action_select_mode:
                startActivities(new Intent[]{new Intent(this, FinishActivity.class)});
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
}
