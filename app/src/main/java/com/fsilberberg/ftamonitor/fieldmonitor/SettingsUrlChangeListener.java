package com.fsilberberg.ftamonitor.fieldmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Fredric on 8/25/14.
 */
public class SettingsUrlChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEFAULT_SIGNALR_URL = "10.0.100.5";

    private final String m_defaultKey;
    private final String m_urlKey;
    private final Context m_context;

    public SettingsUrlChangeListener(String defaultKey, String urlKey, Context context) {
        this.m_defaultKey = defaultKey;
        this.m_urlKey = urlKey;
        m_context = context;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_defaultKey)) {
            boolean defaultUrl = sharedPreferences.getBoolean(key, true);
            // If the default url is now true, update the url to the default signalr url
            if (defaultUrl) {
                sharedPreferences.edit().putString(m_urlKey, DEFAULT_SIGNALR_URL).commit();
                sendServiceUpdate(DEFAULT_SIGNALR_URL);
            }
        } else if (key.equals(m_urlKey)) {
            // If the url was updated, send an update with the new url
            sendServiceUpdate(sharedPreferences.getString(key, DEFAULT_SIGNALR_URL));
        }
    }

    private void sendServiceUpdate(String url) {
        Log.d(SettingsUrlChangeListener.class.getName(), "Sending url update with; " + url);
        Intent intent = new Intent(m_context, FieldConnectionService.class);
        intent.putExtra(FieldConnectionService.URL_INTENT_EXTRA, url);
        intent.putExtra(FieldConnectionService.UPDATE_URL_INTENT_EXTRA, true);
        m_context.startService(intent);
    }
}
