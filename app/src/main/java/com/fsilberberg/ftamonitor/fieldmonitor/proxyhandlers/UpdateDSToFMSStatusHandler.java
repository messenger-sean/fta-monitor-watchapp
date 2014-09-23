package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import android.util.Log;

import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Handles the DStoFMSStatus Signalr Event
 */
public class UpdateDSToFMSStatusHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {

    private static final String BATTERY_ELEMENT = "Battery";
    private static final String DS_STATUS_ELEMENT = "DSStatus";
    private static final String ROUND_TRIP_ELEMENT = "AverageRoundTrip";
    private static final String DROPPED_PACKET_COUNT_ELEMENT = "DroppedPacketCount";
    private static final String DS_LINK_ACTIVE_ELEMENT = "DSLinkActive";
    private static final String RADIO_LINK_ELEMENT = "RadioLink";
    private static final String RIO_LINK_ELEMENT = "RIOLink";
    private static final String ENABLED_ELEMENT = "Enabled";
    private static final String LINK_ACTIVE_ELEMENT = "LinkActive";

    public UpdateDSToFMSStatusHandler(String proxyMethod) {
        super(proxyMethod);
    }

    @Override
    public void run(JsonObject object) {
        int alliance = object.get(ALLIANCE_ELEMENT).getAsInt();
        int station = object.get(STATION_ELEMENT).getAsInt();

        if (alliance == RED_ALLIANCE) {
            if (station == STATION_1) {
                updateStatus(m_fieldStatus.getRed1(), object);
            } else if (station == STATION_2) {
                updateStatus(m_fieldStatus.getRed2(), object);
            } else if (station == STATION_3) {
                updateStatus(m_fieldStatus.getRed3(), object);
            } else {
                Log.w(UpdateDSToFMSStatusHandler.class.getName(), "Unknown station number " + station);
            }
        } else if (alliance == BLUE_ALLIANCE) {
            if (station == STATION_1) {
                updateStatus(m_fieldStatus.getBlue1(), object);
                Log.d(UpdateDSToFMSStatusHandler.class.getName(), "Updating blue1");
            } else if (station == STATION_2) {
                updateStatus(m_fieldStatus.getBlue2(), object);
            } else if (station == STATION_3) {
                updateStatus(m_fieldStatus.getBlue3(), object);
            } else {
                Log.w(UpdateDSToFMSStatusHandler.class.getName(), "Unknown station number " + station);
            }
        } else {
            Log.w(UpdateDSToFMSStatusHandler.class.getName(), "Unrecognized alliance number " + alliance);
        }
    }

    private void updateStatus(TeamStatus team, JsonObject status) {
        JsonObject dsStatus = status.get(DS_STATUS_ELEMENT).getAsJsonObject();
        float battery = status.get(BATTERY_ELEMENT).getAsFloat();
        int roundTrip = dsStatus.get(ROUND_TRIP_ELEMENT).getAsInt();
        int missedPackets = dsStatus.get(DROPPED_PACKET_COUNT_ELEMENT).getAsInt();
        boolean dsLink = dsStatus.get(DS_LINK_ACTIVE_ELEMENT).getAsBoolean();
        boolean radio = dsStatus.get(RADIO_LINK_ELEMENT).getAsBoolean();
        boolean rio = dsStatus.get(RIO_LINK_ELEMENT).getAsBoolean();
        boolean enabled = dsStatus.get(ENABLED_ELEMENT).getAsBoolean();
        boolean codeLoaded = dsStatus.get(LINK_ACTIVE_ELEMENT).getAsBoolean();

        team.setBattery(battery);
        team.setRoundTrip(roundTrip);
        team.setDroppedPackets(missedPackets);
        team.setDs(dsLink);
        team.setRadio(radio);
        team.setRobot(rio);
        team.setEnabled(enabled);
        team.setCode(codeLoaded);
    }
}
