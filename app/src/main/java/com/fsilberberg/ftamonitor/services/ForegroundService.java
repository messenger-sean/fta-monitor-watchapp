package com.fsilberberg.ftamonitor.services;

import android.content.Context;

/**
 * This interface is implemented by all of the services managed by {@link com.fsilberberg.ftamonitor.services.FieldServiceManager}
 */
interface ForegroundService {

    /**
     * Starts the service in the given context.
     *
     * @param context The context for the new service to use
     */
    public void startService(final Context context);

    /**
     * Stops the service from running
     */
    public void stopService();

}
