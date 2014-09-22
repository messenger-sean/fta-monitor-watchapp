package com.fsilberberg.ftamonitor.fieldmonitor;

import android.util.Log;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Card;
import com.fsilberberg.ftamonitor.common.IObserver;

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

    private final Collection<IObserver<TeamUpdateType>> m_observers = new ArrayList<>();

    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        boolean set = false;

        synchronized (this) {
            if (!m_teamNumber.equals(teamNumber)) {
                m_teamNumber = teamNumber;
                set = true;
            }
        }

        if (set) {
            updateObservers(TEAM_NUMBER);
        }
    }

    public synchronized int getDroppedPackets() {
        return m_droppedPackets;
    }

    public void setDroppedPackets(int droppedPackets) {
        boolean set = false;

        synchronized (this) {
            if (!m_droppedPackets.equals(droppedPackets)) {
                m_droppedPackets = droppedPackets;
                set = true;
            }
        }

        if (set) {
            updateObservers(DROPPED_PACKETS);
        }
    }

    public synchronized int getRoundTrip() {
        return m_roundTrip;
    }

    public void setRoundTrip(int roundTrip) {
        boolean set = false;

        synchronized (this) {
            if (!m_roundTrip.equals(roundTrip)) {
                m_roundTrip = roundTrip;
                set = true;
            }
        }

        if (set) {
            updateObservers(ROUND_TRIP);
        }
    }

    public synchronized boolean isDsEth() {
        return m_dsEth;
    }

    public void setDsEth(boolean dsEth) {
        boolean set = false;

        synchronized (this) {
            if (!m_dsEth.equals(dsEth)) {
                m_dsEth = dsEth;
                set = true;
            }
        }

        if (set) {
            updateObservers(DS_ETH);
        }
    }

    public synchronized boolean isDs() {
        return m_ds;
    }

    public void setDs(boolean ds) {
        boolean set = false;

        synchronized (this) {
            if (!m_ds.equals(ds)) {
                m_ds = ds;
                set = true;
            }
        }

        if (set) {
            updateObservers(DS);
        }
    }

    public synchronized boolean isRadio() {
        return m_radio;
    }

    public void setRadio(boolean radio) {
        boolean set = false;

        synchronized (this) {
            if (!m_radio.equals(radio)) {
                m_radio = radio;
                set = true;
            }
        }

        if (set) {
            updateObservers(RADIO);
        }
    }

    public synchronized boolean isRobot() {
        return m_robot;
    }

    public void setRobot(boolean robot) {
        boolean set = false;

        synchronized (this) {
            if (!m_robot.equals(robot)) {
                m_robot = robot;
                set = true;
            }
        }

        if (set) {
            updateObservers(ROBOT);
        }
    }

    public synchronized boolean isEstop() {
        return m_estop;
    }

    public void setEstop(boolean estop) {
        boolean set = false;

        synchronized (this) {
            if (!m_estop.equals(estop)) {
                m_enabled = estop;
                set = true;
            }
        }

        if (set) {
            updateObservers(ESTOP);
        }
    }

    public synchronized boolean isCode() {
        return m_code;
    }

    public void setCode(boolean code) {
        boolean set = false;

        synchronized (this) {
            if (!m_code.equals(code)) {
                m_code = code;
                set = true;
            }
        }

        if (set) {
            updateObservers(CODE);
        }
    }

    public synchronized float getBattery() {
        return m_battery;
    }

    public void setBattery(float battery) {
        boolean set = false;

        synchronized (this) {
            if (!m_battery.equals(battery)) {
                m_battery = battery;
                set = true;
            }
        }

        if (set) {
            updateObservers(BATTERY);
        }
    }

    public synchronized float getDataRate() {
        return m_dataRate;
    }

    public void setDataRate(float dataRate) {
        boolean set = false;

        synchronized (this) {
            if (!m_dataRate.equals(dataRate)) {
                m_dataRate = dataRate;
                set = true;
            }
        }

        if (set) {
            updateObservers(DATA_RATE);
        }
    }

    public synchronized float getSignalStrength() {
        return m_signalStrength;
    }

    public void setSignalStrength(float signalStrength) {
        boolean set = false;

        synchronized (this) {
            if (!m_signalStrength.equals(signalStrength)) {
                m_signalStrength = signalStrength;
                set = true;
            }
        }

        if (set) {
            updateObservers(SIGNAL_STRENGTH);
        }
    }

    public synchronized float getSignalQuality() {
        return m_signalQuality;
    }

    public void setSignalQuality(float signalQuality) {
        boolean set = false;

        synchronized (this) {
            if (!m_signalQuality.equals(signalQuality)) {
                m_signalQuality = signalQuality;
                set = true;
            }
        }

        if (set) {
            updateObservers(SIGNAL_QUALITY);
        }
    }

    public synchronized boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean set = false;

        synchronized (this) {
            if (!m_enabled.equals(enabled)) {
                m_enabled = enabled;
                set = true;
            }
        }

        if (set) {
            updateObservers(ENABLED);
        }
    }

    public synchronized boolean isBypassed() {
        return m_bypassed;
    }

    public void setBypassed(boolean bypassed) {
        boolean set = false;

        synchronized (this) {
            if (!m_bypassed.equals(bypassed)) {
                m_bypassed = bypassed;
                set = true;
            }
        }

        if (set) {
            updateObservers(BYPASSED);
        }
    }

    public synchronized Card getCard() {
        return m_card;
    }

    public void setCard(Card card) {
        boolean set = false;

        synchronized (this) {
            if (!m_card.equals(card)) {
                m_card = card;
                set = true;
            }
        }

        if (set) {
            updateObservers(CARD);
        }
    }

    public void registerObserver(IObserver<TeamUpdateType> observer) {
        m_observers.add(observer);
    }

    public void deregisterObserver(IObserver<TeamUpdateType> observer) {
        m_observers.remove(observer);
    }

    public void updateObservers(TeamUpdateType updateType) {
        for (IObserver<TeamUpdateType> observer : m_observers) {
            observer.update(updateType);
        }
    }
}
