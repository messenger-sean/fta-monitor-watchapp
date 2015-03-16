package com.fsilberberg.ftamonitor;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;
import com.fsilberberg.ftamonitor.services.FieldProblemNotificationService;
import com.fsilberberg.ftamonitor.services.PebbleCommunicationService;

/**
 * This is the main entry point of the application. On startup, it will create the main foreground
 * service responsible for maintaining all app functions
 */
public class FTAMonitorApplication extends Application {

    private static Context _context;
    public static final String DEFAULT_IP = "10.0.100.5:8189";

    public static Context getContext() {
        return _context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _context = getApplicationContext();

        // Initialize the field monitor factory
        FieldMonitorFactory.initialize();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String fmEnabledKey = getString(R.string.field_monitor_enabled_key);
        String pebbleEnabledKey = getString(R.string.pebble_key);
        String notificationKey = getString(R.string.notification_key);
        boolean fmEnabled = preferences.getBoolean(fmEnabledKey, true);

        // If the field monitor is enabled, start the connection service
        if (fmEnabled) {
            FieldConnectionService.start();

            // If the Pebble service is also enabled, start that
            boolean pebbleEnabled = preferences.getBoolean(pebbleEnabledKey, false);
            if (pebbleEnabled) {
                PebbleCommunicationService.start();
            }

            // If the notification service is also enabled start that
            boolean notificationEnabled = preferences.getBoolean(notificationKey, false);
            if (notificationEnabled) {
                FieldProblemNotificationService.start();
            }
        }
    }
}
