package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Card;
import com.fsilberberg.ftamonitor.common.RobotStatus;

import static com.fsilberberg.ftamonitor.common.Card.*;
import static com.fsilberberg.ftamonitor.common.RobotStatus.*;

/**
 * Created by Fredric on 8/17/14.
 */
public class TeamStatus {
    private int m_teamNumber = 1;
    private int m_droppedPackets = 0;
    private int m_roundTrip = 0;
    private boolean m_dsEth = false;
    private boolean m_ds = false;
    private boolean m_radio = false;
    private boolean m_robot = false;
    private boolean m_estop = false;
    private boolean m_code = false;
    private float m_battery = 0.0f;
    private float m_dataRate = 0.0f;
    private float m_signalStrength = 0.0f;
    private float m_signalQuality = 0.0f;
    private boolean m_enabled = false;
    private boolean m_bypassed = false;
    private Card m_card = NONE;

    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public synchronized void setTeamNumber(int teamNumber) {
        this.m_teamNumber = teamNumber;
    }

    public synchronized int getDroppedPackets() {
        return m_droppedPackets;
    }

    public synchronized void setDroppedPackets(int droppedPackets) {
        m_droppedPackets = droppedPackets;
    }

    public synchronized int getRoundTrip() {
        return m_roundTrip;
    }

    public synchronized void setRoundTrip(int roundTrip) {
        m_roundTrip = roundTrip;
    }

    public synchronized boolean isDsEth() {
        return m_dsEth;
    }

    public synchronized void setDsEth(boolean dsEth) {
        this.m_dsEth = dsEth;
    }

    public synchronized boolean isDs() {
        return m_ds;
    }

    public synchronized void setDs(boolean ds) {
        this.m_ds = ds;
    }

    public synchronized boolean isRadio() {
        return m_radio;
    }

    public synchronized void setRadio(boolean radio) {
        this.m_radio = radio;
    }

    public synchronized boolean isRobot() {
        return m_robot;
    }

    public synchronized void setRobot(boolean robot) {
        this.m_robot = robot;
    }

    public synchronized boolean isEstop() {
        return m_estop;
    }

    public synchronized void setEstop(boolean estop) {
        this.m_estop = estop;
    }

    public synchronized boolean isCode() {
        return m_code;
    }

    public synchronized void setCode(boolean code) {
        m_code = code;
    }

    public synchronized float getBattery() {
        return m_battery;
    }

    public synchronized void setBattery(float battery) {
        m_battery = battery;
    }

    public synchronized float getDataRate() {
        return m_dataRate;
    }

    public synchronized void setDataRate(float dataRate) {
        this.m_dataRate = dataRate;
    }

    public synchronized float getSignalStrength() {
        return m_signalStrength;
    }

    public synchronized void setSignalStrength(float signalStrength) {
        this.m_signalStrength = signalStrength;
    }

    public synchronized float getSignalQuality() {
        return m_signalQuality;
    }

    public synchronized void setSignalQuality(float signalQuality) {
        this.m_signalQuality = signalQuality;
    }

    public synchronized boolean isEnabled() {
        return m_enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    public synchronized boolean isBypassed() {
        return m_bypassed;
    }

    public synchronized void setBypassed(boolean bypassed) {
        m_bypassed = bypassed;
    }

    public synchronized Card getCard() {
        return m_card;
    }

    public synchronized void setCard(Card card) {
        m_card = card;
    }
}
