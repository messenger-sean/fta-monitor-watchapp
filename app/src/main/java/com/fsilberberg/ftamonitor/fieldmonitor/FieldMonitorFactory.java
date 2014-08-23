package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.IObserver;

/**
 * Created by 333fr_000 on 8/22/14.
 */
public class FieldMonitorFactory {

    private static FieldMonitorFactory instance = new FieldMonitorFactory();

    public static FieldMonitorFactory getInstance() {
        return instance;
    }

    // TODO: Retrieve these values from settings
    private FieldStatus fieldStatus = new FieldStatus(10, 140);

    public FieldStatus getFieldStatus() {
        return fieldStatus;
    }

    public void registerForUpdates(IObserver observer) {
        fieldStatus.registerObserver(observer);
    }
}
