package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import android.util.Log;

import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Handles the field network status event
 */
public class UpdateFieldNetworkStatusHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonArray> {

    private static final String DATA_RATE_ELEMENT = "DataRateTotal";
    private static final String SIGNAL_STRENGTH_ELEMENT = "SignalStrength";
    private static final String SIGNAL_QUALITY_ELEMENT = "SignalQuality";

    public UpdateFieldNetworkStatusHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonArray jsonArray) {
        for (JsonElement networkStatus : jsonArray) {
            JsonObject networkStatusObject = networkStatus.getAsJsonObject();
            int alliance = networkStatusObject.get(ALLIANCE_ELEMENT).getAsInt();
            int station = networkStatusObject.get(STATION_ELEMENT).getAsInt();
            if (alliance == RED_ALLIANCE) {
                if (station == STATION_1) {
                    setNetworkStatus(m_fieldStatus.getRed1(), networkStatusObject);
                } else if (station == STATION_2) {
                    setNetworkStatus(m_fieldStatus.getRed2(), networkStatusObject);
                } else if (station == STATION_3) {
                    setNetworkStatus(m_fieldStatus.getRed3(), networkStatusObject);
                } else {
                    Log.w(UpdateFieldNetworkStatusHandler.class.getName(), "Unknown station number " + station);
                }
            } else if (alliance == BLUE_ALLIANCE) {
                if (station == STATION_1) {
                    setNetworkStatus(m_fieldStatus.getBlue1(), networkStatusObject);
                } else if (station == STATION_2) {
                    setNetworkStatus(m_fieldStatus.getBlue2(), networkStatusObject);
                } else if (station == STATION_3) {
                    setNetworkStatus(m_fieldStatus.getBlue3(), networkStatusObject);
                } else {
                    Log.w(UpdateFieldNetworkStatusHandler.class.getName(), "Unknown station number " + station);
                }
            } else {
                Log.w(UpdateFieldNetworkStatusHandler.class.getName(), "Unknown alliance number " + alliance);
            }
        }
    }

    private void setNetworkStatus(TeamStatus status, JsonObject networkStatus) {
        status.setDataRate(networkStatus.get(DATA_RATE_ELEMENT).getAsFloat());
        status.setSignalStrength(networkStatus.get(SIGNAL_STRENGTH_ELEMENT).getAsFloat());
        status.setSignalQuality(networkStatus.get(SIGNAL_QUALITY_ELEMENT).getAsFloat());
    }
}
