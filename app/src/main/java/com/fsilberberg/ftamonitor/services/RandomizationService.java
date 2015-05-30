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
    private boolean m_isStarted;

    public RandomizationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m_isStarted = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        m_isStarted = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    public boolean isStarted() {
        return m_isStarted;
    }

    public class RandomizationBinder extends Binder {
        public RandomizationService getService() {
            return RandomizationService.this;
        }
    }
}
