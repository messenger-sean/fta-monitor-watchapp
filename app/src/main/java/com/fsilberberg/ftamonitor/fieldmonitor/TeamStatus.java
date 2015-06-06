package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Observer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Encapsulates the current status of one of the alliance team members. There are however many of these
 * per alliance accessible through the {@link com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory}.
 * This class should never be instantiated directly outside this package
 */
public class TeamStatus {
    private Integer m_teamNumber = 1;
    private Integer m_droppedPackets = 0;
    private Integer m_roundTrip = 0;
    private Boolean m_dsEth = false;
    private Boolean m_ds = false;
    private Boolean m_radio = false;
    private Boolean m_rio = false;
    private Boolean m_estop = false;
    private Boolean m_code = false;
    private Float m_battery = 0.0f;
    private Float m_dataRate = 0.0f;
    private Float m_signalStrength = 0.0f;
    private Float m_signalQuality = 0.0f;
    private Boolean m_enabled = false;
    private Boolean m_bypassed = false;

    public TeamStatus() {
    }

    private final Collection<Observer<UpdateType>> m_observers = new ArrayList<>();

    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        m_teamNumber = teamNumber;
    }

    public synchronized int getDroppedPackets() {
        return m_droppedPackets;
    }

    public void setDroppedPackets(int droppedPackets) {
        m_droppedPackets = droppedPackets;
    }

    public synchronized int getRoundTrip() {
        return m_roundTrip;
    }

    public void setRoundTrip(int roundTrip) {
        m_roundTrip = roundTrip;
    }

    public synchronized boolean isDsEth() {
        return m_dsEth;
    }

    public void setDsEth(boolean dsEth) {
        m_dsEth = dsEth;
    }

    public synchronized boolean isDs() {
        return m_ds;
    }

    public void setDs(boolean ds) {
        m_ds = ds;
    }

    public synchronized boolean isRadio() {
        return m_radio;
    }

    public void setRadio(boolean radio) {
        m_radio = radio;
    }

    public synchronized boolean isRio() {
        return m_rio;
    }

    public void setRobot(boolean robot) {
        m_rio = robot;
    }

    public synchronized boolean isEstop() {
        return m_estop;
    }

    public void setEstop(boolean estop) {
        m_estop = estop;
    }

    public synchronized boolean isCode() {
        return m_code;
    }

    public void setCode(boolean code) {
        m_code = code;
    }

    public synchronized float getBattery() {
        return m_battery;
    }

    public void setBattery(float battery) {
        m_battery = battery;
    }

    public synchronized float getDataRate() {
        return m_dataRate;
    }

    public void setDataRate(float dataRate) {
        m_dataRate = dataRate;
    }

    public synchronized float getSignalStrength() {
        return m_signalStrength;
    }

    public void setSignalStrength(float signalStrength) {
        m_signalStrength = signalStrength;
    }

    public synchronized float getSignalQuality() {
        return m_signalQuality;
    }

    public void setSignalQuality(float signalQuality) {
        m_signalQuality = signalQuality;
    }

    public synchronized boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    public synchronized boolean isBypassed() {
        return m_bypassed;
    }

    public void setBypassed(boolean bypassed) {
        m_bypassed = bypassed;
    }

    public void registerObserver(Observer<UpdateType> observer) {
        m_observers.add(observer);
    }

    public void unregisterObserver(Observer<UpdateType> observer) {
        m_observers.remove(observer);
    }

    public void updateObservers() {
        for (Observer<UpdateType> observer : m_observers) {
            observer.update(UpdateType.TEAM);
        }
    }
}
