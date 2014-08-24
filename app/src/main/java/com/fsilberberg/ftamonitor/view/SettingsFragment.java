package com.fsilberberg.ftamonitor.view;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fsilberberg.ftamonitor.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String fmsKey;
    private String autoKey;
    private String teleopKey;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        fmsKey = getString(R.string.fms_ip_addr_key);
        autoKey = getString(R.string.auto_time_key);
        teleopKey = getString(R.string.telelop_time_key);
        getActivity().getActionBar().setTitle(getString(R.string.action_settings));
    }

    @Override
    public void onResume() {
        super.onResume();
        EditTextPreference fmsPref = (EditTextPreference) findPreference(fmsKey);
        fmsPref.setSummary(fmsPref.getText());
        EditTextPreference autoPref = (EditTextPreference) findPreference(autoKey);
        autoPref.setSummary(autoPref.getText());
        EditTextPreference teleopPref = (EditTextPreference) findPreference(teleopKey);
        teleopPref.setSummary(teleopPref.getText());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s == fmsKey || s == autoKey || s == teleopKey) {
            EditTextPreference pref = (EditTextPreference) findPreference(s);
            pref.setSummary(pref.getText());
        }
    }
}
