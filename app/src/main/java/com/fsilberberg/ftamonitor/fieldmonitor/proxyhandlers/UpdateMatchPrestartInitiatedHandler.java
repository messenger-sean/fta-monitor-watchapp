package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_INITIATED;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateMatchPrestartInitiatedHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    protected UpdateMatchPrestartInitiatedHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject jsonObject) {
        updateMatchInfo(jsonObject);
        m_fieldStatus.setMatchStatus(PRESTART_INITIATED);
    }
}
