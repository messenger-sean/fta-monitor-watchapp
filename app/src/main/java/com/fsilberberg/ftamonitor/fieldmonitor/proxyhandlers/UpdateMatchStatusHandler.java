package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * This is a general handler for all objects that are just updating the robot and match statuses,
 * and that implement SubscriptionHandler1
 */
public class UpdateMatchStatusHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    private final MatchStatus m_toUpdateTo;

    public UpdateMatchStatusHandler(String proxyMethod, MatchStatus toUpdateTo) {
        super(proxyMethod);
        m_toUpdateTo = toUpdateTo;
    }

    @Override
    public void run(JsonObject object) {
        updateMatchInfo(object);
        m_fieldStatus.setMatchStatus(m_toUpdateTo);
    }
}
