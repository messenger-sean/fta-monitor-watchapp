package com.fsilberberg.ftamonitor.fieldmonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.NoopHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateDSToFMSStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateEStopChangedHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateFieldNetworkStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchChangedHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchNotReadyHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchPlayStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateStationConnectionChangedHandler;
import com.fsilberberg.ftamonitor.view.DrawerActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

import static com.fsilberberg.ftamonitor.common.MatchStatus.AUTO;
import static com.fsilberberg.ftamonitor.common.MatchStatus.AUTO_END;
import static com.fsilberberg.ftamonitor.common.MatchStatus.AUTO_PAUSED;
import static com.fsilberberg.ftamonitor.common.MatchStatus.MATCH_READY;
import static com.fsilberberg.ftamonitor.common.MatchStatus.OVER;
import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_COMPLETED;
import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_INITIATED;
import static com.fsilberberg.ftamonitor.common.MatchStatus.READY_TO_PRESTART;
import static com.fsilberberg.ftamonitor.common.MatchStatus.TELEOP;
import static com.fsilberberg.ftamonitor.common.MatchStatus.TELEOP_PAUSED;

public class FieldConnectionService extends Service {

    // Public intent extras for communicating with this service
    public static final String URL_INTENT_EXTRA = "URL_INTENT_EXTRA";
    public static final String CLOSE_CONNECTION_INTENT_EXTRA = "CLOSE_CONNECTION_INTENT_EXTRA";
    public static final String UPDATE_URL_INTENT_EXTRA = "UPDATE_URL_INTENT_EXTRA";

    // Signalr Constants
    private static final String HUB_NAME = "messageservicehub";
    private static final String UPDATE_MATCH_READY_TO_PRESTART = "updateMatchReadyToPrestart";
    private static final String UPDATE_MATCH_PRESTART_INITIATED = "updateMatchPreStartInitiated";
    private static final String UPDATE_MATCH_PRESTART_COMPLETE = "updateMatchPreStartCompleted";
    private static final String UPDATE_MATCH_READY = "updateMatchReady";
    private static final String UPDATE_MATCH_NOT_READY = "updateMatchNotReady";
    private static final String UPDATE_MATCH_START_AUTO = "updateMatchStartAuto";
    private static final String UPDATE_MATCH_PAUSE_AUT0 = "updateMatchPauseAuto";
    private static final String UPDATE_MATCH_END_AUTO = "updateMatchEndAuto";
    private static final String UPDATE_MATCH_START_TELEOP = "updateMatchStartTeleop";
    private static final String UPDATE_MATCH_PAUSE_TELEOP = "updateMatchPauseTeleop";
    private static final String UPDATE_MATCH_END_TELEOP = "updateMatchEndTeleop";
    private static final String UPDATE_MATCH_POST_SCORE = "updateMatchPostScore";
    private static final String UPDATE_DS_CONTROL = "updateDSControl";
    private static final String UPDATE_ESTOP_CHANGED = "updateEStopChanged";
    private static final String UPDATE_STATION_CONNECTION_CHANGED = "updateStationConnectionChanged";
    private static final String UPDATE_DS_TO_FMS_STATUS = "updateDSToFMSStatus";
    private static final String UPDATE_MATCH_CHANGED = "updateMatchChanged";
    private static final String UPDATE_MATCH_PLAY_STATUS = "updateMatchPlayStatus";
    private static final String UPDATE_FIELD_NETWORK_STATUS = "updateFieldNetworkStatus";

    // Notification ID for updating the ongoing notification. 3 has no special significance other
    // than being my favorite number
    private static final int ID = 3;
    private static final int MAIN_ACTIVITY_INTENT_ID = 1;
    private static final int CLOSE_SERVICE_INTENT_ID = 2;

    private HubConnection m_fieldConnection;
    private boolean m_connectionInProgress = false;
    private boolean m_connectionStarted = false;
    private HubProxy m_fieldProxy;
    private String m_signalrPath = "/signalr";
    private String m_url;

    private final Object m_lock = new Object();

