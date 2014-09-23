package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fsilberberg.ftamonitor.common.IObservable;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.NoopHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateDSToFMSStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateEStopChangedHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateFieldNetworkStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchChangedHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchNotReadyHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchPlayStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateMatchStatusHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.UpdateStationConnectionChangedHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * This "service" maintains the connection to field, and sets up the various proxy functions for
 * listening to the field calls
 */
public class FieldConnectionService implements IForegroundService {

    // Public intent extras for communicating with this service
    public static final String URL_INTENT_EXTRA = "URL_INTENT_EXTRA";
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


    private static final ConnectionStateObservable m_observable = new ConnectionStateObservable();

    public static void registerConnectionObserver(IObserver<ConnectionState> observer) {
        m_observable.registerObserver(observer);
    }

    public static void deregisterConnectionObserver(IObserver<ConnectionState> observer) {
        m_observable.deregisterObserver(observer);
    }

    public static ConnectionState getState() {
        return m_observable.getState();
    }

    private HubConnection m_fieldConnection;
    private boolean m_connectionInProgress = false;
    private boolean m_connectionStarted = false;
    private HubProxy m_fieldProxy;
    private String m_url;

    private final Object m_lock = new Object();

    public FieldConnectionService() {
    }

    @Override
    public void stopService() {
        m_fieldConnection.disconnect();
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

    @Override
    public void startService(Context context, final Intent intent) {
        // Run on a background thread to avoid blocking the UI
        Log.d(FieldConnectionService.class.getName(), "Called startservice");
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
                        Log.w(FieldConnectionService.class.getName(), "Received signalr error", throwable);
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
                        m_observable.setConnectionState(newState);
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
    }

    /**
     * Gets the url being used by the connection
     * @return The connection url
     */
    public String getUrl() {
        return m_url;
    }

    private static class ConnectionStateObservable implements IObservable<ConnectionState> {

        private final Collection<IObserver<ConnectionState>> m_observers = new ArrayList<>();
        private ConnectionState m_connectionState = ConnectionState.Disconnected;

        public void setConnectionState(ConnectionState newState) {
            if (!m_connectionState.equals(newState)) {
                m_connectionState = newState;
                for (IObserver<ConnectionState> observer : m_observers) {
                    observer.update(newState);
                }
            }
        }

        public ConnectionState getState() {
            return m_connectionState;
        }

        @Override
        public void registerObserver(IObserver<ConnectionState> observer) {
            if (!m_observers.contains(observer)) {
                m_observers.add(observer);
            }
        }

        @Override
        public void deregisterObserver(IObserver<ConnectionState> observer) {
            m_observers.remove(observer);
        }
    }
}
