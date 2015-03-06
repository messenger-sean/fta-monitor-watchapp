package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Maintains the connection to the field.
 */
public class FieldConnectionService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // We don't support service binding
        return null;
    }
}
