package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.services.pebble.PebbleCommunicationService;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is a shared preference listener that starts or stops services when they are enabled, disabled, or their settings
 * are changed
 */
public class ServicePreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String m_fmsEnabledKey;
    private final String m_fmsIpKey;
    private final Collection<String> m_pebbleUpdateKeys;
    private final Collection<String> m_notificationUpdateKeys;
    private final Context m_context;

    public ServicePreferenceListener(Context context) {
        m_context = context;
        m_fmsEnabledKey = context.getString(R.string.field_monitor_enabled_key);
        m_fmsIpKey = context.getString(R.string.fms_ip_addr_key);
        String bwuKey = context.getString(R.string.bandwidth_key);
        String lowBatKey = context.getString(R.string.low_battery_key);
        m_pebbleUpdateKeys = Arrays.asList(context.getString(R.string.pebble_key),
                context.getString(R.string.pebble_notify_times_key),
                context.getString(R.string.pebble_vibe_interval_key),
                context.getString(R.string.pebble_bandwidth_notify_key),
                context.getString(R.string.pebble_low_battery_notify_key),
                bwuKey, lowBatKey);
        m_notificationUpdateKeys = Arrays.asList(context.getString(R.string.notification_key),
                context.getString(R.string.notify_always_key),
                context.getString(R.string.low_battery_notify_key),
                context.getString(R.string.bandwidth_notify_key),
                bwuKey, lowBatKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_fmsEnabledKey) || key.equals(m_fmsIpKey)) {
            updateFmsConnection();
        }
        if (m_pebbleUpdateKeys.contains(key)) {
            updatePebbleConnection();
        }
        if (m_notificationUpdateKeys.contains(key)) {
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
        PebbleCommunicationService.start(m_context);
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
