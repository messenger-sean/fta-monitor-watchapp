package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.UpdateType;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import microsoft.aspnet.signalr.client.ConnectionState;
import org.joda.time.DateTime;

import java.util.*;

/**
 * This service is responsible for sending data to the pebble
 */
public class PebbleCommunicationService extends Service {

    // The app UID
    private static final UUID PEBBLE_UUID = UUID.fromString("5b742e45-2918-4f69-a510-7c0457d9df16");

    private static final String LIFECYCLE_INTENT_EXTRA = "lifecycle_intent_extra";
    private static final int START = 0;
    private static final int STOP = 1;

    private static final String VIBE_INTERVAL_INTENT_EXTRA = "vibe_interval_intent_extra";
    private static final String OUT_OF_MATCH_INTENT_EXTRA = "out_of_match_intent_extra";
    private static final String BANDWIDTH_INTENT_EXTRA = "bandwidth_intent_extra";

    // Constants for the pebble dictionary keys
    private static final int RED1 = 1;
    private static final int RED2 = 2;
    private static final int RED3 = 3;
    private static final int BLUE1 = 4;
    private static final int BLUE2 = 5;
    private static final int BLUE3 = 6;

    /**
     * Checks the current status of the pebble service, and starts/stops as necessary
     */
    public static void start() {
        Context ctx = FTAMonitorApplication.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Intent intent = new Intent(ctx, PebbleCommunicationService.class);
        String pebbleEnabledKey = ctx.getString(R.string.pebble_key);
        boolean pebbleEnabled = prefs.getBoolean(pebbleEnabledKey, false);

        // If the pebble service is enabled, get the parameters and enable it. Otherwise, disable the service
        if (pebbleEnabled) {
            // Get the arguments
            String vibeIntervalKey = ctx.getString(R.string.pebble_vibe_interval_key);
            String outOfMatchKey = ctx.getString(R.string.pebble_notify_times_key);
            String bandwidthKey = ctx.getString(R.string.bandwidth_key);
            int vibeInterval = Integer.parseInt(prefs.getString(vibeIntervalKey, "10"));
            boolean outOfMatch = prefs.getBoolean(outOfMatchKey, true);
            float bandwidth = Float.parseFloat(prefs.getString(bandwidthKey, "7.0"));

            // Put all of the arguments
            intent.putExtra(LIFECYCLE_INTENT_EXTRA, START);
            intent.putExtra(VIBE_INTERVAL_INTENT_EXTRA, vibeInterval);
            intent.putExtra(OUT_OF_MATCH_INTENT_EXTRA, outOfMatch);
            intent.putExtra(BANDWIDTH_INTENT_EXTRA, bandwidth);
        } else {
            intent.putExtra(LIFECYCLE_INTENT_EXTRA, STOP);
        }

        ctx.startService(intent);
    }

    /**
     * Private enum that holds the different states the service can be in
     */
    private enum State {
        DISCONNECTED, NOT_SENDING, SENDING, RETRY
    }

    // Class lock and notifier for accessing the queue and making changes to the state, as well as letting
    // the communications thread know to send new data
    private final Object m_lock = new Object();
    private final Deque<PebbleDictionary> m_queue = new ArrayDeque<>();
    private State m_curState = State.DISCONNECTED;
    private Thread m_sendThread;

    // The receiver for messages from the pebble
    private final UpdateReceiver m_dataReceiver = new UpdateReceiver();

    // The registered team problem observers
    private Collection<TeamProblemObserver> m_observers = new ArrayList<>();

