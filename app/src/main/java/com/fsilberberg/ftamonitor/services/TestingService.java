package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

import java.util.Random;

public class TestingService extends Service {

    public static final String STOP_INTENT = "stop_intent";
    private final TestingBinder m_binder = new TestingBinder();
    private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_red1 = m_field.getRed1();
    private final TeamStatus m_red2 = m_field.getRed2();
    private final TeamStatus m_red3 = m_field.getRed3();
    private final TeamStatus m_blue1 = m_field.getBlue1();
    private final TeamStatus m_blue2 = m_field.getBlue2();
    private final TeamStatus m_blue3 = m_field.getBlue3();
    private Thread m_fieldThread;
    private Thread m_red1Thread;
    private Thread m_red2Thread;
    private Thread m_red3Thread;
    private Thread m_blue1Thread;
    private Thread m_blue2Thread;
    private Thread m_blue3Thread;

    public TestingService() {
    }

    public void setFieldStatusRandomize(boolean randomize) {
        if (randomize) {
            if (m_fieldThread != null) {
                m_fieldThread.interrupt();
                m_fieldThread = null;
            }
            m_fieldThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Random rand = new Random();
                    while (true) {
                        int ord = rand.nextInt() % MatchStatus.values().length;
                        setFieldStatus(MatchStatus.values()[ord], String.format("%d", ord));
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            });
        } else {
            m_fieldThread.interrupt();
            m_fieldThread = null;
        }
    }

    /**
     * Sets the field status to the given status
     */
    public void setFieldStatus(MatchStatus status, String matchNum) {
        m_field.setMatchStatus(status);
        m_field.setMatchNumber(matchNum);
    }

    public void setTeamStatus(Alliance alliance, int team, int num, float battery) {
        TeamStatus status = selectFromTeamAlliance(alliance, team, m_red1, m_red2, m_red3, m_blue1, m_blue2, m_blue3);
        status.setTeamNumber(num);
        status.setBattery(battery);
    }

    private <T> T selectFromTeamAlliance(Alliance alliance, int team, T r1, T r2, T r3, T b1, T b2, T b3) {
        T t1 = null, t2 = null, t3 = null;
        switch (alliance) {
            case RED:
                t1 = r1;
                t2 = r2;
                t3 = r3;
                break;
            case BLUE:
                t1 = b1;
                t2 = b2;
                t3 = b3;
                break;
        }

        switch (team) {
            case 1:
                return t1;
            case 2:
                return t2;
            case 3:
            default:
                return t3;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(STOP_INTENT)) {
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    public class TestingBinder extends Binder {
        public TestingService getService() {
            return TestingService.this;
        }
    }
}
