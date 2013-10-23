package net.staric.kronometer.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import net.staric.kronometer.R;


public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
