package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.SharedPreferences;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;

/**
 * Manages the state of the field and team observers
 */
public class FieldServiceManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final FieldServiceManager instance = new FieldServiceManager();

    public static FieldServiceManager getInstance() {
        return instance;
    }

    private final String m_notificationServiceKey;
    private final String m_pebbleServiceKey;
    private final Context m_ctx;
    private FieldProblemNotificationService m_notificationService;
    private PebbleCommunicationService m_pebbleService;

    public FieldServiceManager() {
        m_ctx = FTAMonitorApplication.getContext();
        m_notificationServiceKey = m_ctx.getString(R.string.notification_key);
        m_pebbleServiceKey = m_ctx.getString(R.string.pebble_key);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateServices(sharedPreferences);
    }

    public void updateServices(SharedPreferences prefs) {
        boolean notificationsEnabled = prefs.getBoolean(m_notificationServiceKey, false);
        boolean pebbleEnabled = prefs.getBoolean(m_pebbleServiceKey, false);

        // If notifications are enabled, enable them. Otherwise, disable them
        if (notificationsEnabled) {
            if (m_notificationService != null) {
                m_notificationService.stopService();
                m_notificationService = null;
            }

            m_notificationService = new FieldProblemNotificationService();
            m_notificationService.startService(m_ctx);
        } else {
            if (m_notificationService != null) {
                m_notificationService.stopService();
                m_notificationService = null;
            }
        }

        // If pebble integration is enabled, enable it. Otherwise, disable it
        if (pebbleEnabled) {
            if (m_pebbleService != null) {
                m_pebbleService.stopService();
                m_pebbleService = null;
            }

            m_pebbleService = new PebbleCommunicationService();
            m_pebbleService.startService(m_ctx);
        } else {
            if (m_pebbleService != null) {
                m_pebbleService.stopService();
                m_pebbleService = null;
            }
        }
    }
}
