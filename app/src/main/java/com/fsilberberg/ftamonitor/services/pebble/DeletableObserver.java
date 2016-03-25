package com.fsilberberg.ftamonitor.services.pebble;

/**
 * Simple interface to allow all observers to be managed in the same list.
 */
public interface DeletableObserver {
    void checkUpdate();

    void unregister();
}
