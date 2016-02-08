package com.fsilberberg.ftamonitor;

import android.app.Application;
import android.content.Context;

import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.services.ServicePreferenceListener;

/**
 * This is the main entry point of the application. On startup, it will create the main foreground
 * service responsible for maintaining all app functions
 */
public class FTAMonitorApplication extends Application {

    private static volatile Context _context;

    public static Context getContext() {
        return _context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _context = getApplicationContext();

        // Initialize the field monitor factory
        FieldMonitorFactory.initialize();

        // Register the global shared preference listener and update all services
        new ServicePreferenceListener(_context).updateAllServices();
    }
}
