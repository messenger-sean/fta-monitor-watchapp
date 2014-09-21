package com.fsilberberg.ftamonitor.common;

/**
 * This is a generic observable implementation that allows for the observer pattern with an enum
 * as the update data type. This is meant for essentially sending URI's, not full data. This only
 * tells the observer what it needs to update, not the updated data
 *
 * @author Fredric
 */
public interface IObservable<E extends Enum<E>> {
    void registerObserver(IObserver<E> observer);
    void deregisterObserver(IObserver<E> observer);
}
