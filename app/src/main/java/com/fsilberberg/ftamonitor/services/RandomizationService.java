package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Randomizes the values seen on the field monitor every second for testing
 */
public class RandomizationService extends Service {

    private final RandomizationBinder m_binder = new RandomizationBinder();

    public RandomizationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    public class RandomizationBinder extends Binder {
        public RandomizationService getService() {
            return RandomizationService.this;
        }
    }
}
