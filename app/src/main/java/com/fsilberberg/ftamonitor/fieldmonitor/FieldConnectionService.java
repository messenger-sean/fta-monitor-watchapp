package com.fsilberberg.ftamonitor.fieldmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

public class FieldConnectionService extends Service {

    public static final String URL_INTENT_EXTRA = "url_intent_extra";

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
            registerProxyFunctions();
            try {
                m_fieldConnection.start().get();
                m_connectionStarted = true;
                return Service.START_REDELIVER_INTENT;
            } catch (InterruptedException e) {
                Log.e(FieldConnectionService.class.getName(), "Error when creating the field monitor connection", e);
                return Service.START_NOT_STICKY;
            } catch (ExecutionException e) {
                Log.e(FieldConnectionService.class.getName(), "Error when creating the field monitor connection", e);
                e.printStackTrace();
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

    @Override
    public void onDestroy() {
        m_fieldConnection.disconnect();
        super.onDestroy();
    }

    private void registerProxyFunctions() {
        m_fieldProxy.on("updateMatchReadyToPrestart", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchPreStartInitiated", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchPreStartCompleted", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchReady", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchNotReady", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchStartAuto", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchPauseAuto", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchEndAuto", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchStartTeleop", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchPauseTeleop", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchEndTeleop", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchPostScore", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateDSControl", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateEStopChanged", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateStationConnectionChanged", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateDSToFMSStatus", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchChanged", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateMatchPlayStatus", new Handler(), JSONObject.class);
        m_fieldProxy.on("updateFieldNetworkStatus", new Handler(), JSONObject.class);
    }

    private class Handler implements SubscriptionHandler1<JSONObject> {

        @Override
        public void run(JSONObject jsonObject) {
            Log.d(Handler.class.getName(), jsonObject.toString());
        }
    }
}
