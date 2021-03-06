package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

import java.math.BigDecimal;
import java.util.Random;

import microsoft.aspnet.signalr.client.ConnectionState;

/**
 * Randomizes the values seen on the field monitor every second for testing
 */
public class RandomizationService extends Service {
    private final Object m_lock = new Object();
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
    private volatile boolean m_isBound = false;
    private volatile boolean m_isStarted = false;
    private volatile boolean m_shouldUpdate = false;

    public RandomizationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (m_lock) {
            m_shouldUpdate = true;
            // If we've already started, then we're done, just return
            if (m_isStarted) {
                return START_NOT_STICKY;
            } else {
                m_isStarted = true;
            }
        }

        if (!m_isBound) {
            bindService(new Intent(this, FieldConnectionService.class), m_connection, Context.BIND_AUTO_CREATE);
        }

        m_randomThread = new Thread(new RandomThread());
        m_randomThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        synchronized (m_lock) {
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
        synchronized (m_lock) {
            return m_isStarted;
        }
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
            synchronized (m_lock) {
                if (!m_isStarted) {
                    startService(new Intent(context, RandomizationService.class));
                }
            }
        } else {
            synchronized (m_lock) {
                if (m_isStarted) {
                    m_isStarted = false;
                    stopService(new Intent(context, RandomizationService.class));
                }
            }
        }
    }

    public void update() {
        synchronized (m_lock) {
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
        private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
        private final TeamStatus[] m_robots = new TeamStatus[]{
                m_field.getBlue1(),
                m_field.getBlue2(),
                m_field.getBlue3(),
                m_field.getRed1(),
                m_field.getRed2(),
                m_field.getRed3()
        };
        private boolean m_fieldConRandom;
        private boolean m_matchStatusRandom;
        private boolean m_robotConRandom;
        private boolean m_robotValRandom;
        private String m_fieldConKey;
        private String m_matchStatusKey;
        private String m_robotConKey;
        private String m_robotValKey;

        public RandomThread() {
            m_fieldConKey = RandomizationService.this.getString(R.string.randomize_field_con_key);
            m_matchStatusKey = getString(R.string.randomize_match_status_key);
            m_robotConKey = RandomizationService.this.getString(R.string.randomize_robot_con_key);
            m_robotValKey = RandomizationService.this.getString(R.string.randomize_robot_vals_key);
            update();
        }

        private void update() {
            m_fieldConRandom = m_prefs.getBoolean(m_fieldConKey, false);
            m_matchStatusRandom = m_prefs.getBoolean(m_matchStatusKey, false);
            m_robotConRandom = m_prefs.getBoolean(m_robotConKey, false);
            m_robotValRandom = m_prefs.getBoolean(m_robotValKey, false);
        }

        @Override
        public void run() {
            while (!Thread.interrupted() && m_isStarted) {
                boolean shouldUpdate;
                synchronized (m_lock) {
                    shouldUpdate = m_shouldUpdate;
                }
                if (shouldUpdate) {
                    update();
                    synchronized (m_lock) {
                        m_shouldUpdate = false;
                    }
                }

                if (m_fieldConRandom && m_isBound) {
                    int newState = m_random.nextInt(ConnectionState.values().length);
                    m_stateObservable.setConnectionState(ConnectionState.values()[newState]);
                }

                if (m_matchStatusRandom) {
                    int newState = m_random.nextInt(MatchStatus.values().length);
                    m_field.setMatchNumber(Integer.toString(m_random.nextInt(999)));
                    m_field.setMatchStatus(MatchStatus.values()[newState]);
                    m_field.setPlayNumber(m_random.nextInt(9));
                }

                if (m_robotConRandom) {
                    for (TeamStatus robot : m_robots) {
                        int newState = m_random.nextInt(RobotEnableStatus.values().length);
                        RobotEnableStatus.values()[newState].setRobot(robot);
                    }
                }

                if (m_robotValRandom) {
                    for (TeamStatus robot : m_robots) {
                        updateRobotValues(robot);
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        private void updateRobotValues(TeamStatus robot) {
            // Team number goes between 1 and 9999
            int team = m_random.nextInt(9998) + 1;
            robot.setTeamNumber(team);

            // Battery voltage goes between 6.0V and 13.5V
            float voltage = generateRoundedFloat(6.0f, 13.5f, 2);
            robot.setBattery(voltage);

            // Bandwidth goes between 0.125 Mbps to 22.0 Mbps
            float bw = generateRoundedFloat(.125f, 22.0f, 3);
            robot.setDataRate(bw);

            // Round trip times go between 1 ms to 999 ms
            int roundTrip = m_random.nextInt(998) + 1;
            robot.setRoundTrip(roundTrip);

            // Missed packets go between 0 and 999
            int mp = m_random.nextInt(999);
            robot.setDroppedPackets(mp);

            // Signal quality goes from 0% to 100%
            int sq = m_random.nextInt(100);
            robot.setSignalQuality(sq);

            // Signal strength goes from -100 db to -5db
            int ss = (m_random.nextInt(95) + 5) * -1;
            robot.setSignalStrength(ss);
        }

        private float generateRoundedFloat(float min, float max, int places) {
            float unrounded = (m_random.nextFloat() * (max - min)) + min;
            BigDecimal bd = new BigDecimal(Float.toString(unrounded));
            bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
            return bd.floatValue();
        }
    }

    /**
     * Represents the different states a robot can be in. Used for random state generation
     */
    private enum RobotEnableStatus {
        ETH(false, false, false, false, false, false, false),
        DS(true, false, false, false, false, false, false),
        RADIO(true, true, false, false, false, false, false),
        RIO(true, true, true, false, false, false, false),
        CODE(true, true, true, true, false, false, false),
        GOOD(true, true, true, true, true, false, false),
        BYPASS(true, true, true, true, true, true, false),
        ESTOP(true, true, true, true, true, true, true);

        private final boolean eth;
        private final boolean ds;
        private final boolean radio;
        private final boolean rio;
        private final boolean code;
        private final boolean bypass;
        private final boolean estop;

        RobotEnableStatus(boolean eth, boolean ds, boolean radio, boolean rio, boolean code, boolean bypass, boolean estop) {
            this.eth = eth;
            this.ds = ds;
            this.radio = radio;
            this.rio = rio;
            this.code = code;
            this.bypass = bypass;
            this.estop = estop;
        }

        public void setRobot(TeamStatus robot) {
            robot.setDsEth(eth);
            robot.setDs(ds);
            robot.setRadio(radio);
            robot.setRio(rio);
            robot.setCode(code);
            robot.setBypassed(bypass);
            robot.setEstop(estop);
        }
    }

    public class RandomizationBinder extends Binder {
        public RandomizationService getService() {
            return RandomizationService.this;
        }
    }
}
