package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler;

import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateMatchNotReadyHandler extends ProxyHandlerBase implements SubscriptionHandler {
    public UpdateMatchNotReadyHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run() {
        m_fieldStatus.setMatchStatus(NOT_READY);
    }
}
