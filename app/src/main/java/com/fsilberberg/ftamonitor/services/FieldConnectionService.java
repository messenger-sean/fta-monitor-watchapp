package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.fsilberberg.ftamonitor.common.Observable;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.MatchStateProxyHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.TeamProxyHandler;
import com.google.gson.JsonArray;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * This "service" maintains the connection to field, and sets up the various proxy functions for
 * listening to the field calls
 */
public class FieldConnectionService implements ForegroundService {

    // Public intent extras for communicating with this service
    public static final String URL_INTENT_EXTRA = "URL_INTENT_EXTRA";
    public static final String UPDATE_URL_INTENT_EXTRA = "UPDATE_URL_INTENT_EXTRA";

    // Signalr Constants
    private static final String HUB_NAME = "messageservicehub";
    private static final String FIELD_MONITOR = "fieldMonitorDataChanged";
    private static final String MATCH_STATE_CHANGED = "matchStateChanged";

    private static final ConnectionStateObservable m_observable = new ConnectionStateObservable();

    public static void registerConnectionObserver(Observer<ConnectionState> observer) {
        m_observable.registerObserver(observer);
    }

    public static void deregisterConnectionObserver(Observer<ConnectionState> observer) {
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
        m_fieldProxy.on(FIELD_MONITOR, new TeamProxyHandler(), JsonArray.class);
        m_fieldProxy.on(MATCH_STATE_CHANGED, new MatchStateProxyHandler(), Integer.class);
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
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(FieldConnectionService.class.getName(), "Error when creating the field monitor connection", e);
                }
            }
        }).start();
    }

    /**
     * Gets the url being used by the connection
     *
     * @return The connection url
     */
    public String getUrl() {
        return m_url;
    }

    private static class ConnectionStateObservable implements Observable<ConnectionState> {

        private final Collection<Observer<ConnectionState>> m_observers = new ArrayList<>();
        private ConnectionState m_connectionState = ConnectionState.Disconnected;

        public void setConnectionState(ConnectionState newState) {
            if (!m_connectionState.equals(newState)) {
                m_connectionState = newState;
                for (Observer<ConnectionState> observer : m_observers) {
                    observer.update(newState);
                }
            }
        }

        public ConnectionState getState() {
            return m_connectionState;
        }

        @Override
        public void registerObserver(Observer<ConnectionState> observer) {
            if (!m_observers.contains(observer)) {
                m_observers.add(observer);
            }
        }

        @Override
        public void deregisterObserver(Observer<ConnectionState> observer) {
            m_observers.remove(observer);
        }
    }
}