    public FieldConnectionService() {
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // First, check to see if we are cancelling this service
        if (intent.getBooleanExtra(CLOSE_CONNECTION_INTENT_EXTRA, false)) {
            stopForeground(true);
            stopSelf();
            return START_REDELIVER_INTENT;
        }

        // Run on a background thread to avoid blocking the UI
        new Thread(new Runnable() {
            public void run() {
                // Close the connection if we're restarting
                if (intent.getBooleanExtra(UPDATE_URL_INTENT_EXTRA, false) && m_connectionStarted) {
                    m_fieldConnection.disconnect();
                    m_connectionStarted = false;
                    m_connectionInProgress = false;
                }


                synchronized (m_lock) {
                    // Start the connection if it's not currently started
                    if (!m_connectionStarted && !m_connectionInProgress) {
                        // We're starting a connection attempt, only let one occur at a time
                        m_connectionInProgress = true;
                    } else {
                        // If we're not starting, just return
                        return;
                    }
                }

                // Set up the connection and proxy objects
                m_url = intent.getStringExtra(URL_INTENT_EXTRA);
                m_fieldConnection = new HubConnection(m_url);
                m_fieldProxy = m_fieldConnection.createHubProxy(HUB_NAME);
                registerProxyFunctions();

                // On error, reset the variables and log an error
                m_fieldConnection.error(new ErrorCallback() {
                    @Override
                    public void onError(Throwable throwable) {
                        synchronized (m_lock) {
                            m_connectionInProgress = false;
                        }
                        Log.w(FieldConnectionService.class.getName(), "Received error", throwable);
                    }
                });

                // On connection, reset the variables
                m_fieldConnection.connected(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (m_lock) {
                            m_connectionStarted = true;
                            m_connectionInProgress = false;
                        }
                        Log.d(FieldConnectionService.class.getName(), "Connected to FMS at " + m_url);
                    }
                });

                // On state changed, we update the notification with the new state
                m_fieldConnection.stateChanged(new StateChangedCallback() {
                    @Override
                    public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                        startForeground(ID, createNotification(newState));
                    }
                });

                // Attempt to start the connection
                try {
                    m_fieldConnection.start().get();
                } catch (InterruptedException e) {
                    Log.e(FieldConnectionService.class.getName(), "Error when creating the field monitor connection", e);
                } catch (ExecutionException e) {
                    Log.e(FieldConnectionService.class.getName(), "Error when creating the field monitor connection", e);
                    e.printStackTrace();
                }
            }
        }).start();

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't support binding
        return null;
    }

    @Override
    public void onDestroy() {
        m_fieldConnection.disconnect();
        super.onDestroy();
    }

    private void registerProxyFunctions() {
        m_fieldProxy.on(UPDATE_MATCH_READY_TO_PRESTART, new UpdateMatchStatusHandler(UPDATE_MATCH_READY_TO_PRESTART, READY_TO_PRESTART), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_PRESTART_INITIATED, new UpdateMatchStatusHandler(UPDATE_MATCH_PRESTART_INITIATED, PRESTART_INITIATED), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_PRESTART_COMPLETE, new UpdateMatchStatusHandler(UPDATE_MATCH_PRESTART_COMPLETE, PRESTART_COMPLETED), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_READY, new UpdateMatchStatusHandler(UPDATE_MATCH_READY, MATCH_READY), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_NOT_READY, new UpdateMatchNotReadyHandler(UPDATE_MATCH_NOT_READY));
        m_fieldProxy.on(UPDATE_MATCH_START_AUTO, new UpdateMatchStatusHandler(UPDATE_MATCH_START_AUTO, AUTO), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_PAUSE_AUT0, new UpdateMatchStatusHandler(UPDATE_MATCH_PAUSE_AUT0, AUTO_PAUSED), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_END_AUTO, new UpdateMatchStatusHandler(UPDATE_MATCH_END_AUTO, AUTO_END), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_START_TELEOP, new UpdateMatchStatusHandler(UPDATE_MATCH_START_TELEOP, TELEOP), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_PAUSE_TELEOP, new UpdateMatchStatusHandler(UPDATE_MATCH_PAUSE_TELEOP, TELEOP_PAUSED), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_END_TELEOP, new UpdateMatchStatusHandler(UPDATE_MATCH_END_TELEOP, OVER), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_POST_SCORE, new UpdateMatchStatusHandler(UPDATE_MATCH_POST_SCORE, OVER), JsonObject.class);
        m_fieldProxy.on(UPDATE_DS_CONTROL, new NoopHandler(), JsonObject.class);
        m_fieldProxy.on(UPDATE_ESTOP_CHANGED, new UpdateEStopChangedHandler(UPDATE_ESTOP_CHANGED), JsonObject.class);
        m_fieldProxy.on(UPDATE_STATION_CONNECTION_CHANGED, new UpdateStationConnectionChangedHandler(UPDATE_STATION_CONNECTION_CHANGED), JsonObject.class);
        m_fieldProxy.on(UPDATE_DS_TO_FMS_STATUS, new UpdateDSToFMSStatusHandler(UPDATE_DS_TO_FMS_STATUS), JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_CHANGED, new UpdateMatchChangedHandler(UPDATE_MATCH_CHANGED), JsonObject.class, JsonObject.class);
        m_fieldProxy.on(UPDATE_MATCH_PLAY_STATUS, new UpdateMatchPlayStatusHandler(UPDATE_MATCH_PLAY_STATUS), JsonObject.class);
        m_fieldProxy.on(UPDATE_FIELD_NETWORK_STATUS, new UpdateFieldNetworkStatusHandler(UPDATE_FIELD_NETWORK_STATUS), JsonArray.class);
    }

    private Notification createNotification(ConnectionState state) {
        String contentText = state.toString() + " - " + m_url;

        // Create the intent for the main action
        Intent mainIntent = new Intent(this, DrawerActivity.class);
        mainIntent.putExtra(DrawerActivity.VIEW_INTENT_EXTRA, DrawerActivity.DisplayView.FIELD_MONITOR.ordinal());

        // Create the intent for the action button
        Intent actionIntent = new Intent(this, getClass());
        actionIntent.putExtra(CLOSE_CONNECTION_INTENT_EXTRA, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Field Monitor")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(
                        PendingIntent.getActivity(this, MAIN_ACTIVITY_INTENT_ID, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .addAction(R.drawable.ic_action_remove, "Close Monitor",
                        PendingIntent.getService(this, CLOSE_SERVICE_INTENT_ID, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        return builder.build();
    }
}
