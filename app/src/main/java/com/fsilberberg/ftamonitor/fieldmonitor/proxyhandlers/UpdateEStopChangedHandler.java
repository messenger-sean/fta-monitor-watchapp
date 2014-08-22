package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import android.util.Log;

import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Created by Fredric on 8/22/14.
 */
public class UpdateEStopChangedHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    // FRC-Defined constants, located in FieldMonitor.js
    private static int RED_ALLIANCE = 1;
    private static int BLUE_ALLIANCE = 2;
    private static int STATION_1 = 1;
    private static int STATION_2 = 2;
    private static int STATION_3 = 3;
    private static String ALLIANCE_ELEMENT = "Alliance";
    private static String PRESSED_ELEMENT = "Pressed";
    private static String STATION_ELEMENT = "Station";

    protected UpdateEStopChangedHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject object) {
        int alliance = object.get(ALLIANCE_ELEMENT).getAsInt();
        int station = object.get(STATION_ELEMENT).getAsInt();
        boolean pressed = object.get(PRESSED_ELEMENT).getAsBoolean();
        if (alliance == RED_ALLIANCE) {
            if (station == STATION_1) {
                m_fieldStatus.getRed1().setEstop(pressed);
            } else if (station == STATION_2) {
                m_fieldStatus.getRed2().setEstop(pressed);
            } else if (station == STATION_3) {
                m_fieldStatus.getRed3().setEstop(pressed);
            } else {
                Log.w(UpdateEStopChangedHandler.class.getName(), "Unknown station number " + station);
            }
        } else if (alliance == BLUE_ALLIANCE) {
            if (station == STATION_1) {
                m_fieldStatus.getBlue1().setEstop(pressed);
            } else if (station == STATION_2) {
                m_fieldStatus.getBlue2().setEstop(pressed);
            } else if (station == STATION_3) {
                m_fieldStatus.getBlue3().setEstop(pressed);
            } else {
                Log.w(UpdateEStopChangedHandler.class.getName(), "Unknown station number " + station);
            }
        } else {
            Log.w(UpdateEStopChangedHandler.class.getName(), "Unknown alliance number " + alliance);
        }
    }
}
