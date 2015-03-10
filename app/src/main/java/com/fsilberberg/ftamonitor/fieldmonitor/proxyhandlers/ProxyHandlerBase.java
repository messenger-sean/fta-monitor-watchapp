package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Base class for all Proxy Handles that perform actions
 */
@SuppressWarnings("unused")
public abstract class ProxyHandlerBase {

    // FRC-Defined constants, located in FieldMonitor.js
    protected static final String ALLIANCE_FIELD = "P1";
    protected static final String STATION_FIELD = "P2";
    protected static final String TEAM_NUM_FIELD = "P3";
    protected static final String CONNECTION_FIELD = "P4";
    protected static final String LINK_ACT_FIELD = "P5";
    protected static final String DS_LINK_ACT_FIELD = "P6";
    protected static final String RADIO_FIELD = "P7";
    protected static final String RIO_FIELD = "P8";
    protected static final String ENABLED_FIELD = "P9";
    protected static final String AUTO_FIELD = "PA";
    protected static final String BYPASSED_FIELD = "PB";
    protected static final String ESTOP_PRESSED_FIELD = "PC";
    protected static final String ESTOPPED_FIELD = "PD";
    protected static final String BATTERY_FIELD = "PE";
    protected static final String MONITOR_FIELD = "PF";
    protected static final String AVERAGE_TRIP_FIELD = "PG";
    protected static final String LOST_PACK_FIELD = "PH";
    protected static final String SIGNAL_STRENGTH_FIELD = "PI";
    protected static final String SIGNAL_QUALITY_FIELD = "PJ";
    protected static final String DATA_RATE_FIELD = "PK";
    protected static final String DATA_RATE_TO_ROBOT_FIELD = "PL";
    protected static final String DATA_RATE_TO_DS_FIELD = "PM";
    protected static final String BWU_FIELD = "PN";
    protected static final int RED_ALLIANCE = 1;
    protected static final int BLUE_ALLIANCE = 2;
    protected static final int STATION_1 = 1;
    protected static final int STATION_2 = 2;
    protected static final int STATION_3 = 3;
    protected static final String ALLIANCE_ELEMENT = "Alliance";
    protected static final String STATION_ELEMENT = "Station";

    protected final Gson m_gson;

    protected ProxyHandlerBase() {
        m_gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    }
}
