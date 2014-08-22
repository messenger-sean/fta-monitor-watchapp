package com.fsilberberg.ftamonitor.fieldmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;

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
                m_fieldConnection.error(new ErrorCallback() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(FieldConnectionService.class.getName(), "Received error", throwable);
                    }
                });
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
        m_fieldProxy.on("updateMatchReadyToPrestart", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchPreStartInitiated", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchPreStartCompleted", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchReady", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchNotReady", new Handler());
        m_fieldProxy.on("updateMatchStartAuto", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchPauseAuto", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchEndAuto", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchStartTeleop", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchPauseTeleop", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchEndTeleop", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchPostScore", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateDSControl", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateEStopChanged", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateStationConnectionChanged", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateDSToFMSStatus", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateMatchChanged", new Handler(), JsonObject.class, JsonObject.class);
        m_fieldProxy.on("updateMatchPlayStatus", new Handler(), JsonObject.class);
        m_fieldProxy.on("updateFieldNetworkStatus", new Handler(), JsonObject.class);
    }

    private class Handler implements SubscriptionHandler, SubscriptionHandler1<JsonObject> , SubscriptionHandler2<JsonObject, JsonObject> {

        @Override
        public void run(JsonObject jsonObject, JsonObject otherobject) {
            Log.d(Handler.class.getName(), "Object 1 " + jsonObject.toString());
            Log.d(Handler.class.getName(), "Object 2 " + otherobject.toString());
        }

        @Override
        public void run(JsonObject linkedTreeMap) {
            Log.d(Handler.class.getName(), "One Object " + linkedTreeMap.toString());
        }

        @Override
        public void run() {
            Log.d(Handler.class.getName(), "No object");
        }
    }
}
