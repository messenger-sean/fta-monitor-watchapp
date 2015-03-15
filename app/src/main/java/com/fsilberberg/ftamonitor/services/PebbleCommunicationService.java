package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.UpdateType;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Communicates with the pebble watchapp
 */
public class PebbleCommunicationService implements ForegroundService {

    // Constants for the team types
    private static final byte VIBE = 7;
    private static final byte UPDATE = 8;

    // Constants for the different statuses
    private static final byte ETH = 0;
    private static final byte DS = 1;
    private static final byte RADIO = 2;
    private static final byte RIO = 3;
    private static final byte CODE = 4;
    private static final byte ESTOP = 5;
    private static final byte GOOD = 6;
    private static final byte BWU = 7;
    private static final byte BYP = 8;

    // The app UID
    private static final UUID PEBBLE_UUID = UUID.fromString("5b742e45-2918-4f69-a510-7c0457d9df16");

    // References to all teams and the field status
    private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_blue1 = m_field.getBlue1();
    private final TeamStatus m_blue2 = m_field.getBlue2();
    private final TeamStatus m_blue3 = m_field.getBlue3();
    private final TeamStatus m_red1 = m_field.getRed1();
    private final TeamStatus m_red2 = m_field.getRed2();
    private final TeamStatus m_red3 = m_field.getRed3();
    private boolean m_appOpened = false;
    private int m_timeInterval = 10;
    private boolean m_outOfMatch = true;
    private float m_maxBandwidth;

    private Context m_context;
    private final Collection<PebbleTeamUpdater> m_updaters = new ArrayList<>();

    // Pebble message components for ensuring reliable delivery
    private final Object m_sendLock = new Object();
    private Thread m_resendThread = null;

    public void startService(Context context) {
        m_context = context;
        checkAndOpen();

        // Get the minimum time interval and whether or not to vibrate out of a match
        m_timeInterval = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(m_context).getString(
                        m_context.getResources().getString(R.string.pebble_vibe_interval_key), "10"
                )
        );
        m_outOfMatch = PreferenceManager.getDefaultSharedPreferences(m_context).getBoolean(
                m_context.getResources().getString(R.string.pebble_notify_times_key), true
        );
        m_maxBandwidth = Float.parseFloat(
                PreferenceManager.getDefaultSharedPreferences(m_context).getString(
                        m_context.getResources().getString(R.string.bandwidth_key), "7.0"
                )
        );

