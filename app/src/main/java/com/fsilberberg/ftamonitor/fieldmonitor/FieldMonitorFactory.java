package com.fsilberberg.ftamonitor.fieldmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fsilberberg.ftamonitor.R;

/**
 * Main factory for all Field Monitor related objects.
 */
public class FieldMonitorFactory {

    private static FieldMonitorFactory instance;

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new FieldMonitorFactory(context);
        }
    }

    public static FieldMonitorFactory getInstance() {
        if (instance == null) {
            throw new RuntimeException("Error: Accessing the field monitor factory before initialization. You must call FieldMonitorFactory.initialize() before getInstance()");
        }
        return instance;
    }

    private FieldStatus fieldStatus;

    private FieldMonitorFactory(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int autoTime = Integer.valueOf(prefs.getString(context.getString(R.string.auto_time_key), "10"));
        int teleopTime = Integer.valueOf(prefs.getString(context.getString(R.string.teleop_time_key), "140"));
        fieldStatus = new FieldStatus(autoTime, teleopTime);
    }

    public FieldStatus getFieldStatus() {
        return fieldStatus;
    }
}
