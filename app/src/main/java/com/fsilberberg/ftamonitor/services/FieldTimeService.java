package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldUpdateType;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * Created by Fredric on 9/21/14.
 */
public class FieldTimeService extends Service implements IObserver<FieldUpdateType> {

    private static int m_timeRemaining;
    private static boolean m_timerRunning = false;

    public static int getTimeRemaining() {
        return m_timeRemaining;
    }

    public static boolean isTimerRunning() {
        return m_timerRunning;
    }

    private final IBinder m_binder = new FieldTimeBinder();
    private boolean m_timerPaused = false;
    private SharedPreferences m_sharedPreferences;
    private PowerManager.WakeLock m_wl;
    private final MatchTimer m_timer = new MatchTimer();
    private Thread m_timerThread;

    @Override
    public void onCreate() {
        super.onCreate();
        m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        m_wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, FieldTimeService.class.getName());
        FieldMonitorFactory.getInstance().getFieldStatus().registerObserver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_wl != null && m_wl.isHeld()) {
            m_wl.release();
        }
        FieldMonitorFactory.getInstance().getFieldStatus().deregisterObserver(this);
    }

    @Override
    public void update(FieldUpdateType updateType) {
        switch (updateType) {
            case MATCH_STATUS:
                updateMatchStatus();
        }
    }

    private void updateMatchStatus() {
        switch (FieldMonitorFactory.getInstance().getFieldStatus().getMatchStatus()) {
            case AUTO:
                updateTimer(getString(R.string.auto_time_key));
                break;
            case TELEOP:
                updateTimer(getString(R.string.teleop_time_key));
                break;
            case TELEOP_PAUSED:
            case AUTO_PAUSED:
                m_timerPaused = true;
                m_timerRunning = false;
                break;
            case AUTO_END:
            case OVER:
                // Prestart initiated is the best way to tell if we need to reset the timer
            case PRESTART_COMPLETED:
                m_timerRunning = false;
                m_timerPaused = false;
                m_timeRemaining = 0;

        }
    }

    /**
     * Updates the current timer to be the correct new value. If the timer is paused, it will set
     * the correct resume time for now. Otherwise, it will start a new timer with the value in
     * shared preferences stored at the given key
     *
     * @param key The key of the new timer value
     */
    private void updateTimer(String key) {
        if (!m_timerPaused) {
            m_timeRemaining = Integer.valueOf(m_sharedPreferences.getString(key, "0"));
        }
        m_timerPaused = false;
        m_timerThread = new Thread(m_timer);
        m_timerThread.start();
        m_timerRunning = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    public class FieldTimeBinder extends Binder {
        public FieldTimeService getService() {
            return FieldTimeService.this;
        }
    }

    private class MatchTimer implements Runnable {

        @Override
        public void run() {
            m_timeRemaining++;
            long startTime;
            while (!Thread.interrupted() && m_timerRunning) {
                startTime = System.currentTimeMillis();
                m_timeRemaining -= 1;
                if (m_timeRemaining <= 0) {
                    m_timeRemaining = 0;
                    m_timerRunning = false;
                    break;
                }
                try {
                    Thread.sleep(1000 - (System.currentTimeMillis() - startTime), 0);
                } catch (InterruptedException e) {
                    Log.d(FieldTimeService.class.getName(), "Error while sleeping during timer countdown", e);
                }

            }
        }
    }
}
