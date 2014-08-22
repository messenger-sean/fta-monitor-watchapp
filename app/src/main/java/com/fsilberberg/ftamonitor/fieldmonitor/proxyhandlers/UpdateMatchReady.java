package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

import static com.fsilberberg.ftamonitor.common.MatchStatus.MATCH_READY;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateMatchReady extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {
    protected UpdateMatchReady(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject object) {
        updateMatchInfo(object);
        m_fieldStatus.setMatchStatus(MATCH_READY);
    }
}
