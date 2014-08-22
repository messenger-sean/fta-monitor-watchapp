package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchInfo;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

import static com.fsilberberg.ftamonitor.common.MatchStatus.READY_TO_PRESTART;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateMatchReadyToPrestartHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    protected UpdateMatchReadyToPrestartHandler(String proxyMethod) {
        super(proxyMethod);
    }


    @Override
    public void run(JsonObject jsonObject) {
        MatchInfo info = m_gson.fromJson(jsonObject, MatchInfo.class);
        m_fieldStatus.updateMatchInfo(info);
        m_fieldStatus.setMatchStatus(READY_TO_PRESTART);
    }
}
