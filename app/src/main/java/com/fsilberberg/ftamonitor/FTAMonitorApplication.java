package com.fsilberberg.ftamonitor;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fsilberberg.ftamonitor.fieldmonitor.FieldConnectionService;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;

/**
 * Created by Fredric on 8/23/14.
 */
public class FTAMonitorApplication extends Application {

    private static Context _context;

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
        String url = preferences.getString(getString(R.string.fms_ip_addr_key), "10.0.100.5");
        Intent serviceIntent = new Intent(getBaseContext(), FieldConnectionService.class);
        serviceIntent.putExtra(FieldConnectionService.URL_INTENT_EXTRA, url);
        startService(serviceIntent);
    }
}
