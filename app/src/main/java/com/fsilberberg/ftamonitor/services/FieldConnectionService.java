package com.fsilberberg.ftamonitor.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.MatchStateProxyHandler;
import com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers.TeamProxyHandler;
import com.fsilberberg.ftamonitor.view.MainActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

import static microsoft.aspnet.signalr.client.ConnectionState.Connecting;
import static microsoft.aspnet.signalr.client.ConnectionState.Disconnected;

/**
 * Maintains the connection to the field.
 */
public class FieldConnectionService extends Service {

    // Lifecycle intent Extra
    private static final String LIFECYCLE_INTENT_EXTRA = "lifecycle_intent_extra";
    private static final int START = 0;
    private static final int UPDATE = 1;
    private static final int STOP = 2;

    // Parameter intent extras
    private static final String URL_INTENT_EXTRA = "url_intent_extra";
    private static final String SIGNALR_PORT_INTENT_EXTRA = "signalr_port_intent_extra";
    private static final String MONITOR_PORT_INTENT_EXTRA = "monitor_port_intent_extra";

    // Notification ID for updating the ongoing notification. 3 has no special significance other
    // than being my favorite number
    private static final int FOREGROUND_ID = 3;
    private static final int MAIN_ACTIVITY_INTENT_ID = 1;
    private static final int CLOSE_SERVICE_INTENT_ID = 2;

    // Signalr Constants
    private static final String HUB_NAME = "messageservicehub";
    private static final String FIELD_MONITOR = "fieldMonitorDataChanged";
    private static final String MATCH_STATE_CHANGED = "matchStateChanged";
    private static final String INITIAL_STATE_PATH = "/FieldMonitor/MatchNumberAndPlay";

    // This is a general ip pattern matching the general form of an ipv4 address, used for basic input validation
    // This is only a rough checker, and does not attempt to validate for out of range addresses. However, as this
    // is only working with string input that is either a url or an ip, there shouldn't be an issue with this level
    // of granularity
    private static final Pattern _generalIPPattern = Pattern.compile("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}");

    static {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
    }

    /**
     * Starts the field monitor connection service if it has not already been started. If it has been started, then the
     * connection will not be interrupted
     */
    public static void start() {
        start(false);
    }

    /**
     * Starts the field monitor connection service. If the update flag is specified, then the service will stop
     * any existing connection and update it. If it is false, the service will only start a new connection, not replace
     * the existing connection
     *
     * @param update Whether to restart an existing connection or not
     */
    public static void start(boolean update) {
        Context ctx = FTAMonitorApplication.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Intent intent = new Intent(ctx, FieldConnectionService.class);

        String enabledKey = ctx.getString(R.string.field_monitor_enabled_key);
        boolean enabled = prefs.getBoolean(enabledKey, true);
        int lifecycle;

        // Check if the field monitor is enabled. If it is, then send the start signal to the app
        // If it is not, then send the stop signal to the service
        if (enabled) {
            String fmsUrlKey = ctx.getString(R.string.fms_ip_addr_key);
            String fmsDefaultUrl = ctx.getString(R.string.fms_ip_addr_default);
            String signalrPortKey = ctx.getString(R.string.fms_signalr_port_key);
            String signalrPortDefault = ctx.getString(R.string.fms_signalr_port_default);
            String monitorPortKey = ctx.getString(R.string.fms_monitor_port_key);
            String monitorPortDefault = ctx.getString(R.string.fms_monitor_port_addr_default);
            String fieldUrl = prefs.getString(fmsUrlKey, fmsDefaultUrl);
            String signalrPort = prefs.getString(signalrPortKey, signalrPortDefault);
            String monitorPort = prefs.getString(monitorPortKey, monitorPortDefault);
            intent.putExtra(URL_INTENT_EXTRA, fieldUrl);
            intent.putExtra(SIGNALR_PORT_INTENT_EXTRA, signalrPort);
            intent.putExtra(MONITOR_PORT_INTENT_EXTRA, monitorPort);
            lifecycle = update ? UPDATE : START;
        } else {
            lifecycle = STOP;
        }

        intent.putExtra(LIFECYCLE_INTENT_EXTRA, lifecycle);
        ctx.startService(intent);
    }

