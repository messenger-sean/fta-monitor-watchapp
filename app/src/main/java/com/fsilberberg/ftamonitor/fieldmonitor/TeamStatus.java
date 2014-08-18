package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.RobotStatus;

import static com.fsilberberg.ftamonitor.common.RobotStatus.*;

/**
 * Created by Fredric on 8/17/14.
 */
public class TeamStatus {
    private int teamNumber = 1;
    private boolean dsEth = false;
    private boolean ds = false;
    private boolean radio = false;
    private boolean robot = false;
    private boolean estop = false;
    private float dataRate = 0.0f;
    private float signalStrength = 0.0f;
    private float signalQuality = 0.0f;
    private RobotStatus robotStatus = DISABLED;

    public synchronized int getTeamNumber() {
        return teamNumber;
    }

    public synchronized void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public synchronized boolean isDsEth() {
        return dsEth;
    }

    public synchronized void setDsEth(boolean dsEth) {
        this.dsEth = dsEth;
    }

    public synchronized boolean isDs() {
        return ds;
    }

    public synchronized void setDs(boolean ds) {
        this.ds = ds;
    }

    public synchronized boolean isRadio() {
        return radio;
    }

    public synchronized void setRadio(boolean radio) {
        this.radio = radio;
    }

    public synchronized boolean isRobot() {
        return robot;
    }

    public synchronized void setRobot(boolean robot) {
        this.robot = robot;
    }

    public synchronized boolean isEstop() {
        return estop;
    }

    public synchronized void setEstop(boolean estop) {
        this.estop = estop;
    }

    public synchronized float getDataRate() {
        return dataRate;
    }

    public synchronized void setDataRate(float dataRate) {
        this.dataRate = dataRate;
    }

    public synchronized float getSignalStrength() {
        return signalStrength;
    }

    public synchronized void setSignalStrength(float signalStrength) {
        this.signalStrength = signalStrength;
    }

    public synchronized float getSignalQuality() {
        return signalQuality;
    }

    public synchronized void setSignalQuality(float signalQuality) {
        this.signalQuality = signalQuality;
    }

    public synchronized RobotStatus getRobotStatus() {
        return robotStatus;
    }

    public synchronized void setRobotStatus(RobotStatus robotStatus) {
        this.robotStatus = robotStatus;
    }
}
