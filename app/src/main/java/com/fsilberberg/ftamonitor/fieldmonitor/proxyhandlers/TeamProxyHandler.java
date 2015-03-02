package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Implements proxy handling for the 2015 api
 */
public class TeamProxyHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonArray> {

    private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_red1 = m_fieldStatus.getRed1();
    private final TeamStatus m_red2 = m_fieldStatus.getRed2();
    private final TeamStatus m_red3 = m_fieldStatus.getRed3();
    private final TeamStatus m_blue1 = m_fieldStatus.getBlue1();
    private final TeamStatus m_blue2 = m_fieldStatus.getBlue2();
    private final TeamStatus m_blue3 = m_fieldStatus.getBlue3();

    @Override
    public void run(JsonArray jsonElements) {
        for (JsonElement element : jsonElements) {
            JsonObject object = element.getAsJsonObject();
            switch (object.get(ALLIANCE_ELEMENT).getAsInt()) {
                case 1:
                    switch (object.get(STATION_ELEMENT).getAsInt()) {
                        case 1:
                            updateTeam(m_red1, object);
                            break;
                        case 2:
                            updateTeam(m_red2, object);
                            break;
                        case 3:
                            updateTeam(m_red3, object);
                            break;
                    }
                    break;
                case 2:
                    switch (object.get(STATION_ELEMENT).getAsInt()) {
                        case 1:
                            updateTeam(m_blue1, object);
                            break;
                        case 2:
                            updateTeam(m_blue2, object);
                            break;
                        case 3:
                            updateTeam(m_blue3, object);
                            break;
                    }
            }
        }
    }

    public void updateTeam(TeamStatus status, JsonObject newStatus) {
        status.setTeamNumber(newStatus.get(TEAM_NUM_FIELD).getAsInt());
        status.setDsEth(newStatus.get(CONNECTION_FIELD).getAsBoolean());
        status.setCode(!newStatus.get(LINK_ACT_FIELD).getAsBoolean());
        status.setDs(newStatus.get(DS_LINK_ACT_FIELD).getAsBoolean());
        status.setRadio(newStatus.get(RADIO_FIELD).getAsBoolean());
        status.setDataRate(newStatus.get(DATA_RATE_FIELD).getAsFloat());
        status.setSignalStrength(newStatus.get(SIGNAL_STRENGTH_FIELD).getAsFloat());
        status.setSignalQuality(newStatus.get(SIGNAL_QUALITY_FIELD).getAsFloat());
        status.setBattery(newStatus.get(BATTERY_FIELD).getAsFloat());
        status.setRoundTrip(newStatus.get(AVERAGE_TRIP_FIELD).getAsInt());
        status.setDroppedPackets(newStatus.get(LOST_PACK_FIELD).getAsInt());
        status.setRobot(newStatus.get(RIO_FIELD).getAsBoolean());
        status.setBypassed(newStatus.get(BYPASSED_FIELD).getAsBoolean());
        switch (newStatus.get(ENABLED_FIELD).getAsInt()) {
            case 1: // Estop
                status.setEstop(true);
                status.setEnabled(false);
                break;
            case 2: // Disabled Auto
            case 3: // Disabled Teleop
                status.setEstop(false);
                status.setEnabled(false);
                break;
            case 4: // Enabled Auto
            case 5: // Enabled Teleop
                status.setEnabled(true);
                status.setEstop(false);
                break;
        }
        status.updateObservers();
    }
}
