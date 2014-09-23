package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchInfo;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Base class for all Proxy Handles that perform actions
 */
public abstract class ProxyHandlerBase {
    // FRC-Defined constants, located in FieldMonitor.js
    protected static final int RED_ALLIANCE = 1;
    protected static final int BLUE_ALLIANCE = 2;
    protected static final int STATION_1 = 1;
    protected static final int STATION_2 = 2;
    protected static final int STATION_3 = 3;
    protected static final String ALLIANCE_ELEMENT = "Alliance";
    protected static final String STATION_ELEMENT = "Station";

    protected final String m_proxyMethod;
    protected final FieldStatus m_fieldStatus;
    protected final Gson m_gson;

    protected ProxyHandlerBase(String proxyMethod) {
        m_proxyMethod = proxyMethod;
        m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        m_gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    }

    public String getProxyMethod() {
        return m_proxyMethod;
    }

    protected void updateMatchInfo(JsonObject jsonObject) {
        MatchInfo info = m_gson.fromJson(jsonObject, MatchInfo.class);
        m_fieldStatus.updateMatchInfo(info);
    }

}
