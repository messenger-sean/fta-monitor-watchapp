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
    private static final String BANDWIDTH_INTENT_EXTRA = "bandwidth_intent_extra";

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

    // Class lock and notifier for accessing the queue and making changes to the state, as well as letting
    // the communications thread know to send new data

    private final Semaphore m_sendSem = new Semaphore(1);
    private final int[] m_statusArr = new int[6];
    private final int[] m_numberArr = new int[6];
    private final float[] m_batteryArr = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
            Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
    private boolean m_vibrate = false;
    // Note that while this is a rwlock, I'm not actually using it as a strictly read-write style lock. In this case,
    // the "readers" are the update listeners for each team, which can all modify the status array concurrently,
    // and the send thread is the writer, which must have exclusive access to the entire array
    private final ReadWriteLock m_rlock = new ReentrantReadWriteLock(true);
    private Thread m_sendThread;

    // The receiver for messages from the pebble
    private final UpdateReceiver m_dataReceiver = new UpdateReceiver();
    private boolean m_dataReceiverRegistered = false;

    // The registered team problem observers
    private Collection<TeamProblemObserver> m_observers = new ArrayList<>();

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
        float bandwidth = intent.getFloatExtra(BANDWIDTH_INTENT_EXTRA, 7.0f);

        // Unregister any existing observers
        unregisterObservers();

        // Register all of the observers and set up the send thread
        setup(vibeInterval, outOfMatch, bandwidth);

        return START_STICKY;
    }

    private void setup(int vibeInterval, boolean outOfMatch, float bandwidth) {
        FieldStatus fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue1(), bandwidth, BLUE1, vibeInterval));
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue2(), bandwidth, BLUE2, vibeInterval));
        m_observers.add(new TeamProblemObserver(fieldStatus.getBlue3(), bandwidth, BLUE3, vibeInterval));
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed1(), bandwidth, RED1, vibeInterval));
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed2(), bandwidth, RED2, vibeInterval));
        m_observers.add(new TeamProblemObserver(fieldStatus.getRed3(), bandwidth, RED3, vibeInterval));

        m_sendThread = new Thread(new PebbleSendThread());
        m_sendThread.setName("Pebble Send Thread");
        m_sendThread.start();

        PebbleKit.registerReceivedDataHandler(this, m_dataReceiver);
        m_dataReceiverRegistered = true;

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
        for (TeamProblemObserver observer : m_observers) {
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
                // Pebble isn't overloaded. In total, a send will take 100 ms.
                {
                    PebbleDictionary dict = new PebbleDictionary();
                    for (int i = 0; i < 6; i++) {
                        // Offset by the start of the red team status
                        dict.addUint32(i + RED1, statusArr[i]);
                    }
                    if (vibrate) {
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
                        // Ensure the pebble has at least 10 ms to process exisiting messages.
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

                    try {
                        // Ensure the pebble has at least 10 ms to process exisiting messages.
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

    private final class TeamProblemObserver extends Observable.OnPropertyChangedCallback {
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

        private final TeamStatus m_teamStatus;
        private final float m_maxBandwidth;
        private final int m_teamStation;
        private final int m_vibeInterval;
        private byte m_lastStatus = ETH;
        private int m_teamNum;
        private int m_lastTeamNum;
        private float m_battery = Integer.MAX_VALUE;
        private float m_lastBattery = Integer.MAX_VALUE;
        private DateTime m_lastVibeTime = DateTime.now();

        private TeamProblemObserver(TeamStatus teamStatus, float maxBandwidth, int teamStation, int vibeInterval) {
            m_teamStatus = teamStatus;
            m_maxBandwidth = maxBandwidth;
            m_teamStation = teamStation;
            m_teamStatus.addOnPropertyChangedCallback(this);
            m_vibeInterval = vibeInterval;
            m_teamNum = m_teamStatus.getTeamNumber();
        }

        @Override
        public void onPropertyChanged(Observable observable, int property) {
            if (TEAM_PROPERTIES.contains(property)) {
                updateTeamStatus();
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
                        if (m_teamStatus.getDataRate() > m_maxBandwidth) {
                            status = BWU;
                        } else {
                            status = GOOD;
                        }
                        break;
                }
            }

            m_teamNum = m_teamStatus.getTeamNumber();

            // If we're now on a new team, reset the battery status.
            if (m_teamNum != m_lastTeamNum) {
                m_battery = Float.MAX_VALUE;
                m_lastBattery = Float.MAX_VALUE;
            }

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

        private void unregister() {
            m_teamStatus.removeOnPropertyChangedCallback(this);
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
                for (TeamProblemObserver observer : m_observers) {
                    observer.updateTeamStatus();
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
                // Force all team statuses to be updated
                for (TeamProblemObserver observer : m_observers) {
                    observer.updateTeamStatus();
                }
            }
        }
    }
}
