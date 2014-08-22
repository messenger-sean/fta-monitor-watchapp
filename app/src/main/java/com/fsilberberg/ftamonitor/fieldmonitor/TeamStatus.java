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
    private boolean m_dsEth = false;
    private boolean m_ds = false;
    private boolean m_radio = false;
    private boolean m_robot = false;
    private boolean m_estop = false;
    private float m_dataRate = 0.0f;
    private float m_signalStrength = 0.0f;
    private float m_signalQuality = 0.0f;
    private RobotStatus m_robotStatus = DISABLED;
    private Card m_card = NONE;

    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public synchronized void setTeamNumber(int teamNumber) {
        this.m_teamNumber = teamNumber;
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

    public synchronized RobotStatus getRobotStatus() {
        return m_robotStatus;
    }

    public synchronized void setRobotStatus(RobotStatus robotStatus) {
        this.m_robotStatus = robotStatus;
    }

    public synchronized Card getCard() {
        return m_card;
    }

    public synchronized void setCard(Card card) {
        m_card = card;
    }
}
