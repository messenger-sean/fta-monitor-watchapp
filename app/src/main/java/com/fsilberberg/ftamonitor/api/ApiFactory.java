package com.fsilberberg.ftamonitor.api;

import com.fsilberberg.ftamonitor.api.firstapi.FIRSTApi;

/**
 * Factory for obtaining references to the correct {@link com.fsilberberg.ftamonitor.api.Api} implementation
 */
public final class ApiFactory {

    private static ApiFactory instance = new ApiFactory();

    public static ApiFactory getInstance() {
        return instance;
    }

    private FIRSTApi m_firstApi;

    private ApiFactory() {
        m_firstApi = new FIRSTApi();
    }

    public Api getApi() {
        return m_firstApi;
    }

}
