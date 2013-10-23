package net.staric.kronometer.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.staric.kronometer.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
