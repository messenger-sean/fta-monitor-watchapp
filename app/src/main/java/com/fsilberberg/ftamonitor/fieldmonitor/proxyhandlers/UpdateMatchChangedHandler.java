package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;

/**
 * This is an event handler for the updateMatchChanged signalr event
 */
public class UpdateMatchChangedHandler extends ProxyHandlerBase implements SubscriptionHandler2<JsonObject, JsonObject> {

    public UpdateMatchChangedHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject event, JsonObject match) {
        updateMatchInfo(match);
    }
}
