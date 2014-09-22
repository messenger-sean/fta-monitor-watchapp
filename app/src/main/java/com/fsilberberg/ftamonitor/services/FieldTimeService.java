package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldUpdateType;

/**
 * This service keeps track of the current field time and
 */
public class FieldTimeService implements IForegroundService, IObserver<FieldUpdateType> {

    private static int m_timeRemaining;
    private static boolean m_timerRunning = false;

    public static int getTimeRemaining() {
        return m_timeRemaining;
    }

    public static boolean isTimerRunning() {
        return m_timerRunning;
    }

    private boolean m_timerPaused = false;
    private SharedPreferences m_sharedPreferences;
    private final MatchTimer m_timer = new MatchTimer();
    private Context m_context;
    private Thread m_timerThread;

    public FieldTimeService() {
    }

    @Override
    public void startService(Context context, Intent intent) {
        m_context = context;
        FieldMonitorFactory.getInstance().getFieldStatus().registerObserver(this);
        m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(m_context);
    }

    @Override
    public void stopService() {
        FieldMonitorFactory.getInstance().getFieldStatus().deregisterObserver(this);
        if (m_timerThread != null) {
            m_timerThread.interrupt();
            try {
                m_timerThread.join();
            } catch (InterruptedException e) {
                Log.d(FieldTimeService.class.getName(), "Error while joining the timer thread", e);
            }
        }
        m_timeRemaining = 0;
        m_timerRunning = false;
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
                updateTimer(m_context.getString(R.string.auto_time_key));
                break;
            case TELEOP:
                updateTimer(m_context.getString(R.string.teleop_time_key));
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
