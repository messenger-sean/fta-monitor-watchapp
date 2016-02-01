package com.fsilberberg.ftamonitor.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.fsilberberg.ftamonitor.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREVIOUS_CUSTOM_URL_KEY = "PREVIOUS_CUSTOM_URL";
    private static final String PREVIOUS_CUSTOM_SIGNALR_PORT_KEY = "PREVIOUS_CUSTOM_SIGNALR_PORT";
    private static final String PREVIOUS_CUSTOM_MONITOR_PORT_KEY = "PREVIOUS_CUSTOM_MONITOR_PORT";

    private String m_fmsKey;
    private String m_signalrPortKey;
    private String m_monitorPortKey;
    private String m_defaultKey;
    private String m_bwuKey;
    private String m_fmsEnabledKey;
    private String m_testingEnabledKey;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        m_fmsKey = getString(R.string.fms_ip_addr_key);
        m_signalrPortKey = getString(R.string.fms_signalr_port_key);
        m_monitorPortKey = getString(R.string.fms_monitor_port_key);
        m_defaultKey = getString(R.string.on_field_key);
        m_bwuKey = getString(R.string.bandwidth_key);
        m_fmsEnabledKey = getString(R.string.field_monitor_enabled_key);
        m_testingEnabledKey = getString(R.string.testing_enabled_key);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePref(m_fmsKey);
        updatePref(m_signalrPortKey);
        updatePref(m_monitorPortKey);
        updatePref(m_bwuKey);
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.settings_drawer));
        }
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_fmsKey) || key.equals(m_signalrPortKey) ||
                key.equals(m_monitorPortKey) || key.equals(m_bwuKey)) {
            updatePref(key);
        } else if (key.equals(m_defaultKey)) {
            boolean defaultUrl = sharedPreferences.getBoolean(key, true);
            String newUrl;
            String newSignalrPort;
            String newMonitorPort;
            String fmsUrlDefault = getString(R.string.fms_ip_addr_default);
            String signalrPortDefault = getString(R.string.fms_signalr_port_default);
            String monitorPortDefault = getString(R.string.fms_monitor_port_addr_default);
            // If the default url is now true, update the url to the default signalr url
            if (defaultUrl) {
                String oldUrl = sharedPreferences.getString(m_fmsKey, fmsUrlDefault);
                String oldSignalrPort = sharedPreferences.getString(m_signalrPortKey, signalrPortDefault);
                String oldMonitorPort = sharedPreferences.getString(m_monitorPortKey, monitorPortDefault);
                // Save the old default url and ports and store the new one
                sharedPreferences.edit()
                        .putString(PREVIOUS_CUSTOM_URL_KEY, oldUrl)
                        .putString(PREVIOUS_CUSTOM_SIGNALR_PORT_KEY, oldSignalrPort)
                        .putString(PREVIOUS_CUSTOM_MONITOR_PORT_KEY, oldMonitorPort)
                        .putString(m_fmsKey, fmsUrlDefault)
                        .putString(m_signalrPortKey, signalrPortDefault)
                        .putString(m_monitorPortKey, monitorPortDefault)
                        .apply();
                newUrl = fmsUrlDefault;
                newSignalrPort = signalrPortDefault;
                newMonitorPort = monitorPortDefault;
            } else {
                // Restore the old default url if it exists
                newUrl = sharedPreferences.getString(PREVIOUS_CUSTOM_URL_KEY, fmsUrlDefault);
                newSignalrPort = sharedPreferences.getString(PREVIOUS_CUSTOM_SIGNALR_PORT_KEY, signalrPortDefault);
                newMonitorPort = sharedPreferences.getString(PREVIOUS_CUSTOM_MONITOR_PORT_KEY, monitorPortDefault);
                sharedPreferences.edit()
                        .putString(m_fmsKey, newUrl)
                        .putString(m_signalrPortKey, newSignalrPort)
                        .putString(m_monitorPortKey, newMonitorPort)
                        .apply();
            }
            // The EditText widget's text field is not edited by modifying the shared preferences
            // so we have to manually get the new url and update the widget
            updatePrefText(m_fmsKey, newUrl);
            updatePrefText(m_signalrPortKey, newSignalrPort);
            updatePrefText(m_monitorPortKey, newMonitorPort);
        } else if (key.equals(m_fmsEnabledKey) || key.equals(m_testingEnabledKey)) {
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            // TODO: Fix me
//            mainIntent.putExtra(DrawerActivity.START_FRAGMENT, DrawerActivity.SETTINGS);
            getActivity().finish();
            getActivity().startActivity(mainIntent);
        }
    }

    private void updatePref(String key) {
        EditTextPreference pref = (EditTextPreference) findPreference(key);
        pref.setSummary(pref.getText());
    }

    private void updatePrefText(String key, String summary) {
        EditTextPreference pref = (EditTextPreference) findPreference(key);
        pref.setSummary(summary);
    }
}
