package com.fsilberberg.ftamonitor.fieldmonitor;

/**
 * If you want to subscribe to field update events, you need to implement the {@link com.fsilberberg.ftamonitor.common.IObserver}
 * interface with this as the generic type
 */
public enum FieldUpdateType {
    MATCH_NUMBER,
    MATCH_STATUS,
    AUTO_TIME,
    TELEOP_TIME
}
