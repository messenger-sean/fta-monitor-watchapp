package com.fsilberberg.ftamonitor.services.pebble;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Android service responsible for maintaining communications to the Pebble.
 */
public class PebbleCommunicationService extends Service {

    private static final String LIFECYCLE_INTENT_EXTRA = "lifecycle_intent_extra";
    private static final int START = 0;
    private static final int STOP = 1;

    private static final String VIBE_INTERVAL_INTENT_EXTRA = "vibe_interval_intent_extra";
    private static final String OUT_OF_MATCH_INTENT_EXTRA = "out_of_match_intent_extra";
    private static final String BANDWIDTH_NOTIFY_INTENT_EXTRA = "bandwidth_notify_intent_extra";
    private static final String BANDWIDTH_INTENT_EXTRA = "bandwidth_intent_extra";
    private static final String LOW_BATTERY_NOTIFY_INTENT_EXTRA = "low_battery_notify_intent_extra";
    private static final String LOW_BATTERY_INTENT_EXTRA = "low_battery_intent_extra";

    private PebbleSender m_sender = null;
    private UpdateReceiver m_receiver = null;
    private Thread m_senderThread = null;
    private Collection<DeletableObserver> m_observers = null;
    private boolean m_updateReceiverRegistered = false;

    public static void start(Context ctx) {
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        int lifecycle = intent.getIntExtra(LIFECYCLE_INTENT_EXTRA, STOP);
        if (lifecycle == STOP) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Stop all existing observers
        stopObservers();
        stopThread();

        // Get the parameters for the observers
        int vibeInterval = intent.getIntExtra(VIBE_INTERVAL_INTENT_EXTRA, 10);
        boolean outOfMatch = intent.getBooleanExtra(OUT_OF_MATCH_INTENT_EXTRA, true);
        boolean bandwidthNotify = intent.getBooleanExtra(BANDWIDTH_NOTIFY_INTENT_EXTRA, true);
        float bandwidth = intent.getFloatExtra(BANDWIDTH_INTENT_EXTRA, 7.0f);
        boolean lowBatteryNotify = intent.getBooleanExtra(LOW_BATTERY_NOTIFY_INTENT_EXTRA, true);
        float lowBattery = intent.getFloatExtra(LOW_BATTERY_INTENT_EXTRA, 6.5f);

        m_sender = new PebbleSender(this);
        m_senderThread = new Thread(m_sender, "Pebble Send Thread");
        m_senderThread.setDaemon(true);
        m_senderThread.start();

        startObservers(vibeInterval, outOfMatch, bandwidthNotify, bandwidth, lowBatteryNotify, lowBattery);

        if (!m_updateReceiverRegistered) {
            m_receiver = new UpdateReceiver();
            PebbleKit.registerReceivedDataHandler(this, m_receiver);
            m_updateReceiverRegistered = true;
        }

        m_sender.startApp();

        return START_STICKY;
    }

    private void stopObservers() {
        if (m_observers != null) {
            for (DeletableObserver observer : m_observers) {
                observer.unregister();
            }

            m_observers = null;
        }

        if (m_updateReceiverRegistered) {
            unregisterReceiver(m_receiver);
            m_receiver = null;
            m_updateReceiverRegistered = false;
        }
    }

    private void stopThread() {
        if (m_senderThread != null) {
            m_senderThread.interrupt();
            m_senderThread = null;
            m_sender = null;
        }
    }

    private void startObservers(int vibeInterval, boolean outOfMatch, boolean bandwidthNotify,
                                float bandwidth, boolean lowBatteryNotify, float lowBattery) {
        FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
        m_observers = new ArrayList<>(7);
        m_observers.add(new TeamProblemObserver(field.getRed1(), m_sender, bandwidthNotify,
                bandwidth, lowBatteryNotify, lowBattery, outOfMatch, vibeInterval, 0).init());
        m_observers.add(new TeamProblemObserver(field.getRed2(), m_sender, bandwidthNotify,
                bandwidth, lowBatteryNotify, lowBattery, outOfMatch, vibeInterval, 1).init());
        m_observers.add(new TeamProblemObserver(field.getRed3(), m_sender, bandwidthNotify,
                bandwidth, lowBatteryNotify, lowBattery, outOfMatch, vibeInterval, 2).init());
        m_observers.add(new TeamProblemObserver(field.getBlue1(), m_sender, bandwidthNotify,
                bandwidth, lowBatteryNotify, lowBattery, outOfMatch, vibeInterval, 3).init());
        m_observers.add(new TeamProblemObserver(field.getBlue2(), m_sender, bandwidthNotify,
                bandwidth, lowBatteryNotify, lowBattery, outOfMatch, vibeInterval, 4).init());
        m_observers.add(new TeamProblemObserver(field.getBlue3(), m_sender, bandwidthNotify,
                bandwidth, lowBatteryNotify, lowBattery, outOfMatch, vibeInterval, 5).init());
        m_observers.add(new FieldUpdateObserver(m_sender).init());
    }

    @Override
    public void onDestroy() {
        stopObservers();
        stopThread();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No binding to the coms service.
        return null;
    }

    public void updateAll() {
        for (DeletableObserver observer : m_observers) {
            observer.checkUpdate();
        }
    }

    private final class UpdateReceiver extends PebbleKit.PebbleDataReceiver {

        private static final byte UPDATE = 8;

        public UpdateReceiver() {
            super(PebbleSender.PEBBLE_UUID);
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
