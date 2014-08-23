package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import android.util.Log;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateStationConnectionChangedHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    private static final String CONNECTED_ELEMENT = "Connected";

    public UpdateStationConnectionChangedHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject object) {
        int alliance = object.get(ALLIANCE_ELEMENT).getAsInt();
        int station = object.get(STATION_ELEMENT).getAsInt();
        boolean connected = object.get(CONNECTED_ELEMENT).getAsBoolean();

        if (alliance == RED_ALLIANCE) {
            if (station == STATION_1) {
                m_fieldStatus.getRed1().setDsEth(connected);
            } else if (station == STATION_2) {
                m_fieldStatus.getRed2().setDsEth(connected);
            } else if (station == STATION_3) {
                m_fieldStatus.getRed3().setDsEth(connected);
            } else {
                Log.w(UpdateStationConnectionChangedHandler.class.getName(), "Unrecognized station number " + station);
            }
        } else if (alliance == BLUE_ALLIANCE) {
            if (station == STATION_1) {
                m_fieldStatus.getBlue1().setDsEth(connected);
            } else if (station == STATION_2) {
                m_fieldStatus.getBlue2().setDsEth(connected);
            } else if (station == STATION_3) {
                m_fieldStatus.getBlue3().setDsEth(connected);
            } else {
                Log.w(UpdateStationConnectionChangedHandler.class.getName(), "Unrecognized station number " + station);
            }
        } else {
            Log.w(UpdateStationConnectionChangedHandler.class.getName(), "Unrecognized alliance number " + alliance);
        }
    }
}
