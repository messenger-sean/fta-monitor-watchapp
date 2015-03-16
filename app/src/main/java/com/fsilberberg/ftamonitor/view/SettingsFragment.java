package com.fsilberberg.ftamonitor.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.fsilberberg.ftamonitor.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEFAULT_SIGNALR_URL = "10.0.100.5";
    private static final String PREVIOUS_CUSTOM_URL_KEY = "PREVIOUS_CUSTOM_URL";

    private String m_fmsKey;
    private String m_defaultKey;
    private String m_bwuKey;
    private String m_fmEnabledKey;
    private String m_testingEnabledKey;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        m_fmsKey = getString(R.string.fms_ip_addr_key);
        m_defaultKey = getString(R.string.on_field_key);
        m_bwuKey = getString(R.string.bandwidth_key);
        m_fmEnabledKey = getString(R.string.field_monitor_enabled_key);
        m_testingEnabledKey = getString(R.string.testing_enabled_key);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePref(m_fmsKey);
        updatePref(m_bwuKey);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_settings));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_fmsKey) || key.equals(m_bwuKey)) {
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
                newUrl = DEFAULT_SIGNALR_URL;
            } else {
                // Restore the old default url if it exists
                newUrl = sharedPreferences.getString(PREVIOUS_CUSTOM_URL_KEY, DEFAULT_SIGNALR_URL);
                sharedPreferences.edit()
                        .putString(m_fmsKey, newUrl)
                        .apply();
            }
            // The EditText widget's text field is not edited by modifying the shared preferences
            // so we have to manually get the new url and update the widget
            EditTextPreference pref = (EditTextPreference) findPreference(m_fmsKey);
            pref.setSummary(newUrl);
            Log.d(SettingsFragment.class.getName(), "Text is " + pref.getSummary());
        } else if (key.equals(m_fmEnabledKey) || key.equals(m_testingEnabledKey)) {
            Intent mainIntent = new Intent(getActivity(), DrawerActivity.class);
            mainIntent.putExtra(DrawerActivity.START_FRAGMENT, DrawerActivity.SETTINGS);
            getActivity().finish();
            getActivity().startActivity(mainIntent);
        }
    }

    private void updatePref(String key) {
        EditTextPreference pref = (EditTextPreference) findPreference(key);
        pref.setSummary(pref.getText());
    }
}
