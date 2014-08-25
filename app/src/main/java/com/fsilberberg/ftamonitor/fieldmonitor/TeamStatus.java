package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Card;

import java.util.ArrayList;
import java.util.Collection;

import static com.fsilberberg.ftamonitor.common.Card.*;
import static com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType.*;

/**
 * Created by Fredric on 8/17/14.
 */
public class TeamStatus {
    private Integer m_teamNumber = 1;
    private Integer m_droppedPackets = 0;
    private Integer m_roundTrip = 0;
    private Boolean m_dsEth = false;
    private Boolean m_ds = false;
    private Boolean m_radio = false;
    private Boolean m_robot = false;
    private Boolean m_estop = false;
    private Boolean m_code = false;
    private Float m_battery = 0.0f;
    private Float m_dataRate = 0.0f;
    private Float m_signalStrength = 0.0f;
    private Float m_signalQuality = 0.0f;
    private Boolean m_enabled = false;
    private Boolean m_bypassed = false;
    private Card m_card = NONE;

    private final int m_stationNum;
    private final Alliance m_alliance;

    public TeamStatus(int m_stationNum, Alliance m_alliance) {
        this.m_stationNum = m_stationNum;
        this.m_alliance = m_alliance;
    }

    private final Collection<IFieldMonitorObserver> m_observers = new ArrayList<>();

    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        syncAndUpdateObservers(m_teamNumber, teamNumber, TEAM_NUMBER);
    }

    public synchronized int getDroppedPackets() {
        return m_droppedPackets;
    }

    public void setDroppedPackets(int droppedPackets) {
        syncAndUpdateObservers(m_droppedPackets, droppedPackets, DROPPED_PACKETS);
    }

    public synchronized int getRoundTrip() {
        return m_roundTrip;
    }

    public void setRoundTrip(int roundTrip) {
        syncAndUpdateObservers(m_roundTrip, roundTrip, ROUND_TRIP);
    }

    public synchronized boolean isDsEth() {
        return m_dsEth;
    }

    public void setDsEth(boolean dsEth) {
        syncAndUpdateObservers(m_dsEth, dsEth, DS_ETH);
    }

    public synchronized boolean isDs() {
        return m_ds;
    }

    public void setDs(boolean ds) {
        syncAndUpdateObservers(m_ds, ds, DS);
    }

    public synchronized boolean isRadio() {
        return m_radio;
    }

    public void setRadio(boolean radio) {
        syncAndUpdateObservers(m_radio, radio, RADIO);
    }

    public synchronized boolean isRobot() {
        return m_robot;
    }

    public void setRobot(boolean robot) {
        syncAndUpdateObservers(m_robot, robot, ROBOT);
    }

    public synchronized boolean isEstop() {
        return m_estop;
    }

    public void setEstop(boolean estop) {
        syncAndUpdateObservers(m_estop, estop, ESTOP);
    }

    public synchronized boolean isCode() {
        return m_code;
    }

    public void setCode(boolean code) {
        syncAndUpdateObservers(m_code, code, CODE);
    }

    public synchronized float getBattery() {
        return m_battery;
    }

    public void setBattery(float battery) {
        syncAndUpdateObservers(m_battery, battery, BATTERY);
    }

    public synchronized float getDataRate() {
        return m_dataRate;
    }

    public void setDataRate(float dataRate) {
        syncAndUpdateObservers(m_dataRate, dataRate, DATA_RATE);
    }

    public synchronized float getSignalStrength() {
        return m_signalStrength;
    }

    public void setSignalStrength(float signalStrength) {
        syncAndUpdateObservers(m_signalStrength, signalStrength, SIGNAL_STRENGTH);
    }

    public synchronized float getSignalQuality() {
        return m_signalQuality;
    }

    public void setSignalQuality(float signalQuality) {
        syncAndUpdateObservers(m_signalQuality, signalQuality, SIGNAL_QUALITY);
    }

    public synchronized boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        syncAndUpdateObservers(m_enabled, enabled, ENABLED);
    }

    public synchronized boolean isBypassed() {
        return m_bypassed;
    }

    public void setBypassed(boolean bypassed) {
        syncAndUpdateObservers(m_bypassed, bypassed, BYPASSED);
    }

    public synchronized Card getCard() {
        return m_card;
    }

    public void setCard(Card card) {
        syncAndUpdateObservers(m_card, card, CARD);
    }

    public void registerObserver(IFieldMonitorObserver observer) {
        m_observers.add(observer);
    }

    public void deregisterObserver(IFieldMonitorObserver observer) {
        m_observers.remove(observer);
    }

    public void updateObservers(TeamUpdateType updateType) {
        for (IFieldMonitorObserver observer : m_observers) {
            observer.update(updateType, m_teamNumber, m_alliance);
        }
    }

    private void syncAndUpdateObservers(Object oldVal, Object newVal, TeamUpdateType updateType) {
        boolean set = false;

        synchronized (this) {
            if (!oldVal.equals(newVal)) {
                oldVal = newVal;
                set = true;
            }
        }

        if (set) {
            updateObservers(updateType);
        }
    }
}