        PebbleKit.registerReceivedNackHandler(m_context, new PebbleKit.PebbleNackReceiver(PEBBLE_UUID) {
            @Override
            public void receiveNack(Context context, int i) {
                // On failure, resend the message
                synchronized (m_sendLock) {
                    if (m_resendThread == null) {
                        m_resendThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(100);
                                    for (PebbleTeamUpdater updater : m_updaters) {
                                        updater.update(true);
                                    }
                                    synchronized (m_sendLock) {
                                        m_resendThread = null;
                                    }
                                } catch (InterruptedException e) {
                                    Log.w(PebbleCommunicationService.class.getName(),
                                            "Interrupted while waiting to retransmit statuses",
                                            e);
                                }

                            }
                        });
                    }
                }
            }
        });

        PebbleKit.registerReceivedDataHandler(m_context, new UpdateReceiver());

        m_updaters.add(new PebbleTeamUpdater(1, Alliance.RED, m_red1));
        m_updaters.add(new PebbleTeamUpdater(2, Alliance.RED, m_red2));
        m_updaters.add(new PebbleTeamUpdater(3, Alliance.RED, m_red3));
        m_updaters.add(new PebbleTeamUpdater(1, Alliance.BLUE, m_blue1));
        m_updaters.add(new PebbleTeamUpdater(2, Alliance.BLUE, m_blue2));
        m_updaters.add(new PebbleTeamUpdater(3, Alliance.BLUE, m_blue3));
        for (PebbleTeamUpdater updater : m_updaters) {
            updater.update(true);
        }
    }

    @Override
    public void stopService() {
        for (PebbleTeamUpdater updater : m_updaters) {
            updater.deregister();
        }
    }

    /**
     * Sends update to the watch.
     *
     * @param message The message to send to the pebble
     */
    private void sendMessage(PebbleDictionary message) {
        // Send a message to the pebble only if we're either in a match or should always notify, and make sure the
        // pebble is connected and that we're all set
        if ((m_outOfMatch || isMatchPlaying()) && checkAndOpen()) {
            synchronized (m_sendLock) {
                PebbleKit.sendDataToPebble(m_context, PEBBLE_UUID, message);
            }
        }
    }

    private boolean isMatchPlaying() {
        MatchStatus status = m_field.getMatchStatus();
        switch (status) {
            case AUTO:
            case TELEOP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks to make sure that there is a Pebble connected and that the watchapp is open
     *
     * @return True if the app is ready to go, false if sending updates should be abandoned
     */
    private boolean checkAndOpen() {
        if (PebbleKit.isWatchConnected(m_context)) {
            // Only open the app once, so that we don't keep opening it if the user went to another app on their own
            if (!m_appOpened) {
                PebbleKit.startAppOnPebble(m_context, PEBBLE_UUID);
                m_appOpened = true;
            }
            return true;
        } else {
            return false;
        }
    }

    private class PebbleTeamUpdater implements Observer<UpdateType> {

        private final int m_station;
        private final TeamStatus m_team;
        private final Alliance m_alliance;
        private DateTime m_lastVibeTime = DateTime.now().minusSeconds(m_timeInterval);
        private byte m_curStatus = ETH;

        private PebbleTeamUpdater(int station, Alliance alliance, TeamStatus team) {
            m_team = team;
            m_alliance = alliance;
            m_station = station;
            m_team.registerObserver(this);
        }

        public void deregister() {
            m_team.deregisterObserver(this);
        }

        private int getKey() {
            return m_station + (m_alliance == Alliance.RED ? 0 : 3);
        }

        @Override
        public void update(UpdateType updateType) {
            if (updateType == UpdateType.TEAM) {
                update(false);
            }
        }

        public void update(boolean force) {
            // If this is a team update
            byte status = 0;
            if (m_team.isEstop()) {
                status = ESTOP;
            } else if (m_team.isBypassed()) {
                status = BYP;
            } else if (!m_team.isDsEth()) {
                status = ETH;
            } else if (!m_team.isDs()) {
                status = DS;
            } else if (!m_team.isRadio()) {
                status = RADIO;
            } else if (!m_team.isRobot()) {
                status = RIO;
            } else if (!m_team.isCode()) {
                status = CODE;
            } else if (m_team.getDataRate() > m_maxBandwidth) {
                status = BWU;
            } else {
                status = GOOD;
            }

            // If we're not forcing an update the and the current status is unchanged, then don't update
            if (!force && status == m_curStatus) {
                return;
            }

            PebbleDictionary dict = new PebbleDictionary();
            dict.addUint8(getKey(), status);

            // Check to see if this update should vibrate
            if (m_lastVibeTime.plusSeconds(m_timeInterval).isBeforeNow()) {
                m_lastVibeTime = DateTime.now();
                dict.addUint8(VIBE, (byte) 1);
            }

            sendMessage(dict);
            m_curStatus = status;
        }
    }

    /**
     * Receives messages from the pebble to update the current state, such as when the app is first opened
     */
    private class UpdateReceiver extends PebbleKit.PebbleDataReceiver {

        protected UpdateReceiver() {
            super(PEBBLE_UUID);
        }

        @Override
        public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
            if (pebbleDictionary.contains(UPDATE)) {
                for (PebbleTeamUpdater updater : m_updaters) {
                    updater.update(true);
                }
            }
        }
    }
}
