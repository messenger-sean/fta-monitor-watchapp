package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.JsonObject;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * O Proxy handler type that performs no operations
 */
public class NoopHandler implements SubscriptionHandler1<JsonObject> {
    @Override
    public void run(JsonObject object) {

    }
}