    private final Object m_lock = new Object();
    private final FCSBinder m_binder = new FCSBinder();
    private final ConnectionStateObservable m_statusObservable = new ConnectionStateObservable();
    private String m_url;
    private int m_signalrPort;
    private int m_monitorPort;
    private HubConnection m_fieldConnection;
    private HubProxy m_fieldProxy;
    private Thread m_connectionThread = new Thread();

    public ConnectionState getState() {
        return m_statusObservable.getState();
    }

    public ConnectionStateObservable getStatusObservable() {
        return m_statusObservable;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        m_connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (m_lock) {
                    // Start by checking to see whether we are starting or stopping. If the intent is null, we assume
                    // that we are starting with the default ip
                    if (intent != null) {
                        int lifecycle = intent.getIntExtra(LIFECYCLE_INTENT_EXTRA, 0);
                        switch (lifecycle) {
                            case UPDATE:
                                // If we're updating, then disconnect and proceed to the start state
                                disconnect();
                                break;
                            case START:
                                if (m_statusObservable.getState() != Disconnected) {
                                    return;
                                }
                                break;
                            case STOP:
                                disconnect();
                                stopSelf();
                                return;
                        }
                    }

                    // Get the url to connect to. If the intent is non-null and has the extra, then get the extra
                    // If the intent is null or does not have the extra, we use the default field ip
                    if (intent != null && intent.hasExtra(URL_INTENT_EXTRA)) {
                        m_url = intent.getStringExtra(URL_INTENT_EXTRA);
                        // If the url is an ip address and does not have an http(s):// on the front, then append http
                        if (_generalIPPattern.matcher(m_url).matches() &&
                                (!m_url.startsWith("http://") || !m_url.startsWith("https://"))) {
                            m_url = "http://" + m_url;
                        }
                    } else {
                        m_url = getString(R.string.fms_ip_addr_default);
                    }

                    // Get the signalr port and the monitor port
                    int defaultSignalrPort = Integer.parseInt(getString(R.string.fms_signalr_port_default));
                    if (intent != null && intent.hasExtra(SIGNALR_PORT_INTENT_EXTRA)) {
                        m_signalrPort = tryParse(intent.getStringExtra(SIGNALR_PORT_INTENT_EXTRA), defaultSignalrPort);
                    } else {
                        m_signalrPort = defaultSignalrPort;
                    }
                    int defaultMonitorPort = Integer.parseInt(getString(R.string.fms_monitor_port_addr_default));
                    if (intent != null && intent.hasExtra(MONITOR_PORT_INTENT_EXTRA)) {
                        m_monitorPort = tryParse(intent.getStringExtra(MONITOR_PORT_INTENT_EXTRA), defaultMonitorPort);
                    } else {
                        m_monitorPort = defaultMonitorPort;
                    }
                }

                // Finally, do the actual connection
                try {
                    doConnect();
                } catch (MalformedURLException | URISyntaxException e) {
                    Log.e(FieldConnectionService.class.getName(),
                            "Url: " + m_url + "\n" +
                                    "Signalr Port: " + m_signalrPort + "\n" +
                                    "Monitor Port: " + m_monitorPort, e);
                }
            }
        });
        m_statusObservable.addOnPropertyChangedCallback(new FCSNotificationObserver());
        m_connectionThread.start();

        // We want to make sure that we get the same intent, that way we receive the url to connect to
        return START_REDELIVER_INTENT;
    }

    public void doConnect() throws MalformedURLException, URISyntaxException {
        // Start the connection process
        m_statusObservable.setConnectionState(Connecting);
        synchronized (m_lock) {
            // Create a new connection with the FMS hub name and register the proxy functions
            URI origUrl = new URI(m_url);
            URI finalUrl = changePort(origUrl, m_signalrPort);
            m_url = finalUrl.toASCIIString();
            m_fieldConnection = new HubConnection(m_url);
            m_fieldProxy = m_fieldConnection.createHubProxy(HUB_NAME);
            m_fieldProxy.on(FIELD_MONITOR, new TeamProxyHandler(), JsonArray.class);
            m_fieldProxy.on(MATCH_STATE_CHANGED, new MatchStateProxyHandler(), Integer.class);

            // Set up the error handler, disconnect on error
            m_fieldConnection.error(new ErrorCallback() {
                @Override
                public void onError(Throwable throwable) {
                    Log.i(FieldConnectionService.class.getName(), "Received Signalr error", throwable);
                    disconnect();
                }
            });

            // On connected, get the current match and play number from the field, so the initial state
            // is correct.
            m_fieldConnection.connected(new Runnable() {
                @Override
                public void run() {
                    try {
                        URI signalrURI = new URI(m_url);
                        URL newUrl = new URI(signalrURI.getScheme(), signalrURI.getUserInfo(),
                                signalrURI.getHost(), m_monitorPort, INITIAL_STATE_PATH, "", "").toURL();
                        try (InputStream is = newUrl.openStream();
                             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }

                            Gson gson = new Gson();
                            JsonArray initialState = gson.fromJson(sb.toString(), JsonArray.class);
                            FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
                            field.setMatchNumber(initialState.get(0).toString());
                            field.setPlayNumber(initialState.get(1).getAsInt());
                        }
                    } catch (IOException | URISyntaxException e) {
                        Log.w(FieldConnectionService.class.getName(),
                                "Error when connecting for initial match number state", e);
                    }
                }
            });

            m_fieldConnection.stateChanged(m_statusObservable);
            try {
                m_fieldConnection.start(new ServerSentEventsTransport(new NullLogger())).get();
            } catch (InterruptedException | ExecutionException e) {
                Log.w(FieldConnectionService.class.getName(), "Could not start the signalr connection", e);
            }
        }
    }

    public void disconnect() {
        synchronized (m_lock) {
            m_connectionThread.interrupt();
            if (m_fieldConnection != null) {
                m_fieldConnection.disconnect();
                m_fieldConnection = null;
                m_fieldProxy = null;
            }
        }
        m_statusObservable.setConnectionState(Disconnected);
        stopForeground(false);
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    private class FCSNotificationObserver extends android.databinding.Observable.OnPropertyChangedCallback {

        @Override
        public void onPropertyChanged(android.databinding.Observable observable, int property) {
            startForeground(FOREGROUND_ID,
                    createNotification(((ConnectionStateObservable) observable).getState()));
        }

        private Notification createNotification(ConnectionState state) {
            String contentText = state.toString() + " - " + m_url;

            // Create the intent for the main action
            Intent mainIntent = new Intent(FieldConnectionService.this, MainActivity.class);

            // Create the intent for the action button
            Intent actionIntent = new Intent(FieldConnectionService.this, FieldConnectionService.class);
            actionIntent.putExtra(LIFECYCLE_INTENT_EXTRA, STOP);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(FieldConnectionService.this)
                    .setContentTitle("Field Monitor")
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_stat_big_logo)
                    .setContentIntent(PendingIntent.getActivity(FieldConnectionService.this,
                            MAIN_ACTIVITY_INTENT_ID,
                            mainIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT))
                    .addAction(R.drawable.ic_action_remove, "Close Monitor",
                            PendingIntent.getService(FieldConnectionService.this,
                                    CLOSE_SERVICE_INTENT_ID,
                                    actionIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT));

            return builder.build();
        }
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
     * Observer implementation for the field connection state
     */
    public static class ConnectionStateObservable extends BaseObservable implements StateChangedCallback {

        @Bindable
        private ConnectionState m_connectionState = Disconnected;

        public void setConnectionState(ConnectionState newState) {
            if (!m_connectionState.equals(newState)) {
                m_connectionState = newState;
                notifyPropertyChanged(BR.connectionState);
            }
        }

        public ConnectionState getState() {
            return m_connectionState;
        }

        @Override
        public void stateChanged(ConnectionState oldState, ConnectionState newState) {
            Log.i(FieldConnectionService.class.getName(), "State changed from " + oldState + " to " + newState);
            setConnectionState(newState);
        }
    }

    private static int tryParse(String toParse, int defaultVal) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }

    private static URI changePort(URI old, int port) throws URISyntaxException {
        return new URI(old.getScheme(), old.getUserInfo(), old.getHost(), port, old.getPath(), old.getQuery(), old.getFragment());
    }
}
