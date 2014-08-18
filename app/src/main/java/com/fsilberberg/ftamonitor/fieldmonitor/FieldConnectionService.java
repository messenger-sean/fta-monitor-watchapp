package com.fsilberberg.ftamonitor.fieldmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

public class FieldConnectionService extends Service {

    public static final String URL_INTENT_EXTRA = "url_intent_extra";
    public static final int SUCCESS_CODE = 1;
    public static final int FAILURE_CODE = -1;

    private static final String HUB_NAME = "messageservicehub";

    private HubConnection m_fieldConnection;
    private boolean m_connectionStarted = false;
    private HubProxy m_fieldProxy;

    public FieldConnectionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!m_connectionStarted) {
            String url = intent.getStringExtra(URL_INTENT_EXTRA);
            m_fieldConnection = new HubConnection(url);
            m_fieldProxy = m_fieldConnection.createHubProxy(HUB_NAME);
            try {
                m_fieldConnection.start().wait();
                m_connectionStarted = true;
                registerProxyFunctions();
                return Service.START_REDELIVER_INTENT;
            } catch (InterruptedException e) {
                Log.e(FieldConnectionService.class.getName(), "Error when creating the field monitor connection", e);
                return Service.START_NOT_STICKY;
            }
        } else {
            return Service.START_REDELIVER_INTENT;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't support binding
        return null;
    }

    private void registerProxyFunctions() {

    }
}
