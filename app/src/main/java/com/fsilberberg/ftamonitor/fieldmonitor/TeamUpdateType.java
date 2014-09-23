package com.fsilberberg.ftamonitor.fieldmonitor;

/**
 * If you want to subscribe to team update events, you need to implement the {@link com.fsilberberg.ftamonitor.common.IObserver}
 * interface with this as the generic type
 */
public enum TeamUpdateType {
    TEAM_NUMBER,
    DROPPED_PACKETS,
    ROUND_TRIP,
    DS_ETH,
    DS,
    RADIO,
    ROBOT,
    ESTOP,
    CODE,
    BATTERY,
    DATA_RATE,
    SIGNAL_STRENGTH,
    SIGNAL_QUALITY,
    ENABLED,
    BYPASSED,
    CARD
}
