package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.SharedPreferences;
import com.fsilberberg.ftamonitor.R;

/**
 * This is a shared preference listener that starts or stops services when they are enabled, disabled, or their settings
 * are changed
 */
public class ServicePreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final Context m_context;
    private final String m_fmsEnabledKey;
    private final String m_fmsIpKey;
    private final String m_bwuKey;
    private final String m_pebbleEnabledKey;
    private final String m_pebbleOutOfMatchKey;
    private final String m_pebbleVibeIntervalKey;
    private final String m_notificationEnabledKey;
    private final String m_notificationOutOfMatchKey;

    public ServicePreferenceListener(Context context) {
        m_context = context;
        m_fmsEnabledKey = m_context.getString(R.string.field_monitor_enabled_key);
        m_fmsIpKey = m_context.getString(R.string.fms_ip_addr_key);
        m_bwuKey = m_context.getString(R.string.bandwidth_key);
        m_pebbleEnabledKey = m_context.getString(R.string.pebble_key);
        m_pebbleOutOfMatchKey = m_context.getString(R.string.pebble_notify_times_key);
        m_pebbleVibeIntervalKey = m_context.getString(R.string.pebble_vibe_interval_key);
        m_notificationEnabledKey = m_context.getString(R.string.notification_key);
        m_notificationOutOfMatchKey = m_context.getString(R.string.notify_always_key);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_fmsEnabledKey) || key.equals(m_fmsIpKey)) {
            updateFmsConnection();
        } else if (key.equals(m_pebbleEnabledKey) ||
                key.equals(m_pebbleOutOfMatchKey) ||
                key.equals(m_pebbleVibeIntervalKey)) {
            updatePebbleConnection();
        } else if (key.equals(m_notificationEnabledKey) || key.equals(m_notificationOutOfMatchKey)) {
            updateNotification();
        } else if (key.equals(m_bwuKey)) {
            updatePebbleConnection();
            updateNotification();
        }
    }

    /**
     * Updates the field services, restarting them if necessary
     */
    private void updateFmsConnection() {
        FieldConnectionService.start(true);
    }

    private void updatePebbleConnection() {
        PebbleCommunicationService.start();
    }

    private void updateNotification() {
        FieldProblemNotificationService.start();
    }

    public void updateAllServices() {
        updateFmsConnection();
        updatePebbleConnection();
        updateNotification();
    }
}
