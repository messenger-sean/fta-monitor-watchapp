package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchPlayInfo;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateMatchPlayStatusHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    public UpdateMatchPlayStatusHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject object) {
        MatchPlayInfo info = m_gson.fromJson(object, MatchPlayInfo.class);
        if (info.getMatchInfo() != null) {
            m_fieldStatus.updateMatchInfo(info.getMatchInfo());
        }
    }
}
