package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;

/**
 * This interface is implemented by all "services" that need to be run by the {@link MainForegroundService}.
 */
public interface IForegroundService {

    /**
     * Starts the service in the given context.
     *
     * @param context The context for the new service to use
     */
    public void startService(final Context context, final Intent intent);

    /**
     * Stops the service from running
     */
    public void stopService();

}
