package com.fsilberberg.ftamonitor.fieldmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.ErrorCallback;
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

    public static final String URL_INTENT_EXTRA = "url_intent_extra";

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
}
