package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.*;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import com.fsilberberg.ftamonitor.R;
import microsoft.aspnet.signalr.client.ConnectionState;

import java.util.Random;

/**
 * Randomizes the values seen on the field monitor every second for testing
 */
public class RandomizationService extends Service {

    private final RandomizationBinder m_binder = new RandomizationBinder();
    private final ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FieldConnectionService.FCSBinder binder = (FieldConnectionService.FCSBinder) service;
            m_stateObservable = binder.getService().getStatusObservable();
            m_isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_isBound = false;
            m_stateObservable = null;
        }
    };
    private FieldConnectionService.ConnectionStateObservable m_stateObservable;
    private Thread m_randomThread;
    private boolean m_isBound = false;
    private boolean m_isStarted = false;
    private boolean m_shouldUpdate = false;

    public RandomizationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (this) {
            m_shouldUpdate = true;
            // If we've already started, then we're done, just return
            if (m_isStarted) {
                return START_STICKY;
            } else {
                m_isStarted = true;
            }
        }

        if (!m_isBound) {
            bindService(new Intent(this, FieldConnectionService.class), m_connection, Context.BIND_AUTO_CREATE);
        }

        m_randomThread = new Thread(new RandomThread());
        m_randomThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        synchronized (this) {
            m_isStarted = false;
        }

        if (m_randomThread != null) {
            m_randomThread.interrupt();
            m_randomThread = null;
        }

        if (m_isBound) {
            unbindService(m_connection);
        }
    }

    public boolean isStarted() {
        return m_isStarted;
    }

    /**
     * Enables or disables the randomization service
     *
     * @param toEnable Whether to enable or disable the service
     * @param context  The context for sending stop/startService intents
     */
    public void setEnabled(boolean toEnable, Context context) {
        if (toEnable) {
            // If we have not started, then we need to start up the service. Otherwise, do nothing, the service is
            // already running
            synchronized (this) {
                if (!m_isStarted) {
                    context.startService(new Intent(context, RandomizationService.class));
                }
            }
        } else {
            synchronized (this) {
                if (!m_isStarted) {
                    context.stopService(new Intent(context, RandomizationService.class));
                }
            }
        }
    }

    public void update() {
        synchronized (this) {
            m_shouldUpdate = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    private class RandomThread implements Runnable {

        private final SharedPreferences m_prefs = PreferenceManager.getDefaultSharedPreferences(RandomizationService.this);
        private final Random m_random = new Random();
        private boolean m_fieldConRandom;
        private boolean m_robotConRandom;
        private boolean m_robotValRandom;
        private String m_fieldConKey;
        private String m_robotConKey;
        private String m_robotValKey;

        public RandomThread() {
            m_fieldConKey = RandomizationService.this.getString(R.string.randomize_field_con_key);
            m_robotConKey = RandomizationService.this.getString(R.string.randomize_robot_con_key);
            m_robotValKey = RandomizationService.this.getString(R.string.randomize_robot_vals_key);
            update();
        }

        private void update() {
            m_fieldConRandom = m_prefs.getBoolean(m_fieldConKey, false);
            m_robotConRandom = m_prefs.getBoolean(m_robotConKey, false);
            m_robotValRandom = m_prefs.getBoolean(m_robotValKey, false);
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                if (m_shouldUpdate) {
                    update();
                    synchronized (RandomizationService.this) {
                        m_shouldUpdate = false;
                    }
                }

                if (m_fieldConRandom && m_isBound) {
                    int newState = m_random.nextInt(ConnectionState.values().length);
                    m_stateObservable.setConnectionState(ConnectionState.values()[newState]);
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public class RandomizationBinder extends Binder {
        public RandomizationService getService() {
            return RandomizationService.this;
        }
    }
}
