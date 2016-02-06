package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.Observable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import microsoft.aspnet.signalr.client.ConnectionState;

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
    private static final String BANDWIDTH_NOTIFY_INTENT_EXTRA = "bandwidth_notify_intent_extra";
    private static final String BANDWIDTH_INTENT_EXTRA = "bandwidth_intent_extra";
    private static final String LOW_BATTERY_NOTIFY_INTENT_EXTRA = "low_battery_notify_intent_extra";
    private static final String LOW_BATTERY_INTENT_EXTRA = "low_battery_intent_extra";

    // Constants for the pebble dictionary keys. Not all are used here, but they are included
    // for documentation's sake
    private static final int RED1 = 1;
    private static final int RED2 = 2;
    private static final int RED3 = 3;
    private static final int BLUE1 = 4;
    private static final int BLUE2 = 5;
    private static final int BLUE3 = 6;
    private static final int VIBE = 7;
    @SuppressWarnings("unused")
    private static final int UPDATE = 8;
    @SuppressWarnings("unused")
    private static final int RED1_NUM = 9;
    @SuppressWarnings("unused")
    private static final int RED2_NUM = 10;
    @SuppressWarnings("unused")
    private static final int RED3_NUM = 11;
    @SuppressWarnings("unused")
    private static final int BLUE1_NUM = 12;
    @SuppressWarnings("unused")
    private static final int BLUE2_NUM = 13;
    @SuppressWarnings("unused")
    private static final int BLUE3_NUM = 14;
    @SuppressWarnings("unused")
    private static final int RED1_BATT = 15;
    @SuppressWarnings("unused")
    private static final int RED2_BATT = 16;
    @SuppressWarnings("unused")
    private static final int RED3_BATT = 17;
    @SuppressWarnings("unused")
    private static final int BLUE1_BATT = 18;
    @SuppressWarnings("unused")
    private static final int BLUE2_BATT = 19;
    @SuppressWarnings("unused")
    private static final int BLUE3_BATT = 20;
    @SuppressWarnings("unused")
    private static final int MATCH_STATE = 21;

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
            String bandwidthNotifyKey = ctx.getString(R.string.pebble_bandwidth_notify_key);
            String bandwidthKey = ctx.getString(R.string.bandwidth_key);
            String lowBatteryNotifyKey = ctx.getString(R.string.pebble_low_battery_notify_key);
            String lowBatteryKey = ctx.getString(R.string.low_battery_key);
            int vibeInterval = Integer.parseInt(prefs.getString(vibeIntervalKey, "10"));
            boolean outOfMatch = prefs.getBoolean(outOfMatchKey, true);
            boolean bandwidthNotify = prefs.getBoolean(bandwidthNotifyKey, true);
            float bandwidth = Float.parseFloat(prefs.getString(bandwidthKey, "7.0"));
            boolean lowBatteryNotify = prefs.getBoolean(lowBatteryNotifyKey, true);
            float lowBattery = Float.parseFloat(prefs.getString(lowBatteryKey, "6.5"));

            // Put all of the arguments
            intent.putExtra(LIFECYCLE_INTENT_EXTRA, START);
            intent.putExtra(VIBE_INTERVAL_INTENT_EXTRA, vibeInterval);
            intent.putExtra(OUT_OF_MATCH_INTENT_EXTRA, outOfMatch);
            intent.putExtra(BANDWIDTH_NOTIFY_INTENT_EXTRA, bandwidthNotify);
            intent.putExtra(BANDWIDTH_INTENT_EXTRA, bandwidth);
            intent.putExtra(LOW_BATTERY_NOTIFY_INTENT_EXTRA, lowBatteryNotify);
            intent.putExtra(LOW_BATTERY_INTENT_EXTRA, lowBattery);
        } else {
            intent.putExtra(LIFECYCLE_INTENT_EXTRA, STOP);
        }

        ctx.startService(intent);
    }

    // Class lock and notifier for accessing the queue and making changes to the state, as well as
    // letting the communications thread know to send new data.
    // Note that while this is a rwlock, the reader/writer role is reversed. In this case, the "readers"
    // are the update listeners for each team, which can all modify the status array concurrently,
    // and the send thread is the writer, which must have exclusive access to the entire array
    private final ReadWriteLock m_rlock = new ReentrantReadWriteLock(true);
    private final Semaphore m_sendSem = new Semaphore(1);
    private final int[] m_statusArr = new int[6];
    private final int[] m_numberArr = new int[6];
    private final float[] m_batteryArr = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
            Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
    private MatchStatus m_matchStatus = MatchStatus.NOT_READY;
    private boolean m_vibrate = false;

    private Thread m_sendThread;
    private boolean m_updateOutOfMatch = true;
    private boolean m_inMatch = false;

    // The receiver for messages from the pebble
    private final UpdateReceiver m_dataReceiver = new UpdateReceiver();
    private boolean m_dataReceiverRegistered = false;

    // The registered team problem observers and field status observable
    private Collection<DeletableObserver> m_observers = new ArrayList<>();

    // The service connection to the field status service.
    private ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            m_fieldConnectionService = ((FieldConnectionService.FCSBinder) service).getService();
            m_isBound = true;
            m_fieldConnectionService.getStatusObservable().addOnPropertyChangedCallback(m_connectionObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_fieldConnectionService.getStatusObservable().addOnPropertyChangedCallback(m_connectionObserver);
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
        boolean bandwidthNotify = intent.getBooleanExtra(BANDWIDTH_NOTIFY_INTENT_EXTRA, true);
        float bandwidth = intent.getFloatExtra(BANDWIDTH_INTENT_EXTRA, 7.0f);
        boolean lowBatteryNotify = intent.getBooleanExtra(LOW_BATTERY_NOTIFY_INTENT_EXTRA, true);
        float lowBattery = intent.getFloatExtra(LOW_BATTERY_INTENT_EXTRA, 6.5f);

        // Unregister any existing observers
        unregisterObservers();

        // Register all of the observers and set up the send thread
        setup(vibeInterval, outOfMatch, bandwidthNotify, bandwidth, lowBatteryNotify, lowBattery);

        return START_STICKY;
    }

    private void setup(int vibeInterval, boolean outOfMatch, boolean bandwidthNotify, float bandwidth, boolean lowBatteryNotify, float lowBattery) {
        FieldStatus fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue1(), bandwidthNotify, bandwidth,
                lowBatteryNotify, lowBattery, BLUE1, vibeInterval).init());
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue2(), bandwidthNotify, bandwidth,
                lowBatteryNotify, lowBattery, BLUE2, vibeInterval).init());
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue3(), bandwidthNotify, bandwidth,
                lowBatteryNotify, lowBattery, BLUE3, vibeInterval).init());
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed1(), bandwidthNotify, bandwidth,
                lowBatteryNotify, lowBattery, RED1, vibeInterval).init());
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed2(), bandwidthNotify, bandwidth,
                lowBatteryNotify, lowBattery, RED2, vibeInterval).init());
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed3(), bandwidthNotify, bandwidth,
                lowBatteryNotify, lowBattery, RED3, vibeInterval).init());
        m_observers.add(new FieldUpdateObserver().init());

        m_sendThread = new Thread(new PebbleSendThread());
        m_sendThread.setName("Pebble Send Thread");
        m_sendThread.start();
        m_updateOutOfMatch = outOfMatch;

        PebbleKit.registerReceivedDataHandler(this, m_dataReceiver);
        m_dataReceiverRegistered = true;

        // Start the watchapp
        PebbleKit.startAppOnPebble(this, PEBBLE_UUID);

        // Update all observers for starting state
        for (DeletableObserver observer : m_observers) {
            observer.checkUpdate();
        }
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
            Log.d(PebbleCommunicationService.class.getName(), "Interrupting the send thread for service destruction");
            m_sendThread.interrupt();
        }
        if (m_dataReceiverRegistered) {
            unregisterReceiver(m_dataReceiver);
            m_dataReceiverRegistered = false;
        }
        if (m_isBound) {
            unbindService(m_connection);
        }
        super.onDestroy();
    }

    /**
     * Unregisters any existing observers and clears the array
     */
    private void unregisterObservers() {
        for (DeletableObserver observer : m_observers) {
            observer.unregister();
        }
        m_observers.clear();
    }

    private final class PebbleSendThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Attempt to acquire the send semaphore. When there is an update, it will be acquired
                    m_sendSem.acquire();
                } catch (InterruptedException e) {
                    Log.w(PebbleCommunicationService.class.getName(), "Pebble send thread interrupted. Shutting down.", e);
                    break;
                }

                // Acquire the write lock, copy the array, and release
                m_rlock.writeLock().lock();
                int[] statusArr = m_statusArr;
                int[] numArr = m_numberArr;
                float[] battArr = m_batteryArr;
                boolean vibrate = m_vibrate;
                m_vibrate = false;
                m_rlock.writeLock().unlock();

                DateTime sendStart = DateTime.now();

                // Send each section of data as a separate message with a bit of a backoff so the
                // Pebble isn't overloaded. In total, a send will take at least 120 ms. If the send
                // process for the individual parts ends up not leaving 40 ms at the end for the Pebble
                // to process all messages, it will wait a minimum of 40 ms to not overload it.
                {
                    PebbleDictionary dict = new PebbleDictionary();
                    for (int i = 0; i < 6; i++) {
                        // Offset by the start of the red team status
                        dict.addUint32(i + RED1, statusArr[i]);
                    }
                    if ((m_updateOutOfMatch || m_inMatch) && vibrate) {
                        dict.addUint32(VIBE, (byte) 1);
                    }

                    PebbleKit.sendDataToPebble(PebbleCommunicationService.this, PEBBLE_UUID, dict);
                }

                {
                    DateTime start = DateTime.now();
                    PebbleDictionary dict = new PebbleDictionary();
                    for (int i = 0; i < 6; i++) {
                        // Offset by the start of the red team status
                        dict.addUint32(i + RED1_NUM, numArr[i]);
                    }

                    try {
                        // Ensure the pebble has at least 40 ms to process exisiting messages.
                        long interval = 40 - (DateTime.now().getMillis() - start.getMillis());
                        if (interval > 0) {
                            Thread.sleep(interval);
                        }
                    } catch (InterruptedException e) {
                        Log.w(PebbleCommunicationService.class.getName(), "Pebble send thread interrupted. Shutting down.", e);
                        break;
                    }

                    PebbleKit.sendDataToPebble(PebbleCommunicationService.this, PEBBLE_UUID, dict);
                }

                {
                    DateTime start = DateTime.now();
                    PebbleDictionary dict = new PebbleDictionary();
                    for (int i = 0; i < 6; i++) {
                        float oldBat = battArr[i];
                        if (oldBat > 100) {
                            oldBat = 99.99f;
                        }
                        // We only send 4 digits to the watch, as that's all it can display
                        int intBat = (int) (oldBat * 100);
                        // Offset by the start of the red team battery
                        dict.addUint32(i + RED1_BATT, intBat);
                    }

                    // On the last update, include the current match status
                    dict.addUint32(MATCH_STATE, m_matchStatus.ordinal());

                    try {
                        // Ensure the pebble has at least 40 ms to process exisiting messages.
                        long interval = 40 - (DateTime.now().getMillis() - start.getMillis());
                        if (interval > 0) {
                            Thread.sleep(interval);
                        }
                    } catch (InterruptedException e) {
                        Log.w(PebbleCommunicationService.class.getName(), "Pebble send thread interrupted. Shutting down.", e);
                        break;
                    }

                    PebbleKit.sendDataToPebble(PebbleCommunicationService.this, PEBBLE_UUID, dict);
                }

                // We don't send data any faster than once every 120 ms, in order to prevent
                // congestion when lots of teams update at once and give the pebble app time to
                // process all incoming messages. If the remaining wait time is less than 40 ms, use
                // at least 40.
                try {
                    long interval = 120 - (DateTime.now().getMillis() - sendStart.getMillis());
                    if (interval < 40) {
                        interval = 40;
                    }
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Log.w(PebbleCommunicationService.class.getName(), "Pebble send thread interrupted. Shutting down.", e);
                    break;
                }
            }
        }
    }

    /**
     * Simple interface to allow {@link com.fsilberberg.ftamonitor.services.PebbleCommunicationService.TeamProblemObserver}
     * and {@link com.fsilberberg.ftamonitor.view.fieldmonitor.FieldMonitorStatusFragment.FieldStatusObserver}
     * to live in the same list.
     */
    private interface DeletableObserver {
        void checkUpdate();

        void unregister();
    }

    private final class TeamProblemObserver extends Observable.OnPropertyChangedCallback implements DeletableObserver {
        private final Collection<Integer> TEAM_PROPERTIES = Arrays.asList(BR.teamNumber,
                BR.robotStatus, BR.bypassed, BR.estop, BR.dataRate, BR.battery);
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
        private static final byte BAT = 9;

        private static final float DEFAULT_BATTERY = 37.37f;

        private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        private final TeamStatus m_teamStatus;
        private final boolean m_bandwidthNotify;
        private final float m_maxBandwidth;
        private final boolean m_lowBatteryNotify;
        private final float m_lowBattery;
        private final int m_teamStation;
        private final int m_vibeInterval;
        private byte m_lastStatus = ETH;
        private int m_teamNum;
        private int m_lastTeamNum;
        private float m_battery = DEFAULT_BATTERY;
        private float m_lastBattery = DEFAULT_BATTERY;
        private DateTime m_lastVibeTime = DateTime.now();
        private MatchStatus m_lastMatchStatus;

        private TeamProblemObserver(TeamStatus teamStatus, boolean bandwidthNotify, float maxBandwidth,
                                    boolean lowBatteryNotify, float lowBattery, int teamStation, int vibeInterval) {
            m_teamStatus = teamStatus;
            m_bandwidthNotify = bandwidthNotify;
            m_maxBandwidth = maxBandwidth;
            m_lowBattery = lowBattery;
            m_lowBatteryNotify = lowBatteryNotify;
            m_teamStation = teamStation;
            m_vibeInterval = vibeInterval;
            m_teamNum = m_teamStatus.getTeamNumber();
            m_lastMatchStatus = m_fieldStatus.getMatchStatus();
        }

        /**
         * Initializes the team problem observer to start listening for updatess from the field and
         * team statuses. We make sure not to do this in the constructor to avoid passing around an
         * object that has not been fully initialized.
         *
         * @return This observer, fully initialized.
         */
        public TeamProblemObserver init() {
            m_teamStatus.addOnPropertyChangedCallback(this);
            m_fieldStatus.addOnPropertyChangedCallback(this);
            return this;
        }

        @Override
        public void onPropertyChanged(Observable observable, int property) {
            if (TEAM_PROPERTIES.contains(property)) {
                updateTeamStatus();
            } else if (property == BR.matchStatus) {
                batteryCheckUpdate();
                updateTeamStatus();
            }
        }

        @Override
        public void checkUpdate() {
            batteryCheckUpdate();
            updateTeamStatus();
        }

        /**
         * Updates the current battery monitor checking state. If we're in between matches, then
         * we don't check the battery. If auto just started, reset the battery status.
         */
        private void batteryCheckUpdate() {
            MatchStatus newStatus = m_fieldStatus.getMatchStatus();
            if (newStatus.equals(m_lastMatchStatus)) {
                return;
            }

            m_lastMatchStatus = newStatus;
            switch (m_lastMatchStatus) {
                case AUTO:
                case READY_TO_PRESTART:
                    // In both these cases, we want to reset the battery. We could have gotten
                    // some spurious values from during setup, so we want to make sure that everything
                    // is reset for the actual match. However, the pre-start values might also be
                    // useful, so we reset after the opponents have left the field.
                    m_battery = DEFAULT_BATTERY;
                    break;
                case PRESTART_INITIATED:
                case PRESTART_COMPLETED:
                case NOT_READY:
                case MATCH_READY:
                case TELEOP:
                case ABORTED:
                case OVER:
                case TIMEOUT:
                default:
                    break;
            }
        }

        /**
         * Updates the status of the current team. Normally, this will optimize by not updating the status if the
         * status is the same as the previous status. If force is true, however, then the status will be updated
         * regardless of whether or not the new status is the same as the last status
         */
        private void updateTeamStatus() {
            byte status;
            if (m_teamStatus.isEstop()) {
                status = ESTOP;
            } else if (m_teamStatus.isBypassed()) {
                status = BYP;
            } else {
                switch (m_teamStatus.getRobotStatus()) {
                    case NO_DS_ETH:
                        status = ETH;
                        break;
                    case NO_DS:
                        status = DS;
                        break;
                    case NO_RADIO:
                        status = RADIO;
                        break;
                    case NO_RIO:
                        status = RIO;
                        break;
                    case NO_CODE:
                        status = CODE;
                        break;
                    case GOOD:
                    default:
                        // Battery is more important than bandwidth, imo
                        if (m_lowBatteryNotify && m_teamStatus.getBattery() < m_lowBattery) {
                            status = BAT;
                        } else if (m_bandwidthNotify && m_teamStatus.getDataRate() > m_maxBandwidth) {
                            status = BWU;
                        } else {
                            status = GOOD;
                        }
                        break;
                }
            }

            m_teamNum = m_teamStatus.getTeamNumber();

            // Only update the battery status if it's now less than the previous battery value.
            float battery = m_teamStatus.getBattery();
            if (battery < m_battery) {
                m_battery = battery;
            }

            if (status != m_lastStatus ||
                    m_teamNum != m_lastTeamNum ||
                    m_battery != m_lastBattery) {
                // Only vibrate if the status has been updated. Don't do it for team number or
                // battery updates
                boolean vibrate = false;
                if (status != m_lastStatus) {
                    if (m_lastVibeTime.plusSeconds(m_vibeInterval).isBeforeNow()) {
                        vibrate = true;
                        m_lastVibeTime = DateTime.now();
                    }
                }

                int arrIndex = m_teamStation - 1;

                m_rlock.readLock().lock();
                try {
                    m_statusArr[arrIndex] = status;
                    m_numberArr[arrIndex] = m_teamNum;
                    m_batteryArr[arrIndex] = m_battery;
                    if (vibrate) {
                        m_vibrate = true;
                    }
                    m_sendSem.release();
                } finally {
                    m_rlock.readLock().unlock();
                    m_lastStatus = status;
                    m_lastTeamNum = m_teamNum;
                    m_lastBattery = m_battery;
                }
            }
        }

        @Override
        public void unregister() {
            m_teamStatus.removeOnPropertyChangedCallback(this);
            m_fieldStatus.removeOnPropertyChangedCallback(this);
        }
    }

    /**
     * Watches the field connection service and updates all statuses when the field connects
     */
    private final class ConnectionObserver extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable observable, int property) {
            if (property == BR.connectionState &&
                    ((FieldConnectionService.ConnectionStateObservable) observable).getState() == ConnectionState.Connected) {
                for (DeletableObserver observer : m_observers) {
                    observer.checkUpdate();
                }
            }
        }
    }

    private final class FieldUpdateObserver extends Observable.OnPropertyChangedCallback implements DeletableObserver {
        private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();

        public FieldUpdateObserver init() {
            m_fieldStatus.addOnPropertyChangedCallback(this);
            return this;
        }

        public void unregister() {
            m_fieldStatus.removeOnPropertyChangedCallback(this);
        }

        @Override
        public void checkUpdate() {
            checkMatchStatus();
        }

        @Override
        public void onPropertyChanged(Observable observable, int property) {
            if (property != BR.matchStatus) {
                return;
            }

            checkMatchStatus();
        }

        private void checkMatchStatus() {
            MatchStatus curStatus = m_fieldStatus.getMatchStatus();
            switch (curStatus) {
                case AUTO:
                case TELEOP:
                    m_inMatch = true;
                    break;
                default:
                    m_inMatch = false;
                    break;
            }

            m_rlock.readLock().lock();
            try {
                m_matchStatus = curStatus;
                m_sendSem.release();
            } finally {
                m_rlock.readLock().unlock();
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
                // Force all team statuses to be updated
                for (DeletableObserver observer : m_observers) {
                    observer.checkUpdate();
                }
            }
        }
    }
}
