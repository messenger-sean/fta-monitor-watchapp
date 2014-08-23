package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Created by Fredric on 8/22/14.
 */
public class NoopHandler implements SubscriptionHandler1<JsonObject> {
    @Override
    public void run(JsonObject object) {

    }
}
