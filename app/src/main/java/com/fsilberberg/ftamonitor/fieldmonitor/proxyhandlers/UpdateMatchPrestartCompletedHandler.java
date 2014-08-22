package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_COMPLETED;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateMatchPrestartCompletedHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    protected UpdateMatchPrestartCompletedHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject object) {
        updateMatchInfo(object);
        m_fieldStatus.setMatchStatus(PRESTART_COMPLETED);
    }
}
