package com.fsilberberg.ftamonitor.common;

/**
 * Created by Fredric on 8/23/14.
 */
public interface IObservable {
    void registerObserver(IObserver observer);

    void deregisterObserver(IObserver observer);
}
