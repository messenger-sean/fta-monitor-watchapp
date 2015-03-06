package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;

/**
 * Communicates with the pebble watchapp
 */
public class PebbleCommunicationService implements ForegroundService {

    // The app UID
    private static final String PEBBLE_UID = "5b742e45-2918-4f69-a510-7c0457d9df16";

    @Override
    public void startService(Context context, Intent intent) {

    }

    @Override
    public void stopService() {

    }
}
