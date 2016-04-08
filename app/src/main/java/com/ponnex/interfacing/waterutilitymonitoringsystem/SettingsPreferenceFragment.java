package com.ponnex.interfacing.waterutilitymonitoringsystem;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by Ramos on 3/29/2016.
 */
public class SettingsPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_prefs);
        getPreferenceScreen().findPreference("default_ble").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), DeviceScanActivity.class);
                startActivity(intent);
                return false;
            }
        });
    }
}
