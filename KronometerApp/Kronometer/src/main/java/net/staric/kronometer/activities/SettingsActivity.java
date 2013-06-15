package net.staric.kronometer.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.staric.kronometer.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