    // The service connection to the field status service.
    private ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            m_fieldConnectionService = ((FieldConnectionService.FCSBinder) service).getService();
            m_isBound = true;
            m_fieldConnectionService.registerObserver(m_connectionObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_fieldConnectionService.deregisterObserver(m_connectionObserver);
            m_isBound = false;
            m_fieldConnectionService = null;
        }
    };
    private boolean m_isBound = false;
    private FieldConnectionService m_fieldConnectionService;
    private final ConnectionObserver m_connectionObserver = new ConnectionObserver();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If the intent is null, then stop the service
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // If the intent does not have the extra, assume an error, and stop the service
        int lifecycle = intent.getIntExtra(LIFECYCLE_INTENT_EXTRA, STOP);
        if (lifecycle == STOP) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Get the parameters for the observers
        int vibeInterval = intent.getIntExtra(VIBE_INTERVAL_INTENT_EXTRA, 10);
        boolean outOfMatch = intent.getBooleanExtra(OUT_OF_MATCH_INTENT_EXTRA, true);
        float bandwidth = intent.getFloatExtra(BANDWIDTH_INTENT_EXTRA, 7.0f);

        // Unregister any existing observers
        unregisterObservers();

        // First, check to see if we're connected.

        // Register all of the observers and set up the send thread
        setup(vibeInterval, outOfMatch, bandwidth);

        return START_STICKY;
    }

    private void setup(int vibeInterval, boolean outOfMatch, float bandwidth) {
        FieldStatus fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue1(), vibeInterval, outOfMatch, bandwidth, BLUE1));
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue2(), vibeInterval, outOfMatch, bandwidth, BLUE2));
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue3(), vibeInterval, outOfMatch, bandwidth, BLUE3));
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed1(), vibeInterval, outOfMatch, bandwidth, RED1));
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed2(), vibeInterval, outOfMatch, bandwidth, RED2));
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed3(), vibeInterval, outOfMatch, bandwidth, RED3));

        m_sendThread = new Thread(new PebbleSendThread());
        m_sendThread.setName("Pebble Send Thread");
        m_sendThread.start();

        PebbleKit.registerReceivedDataHandler(this, m_dataReceiver);

        // Start the watchapp
        PebbleKit.startAppOnPebble(this, PEBBLE_UUID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This service does not support binding
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterObservers();
        if (m_sendThread != null) {
            m_sendThread.interrupt();
        }
        unregisterReceiver(m_dataReceiver);
        if (m_isBound) {
            unbindService(m_connection);
        }
        super.onDestroy();
    }

    private boolean checkConnection() {
        boolean pebbleConnection = PebbleKit.isWatchConnected(this);
        if (!pebbleConnection) {
            synchronized (m_lock) {
                m_curState = State.DISCONNECTED;
            }
            PebbleKit.registerPebbleConnectedReceiver(this, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    unregisterReceiver(this);
                    start();
                }
            });

            // When we are disconnected, unregister all observers
            unregisterObservers();
        } else {
            // Register a receiver for when the pebble is disconnected. If it's disconnected, then we remove the
            // receiver, update the current state to disconnected, and register a connection receiver
            PebbleKit.registerPebbleDisconnectedReceiver(this, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    unregisterReceiver(this);
                    synchronized (m_lock) {
                        m_curState = State.DISCONNECTED;
                    }
                    checkConnection();
                }
            });
        }

        return pebbleConnection;
    }

    /**
     * Unregisters any existing observers and clears the array
     */
    private void unregisterObservers() {
        for (TeamProblemObserver observer : m_observers) {
            observer.unregister();
        }
        m_observers.clear();
    }

    private final class PebbleSendThread implements Runnable {

        // Retains the last sent packet to the pebble, for resending if necessary. Access should be blocked by
        // the lock
        private PebbleDictionary m_lastPacket;
        private int m_transId = 0;
        private final AckReceiver m_ack = new AckReceiver();
        private final NackReceiver m_nack = new NackReceiver();
        private boolean m_receivedNack = false;

        @Override
        public void run() {
            // Register the ACK receiver
            PebbleKit.registerReceivedAckHandler(PebbleCommunicationService.this, m_ack);
            PebbleKit.registerReceivedNackHandler(PebbleCommunicationService.this, m_nack);

            synchronized (m_lock) {
                m_curState = State.NOT_SENDING;
            }

            while (true) {
                synchronized (m_lock) {
                    // First, wait for the queue to have an element in it and for us to be not sending
                    while (m_queue.isEmpty() || m_curState != State.NOT_SENDING) {
                        try {
                            m_lock.wait();
                        } catch (InterruptedException e) {
                            Log.d(PebbleCommunicationService.class.getName(),
                                    "Error while waiting for message to send to pebble",
                                    e);
                            unregisterReceiver(m_ack);
                            unregisterReceiver(m_nack);
                            return;
                        }
                    }

                    // Increment the transaction id, keep in a 255 ring
                    incrementTransId();

                    m_curState = State.SENDING;

                    // Next, remove the first element of the queue and send it
                    m_lastPacket = m_queue.pollFirst();
                    PebbleKit.sendDataToPebbleWithTransactionId(PebbleCommunicationService.this,
                            PEBBLE_UUID,
                            m_lastPacket,
                            m_transId);
                }
            }
        }

        private void sendNext() {
            synchronized (m_lock) {
                m_lastPacket = null;
                m_receivedNack = false;
                m_curState = State.NOT_SENDING;
                m_lock.notifyAll();
            }
        }

        private void incrementTransId() {
            synchronized (m_lock) {
                m_transId = (++m_transId) % 255;
            }
        }

        private class AckReceiver extends PebbleKit.PebbleAckReceiver {
            protected AckReceiver() {
                super(PEBBLE_UUID);
            }

            @Override
            public void receiveAck(Context context, int transId) {
                // In the case of a received ack, then update the current status and notify the main thread
                synchronized (m_lock) {
                    if (m_transId != transId || m_lastPacket == null) {
                        Log.w(AckReceiver.class.getName(), "Received invalid ack");
                        return;
                    }

                    sendNext();
                }
            }
        }

        private class NackReceiver extends PebbleKit.PebbleNackReceiver {

            public NackReceiver() {
                super(PEBBLE_UUID);
            }

            @Override
            public void receiveNack(Context context, int transId) {
                // In the case of a nack, first check communication. If we are still connected to the pebble, then check
                // to see if this is the second nack. If it is, then the app is likely closed, so silently discard
                // the packet and continue
                if (m_transId != transId || m_lastPacket == null) {
                    Log.w(NackReceiver.class.getName(), "Received invalid nack");
                    return;
                }

                if (!checkConnection()) {
                    m_sendThread.interrupt();
                } else if (m_receivedNack) {
                    // When we've already received a nack for this, set the status to disconnected and then clear
                    // all incoming messages. We won't attempt another send until the watch is reopened
                    synchronized (m_lock) {
                        m_curState = State.DISCONNECTED;
                        m_queue.clear();
                    }
                } else {
                    m_curState = State.RETRY;
                    m_receivedNack = true;
                    incrementTransId();
                    PebbleKit.sendDataToPebbleWithTransactionId(PebbleCommunicationService.this,
                            PEBBLE_UUID,
                            m_lastPacket,
                            m_transId);
                }
            }
        }
    }

    private final class TeamProblemObserver implements Observer<UpdateType> {
        // Constants for the team types
        private static final byte VIBE = 7;

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

        // This is not a valid state, but it's the state I start off in to make sure to send the first status
        private static final byte INVALID = (byte) 255;

        private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        private final TeamStatus m_teamStatus;
        private final int m_vibeInterval;
        private final boolean m_outOfMatchNotify;
        private final float m_maxBandwidth;
        private final int m_pebbleDictKey;
        private byte m_curStatus = INVALID;
        private DateTime m_lastVibeTime;

        private TeamProblemObserver(TeamStatus teamStatus, int vibeInterval, boolean outOfMatchNotify, float maxBandwidth, int pebbleDictKey) {
            m_teamStatus = teamStatus;
            m_vibeInterval = vibeInterval;
            m_outOfMatchNotify = outOfMatchNotify;
            m_maxBandwidth = maxBandwidth;
            m_pebbleDictKey = pebbleDictKey;
            m_lastVibeTime = DateTime.now().minusSeconds(m_vibeInterval);
            m_teamStatus.registerObserver(this);
        }

        @Override
        public void update(UpdateType updateType) {
            updateTeamStatus(false);
        }

        /**
         * Updates the status of the current team. Normally, this will optimize by not updating the status if the
         * status is the same as the previous status. If force is true, however, then the status will be updated
         * regardless of whether or not the new status is the same as the last status
         *
         * @param force Whether to update if the status is the same or not
         */
        private void updateTeamStatus(boolean force) {
            byte status;
            if (m_teamStatus.isEstop()) {
                status = ESTOP;
            } else if (m_teamStatus.isBypassed()) {
                status = BYP;
            } else if (!m_teamStatus.isDsEth()) {
                status = ETH;
            } else if (!m_teamStatus.isDs()) {
                status = DS;
            } else if (!m_teamStatus.isRadio()) {
                status = RADIO;
            } else if (!m_teamStatus.isRobot()) {
                status = RIO;
            } else if (!m_teamStatus.isCode()) {
                status = CODE;
            } else if (m_teamStatus.getDataRate() > m_maxBandwidth) {
                status = BWU;
            } else {
                status = GOOD;
            }

            // If we're not forcing an update the and the current status is unchanged, then don't update
            if (!force && status == m_curStatus) {
                return;
            }

            PebbleDictionary dict = new PebbleDictionary();
            dict.addUint8(m_pebbleDictKey, status);

            // If we don't do out of match notify, and the current status is not either teleop or autonomous, then
            // don't include the vibrate
            boolean shouldVibe = m_outOfMatchNotify ||
                    (m_fieldStatus.getMatchStatus() == MatchStatus.TELEOP
                            || m_fieldStatus.getMatchStatus() == MatchStatus.AUTO);

            // Check to see if this update should vibrate
            if (m_lastVibeTime.plusSeconds(m_vibeInterval).isBeforeNow() && shouldVibe) {
                m_lastVibeTime = DateTime.now();
                dict.addUint8(VIBE, (byte) 1);
            }

            synchronized (m_lock) {
                // If we're not disconnected, then add to the queue. Otherwise, silently discard
                if (m_curState != State.DISCONNECTED) {
                    m_queue.addLast(dict);
                    m_lock.notifyAll();
                }
            }
        }

        private void unregister() {
            m_teamStatus.unregisterObserver(this);
        }
    }

    /**
     * Watches the field connection service and updates all statuses when the field connects
     */
    private final class ConnectionObserver implements Observer<ConnectionState> {
        @Override
        public void update(ConnectionState updateType) {
            if (updateType == ConnectionState.Connected) {
                for (TeamProblemObserver observer : m_observers) {
                    observer.updateTeamStatus(true);
                }
            }
        }
    }

    private final class UpdateReceiver extends PebbleKit.PebbleDataReceiver {

        private static final byte UPDATE = 8;

        public UpdateReceiver() {
            super(PEBBLE_UUID);
        }

        @Override
        public void receiveData(Context context, int transId, PebbleDictionary pebbleDictionary) {
            PebbleKit.sendAckToPebble(context, transId);
            if (pebbleDictionary.contains(UPDATE)) {
                synchronized (m_lock) {
                    // If we previously thought the app was closed, then set the state to not sending
                    if (m_curState == State.DISCONNECTED) {
                        m_curState = State.NOT_SENDING;
                    }
                }

                // Force all team statuses to be updated
                for (TeamProblemObserver observer : m_observers) {
                    observer.updateTeamStatus(true);
                }
            }
        }
    }
}
