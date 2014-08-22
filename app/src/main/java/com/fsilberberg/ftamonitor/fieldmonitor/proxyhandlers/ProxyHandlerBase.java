package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Fredric on 8/22/14.
 */
public abstract class ProxyHandlerBase {

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

}
