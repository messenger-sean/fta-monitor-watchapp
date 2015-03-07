package com.fsilberberg.ftamonitor.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Observable;
import com.fsilberberg.ftamonitor.common.Observer;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Maintains the connection to the field.
 */
public class FieldConnectionService extends Service {

    private static final String URL_INTENT_EXTRA = "url_intent_extra";

    // Signalr Constants
    private static final String HUB_NAME = "messageservicehub";
    private static final String FIELD_MONITOR = "fieldMonitorDataChanged";
    private static final String MATCH_STATE_CHANGED = "matchStateChanged";

    private final FCSBinder m_binder = new FCSBinder();
    private final ConnectionStateObservable m_observable = new ConnectionStateObservable();
    private HubConnection m_fieldConnection;
    private HubProxy m_fieldProxy;
    private Thread m_connectionThread = new Thread();

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(getClass().getName(), "Received intent in FieldConnectionService, current state is " + m_observable.getState());
        m_connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String url;
                if (intent == null) {
                    url = FTAMonitorApplication.DEFAULT_IP;
                } else {
                    url = intent.getStringExtra(URL_INTENT_EXTRA);
                }

                switch (m_observable.getState()) {
                    case Connected:
                        if (m_fieldConnection != null) {
                            m_fieldConnection.disconnect();
                            m_fieldConnection = null;
                            m_fieldProxy = null;
                        }
                }
            }
        });

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    /**
     * Implements the binder for the Field Connection Service
     */
    public class FCSBinder extends Binder {
        public FieldConnectionService getService() {
            return FieldConnectionService.this;
        }
    }

    /**
     * Watches for changes in the FMS connection settings and restarts the service if necessary
     */
    public class FCSSharedPrefs implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Context ctx = FTAMonitorApplication.getContext();
        private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        private final String fmsUrlKey;
        private final String fmsOnFieldKey;

        public FCSSharedPrefs() {
            fmsUrlKey = ctx.getString(R.string.fms_ip_addr_key);
            fmsOnFieldKey = ctx.getString(R.string.on_field_key);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateService();
        }

        public void updateService() {
            Intent intent = new Intent(ctx, FieldConnectionService.class);
            boolean onField = prefs.getBoolean(fmsUrlKey, true);
            String fieldUrl = onField ? FTAMonitorApplication.DEFAULT_IP :
                    prefs.getString(fmsOnFieldKey, FTAMonitorApplication.DEFAULT_IP);
            intent.putExtra(URL_INTENT_EXTRA, fieldUrl);
            ctx.startService(intent);
        }
    }

    /**
     * Observer implementation for the field connection state
     */
    private static class ConnectionStateObservable implements Observable<ConnectionState> {

        private final Collection<Observer<ConnectionState>> m_observers = new ArrayList<>();
        private ConnectionState m_connectionState = ConnectionState.Disconnected;

        private void setConnectionState(ConnectionState newState) {
            if (!m_connectionState.equals(newState)) {
                m_connectionState = newState;
                for (Observer<ConnectionState> observer : m_observers) {
                    observer.update(newState);
                }
            }
        }

        private ConnectionState getState() {
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
