package com.fsilberberg.ftamonitor.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;
import com.fsilberberg.ftamonitor.services.MainForegroundService;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEFAULT_SIGNALR_URL = "10.0.100.5";
    private static final String PREVIOUS_CUSTOM_URL_KEY = "PREVIOUS_CUSTOM_URL";

    private String m_fmsKey;
    private String m_autoKey;
    private String m_teleopKey;
    private String m_defaultKey;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        m_fmsKey = getString(R.string.fms_ip_addr_key);
        m_autoKey = getString(R.string.auto_time_key);
        m_teleopKey = getString(R.string.teleop_time_key);
        m_defaultKey = getString(R.string.on_field_key);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_settings));
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePref(m_fmsKey);
        updatePref(m_autoKey);
        updatePref(m_teleopKey);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_fmsKey)) {
            // If the url was updated, send an update with the new url
            String url = sharedPreferences.getString(key, DEFAULT_SIGNALR_URL);
            sendServiceUpdate(url);
            updatePref(key);
        } else if (key.equals(m_defaultKey)) {
            boolean defaultUrl = sharedPreferences.getBoolean(key, true);
            String newUrl;
            // If the default url is now true, update the url to the default signalr url
            if (defaultUrl) {
                String oldUrl = sharedPreferences.getString(m_fmsKey, DEFAULT_SIGNALR_URL);
                // Save the old default url and store the new one
                sharedPreferences.edit()
                        .putString(PREVIOUS_CUSTOM_URL_KEY, oldUrl)
                        .putString(m_fmsKey, DEFAULT_SIGNALR_URL)
                        .apply();
                sendServiceUpdate(DEFAULT_SIGNALR_URL);
                newUrl = DEFAULT_SIGNALR_URL;
            } else {
                // Restore the old default url if it exists
                newUrl = sharedPreferences.getString(PREVIOUS_CUSTOM_URL_KEY, DEFAULT_SIGNALR_URL);
                sharedPreferences.edit()
                        .putString(m_fmsKey, newUrl)
                        .apply();

                sendServiceUpdate(newUrl);
            }
            // The EditText widget's text field is not edited by modifying the shared preferences
            // so we have to manually get the new url and update the widget
            EditTextPreference pref = (EditTextPreference) findPreference(m_fmsKey);
            pref.setSummary(newUrl);
            Log.d(SettingsFragment.class.getName(), "Text is " + pref.getSummary());
        } else if (key.equals(m_teleopKey) || key.equals(m_autoKey)) {
            updatePref(key);
        }
    }

    private void sendServiceUpdate(String url) {
        Log.d(SettingsFragment.class.getName(), "Sending url update with; " + url);
        Intent intent = new Intent(getActivity(), MainForegroundService.class);
        intent.putExtra(FieldConnectionService.URL_INTENT_EXTRA, url);
        intent.putExtra(FieldConnectionService.UPDATE_URL_INTENT_EXTRA, true);
        getActivity().startService(intent);
    }

    private void updatePref(String key) {
        EditTextPreference pref = (EditTextPreference) findPreference(key);
        pref.setSummary(pref.getText());
    }
}
