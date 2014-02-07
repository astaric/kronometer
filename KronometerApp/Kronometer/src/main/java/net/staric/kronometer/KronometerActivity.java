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


}
