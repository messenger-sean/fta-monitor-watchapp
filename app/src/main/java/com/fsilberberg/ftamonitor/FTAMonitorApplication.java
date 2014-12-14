package com.fsilberberg.ftamonitor;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;
import com.fsilberberg.ftamonitor.services.MainForegroundService;

/**
 * This is the main entry point of the application. On startup, it will create the main foreground
 * service responsible for maintaining all app functions
 */
public class FTAMonitorApplication extends Application {

    private static Context _context;
    public static final String DEFAULT_IP = "10.0.100.5";

    public static Context getContext() {
        return _context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _context = getApplicationContext();

        // Initialize the field monitor factory
        FieldMonitorFactory.initialize(this);

        // Start the FMS Service
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url = preferences.getString(getString(R.string.fms_ip_addr_key), DEFAULT_IP);
        Intent serviceIntent = new Intent(getBaseContext(), MainForegroundService.class);
        serviceIntent.putExtra(FieldConnectionService.URL_INTENT_EXTRA, url);
        startService(serviceIntent);
    }
}
