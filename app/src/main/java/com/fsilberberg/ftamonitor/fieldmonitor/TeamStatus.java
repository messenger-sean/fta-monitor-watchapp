package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Card;
import com.fsilberberg.ftamonitor.common.IObservable;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.common.RobotStatus;

import java.util.ArrayList;
import java.util.Collection;

import static com.fsilberberg.ftamonitor.common.Card.*;
import static com.fsilberberg.ftamonitor.common.RobotStatus.*;

/**
 * Created by Fredric on 8/17/14.
 */
public class TeamStatus implements IObservable {
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

    private final Collection<IObserver> m_observers = new ArrayList<>();

    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        synchronized (this) {
            this.m_teamNumber = teamNumber;
        }
        updateObservers();
    }

    public synchronized int getDroppedPackets() {
        return m_droppedPackets;
    }

    public void setDroppedPackets(int droppedPackets) {
        synchronized (this) {
            m_droppedPackets = droppedPackets;
        }
        updateObservers();
    }

    public synchronized int getRoundTrip() {
        return m_roundTrip;
    }

    public void setRoundTrip(int roundTrip) {
        synchronized (this) {
            m_roundTrip = roundTrip;
        }
        updateObservers();
    }

    public synchronized boolean isDsEth() {
        return m_dsEth;
    }

    public void setDsEth(boolean dsEth) {
        synchronized (this) {
            this.m_dsEth = dsEth;
        }
        updateObservers();
    }

    public synchronized boolean isDs() {
        return m_ds;
    }

    public void setDs(boolean ds) {
        synchronized (this) {
            this.m_ds = ds;
        }
        updateObservers();
    }

    public synchronized boolean isRadio() {
        return m_radio;
    }

    public void setRadio(boolean radio) {
        synchronized (this) {
            this.m_radio = radio;
        }
        updateObservers();
    }

    public synchronized boolean isRobot() {
        return m_robot;
    }

    public void setRobot(boolean robot) {
        synchronized (this) {
            this.m_robot = robot;
        }
        updateObservers();
    }

    public synchronized boolean isEstop() {
        return m_estop;
    }

    public void setEstop(boolean estop) {
        synchronized (this) {
            this.m_estop = estop;
        }
        updateObservers();
    }

    public synchronized boolean isCode() {
        return m_code;
    }

    public void setCode(boolean code) {
        synchronized (this) {
            m_code = code;
        }
        updateObservers();
    }

    public synchronized float getBattery() {
        return m_battery;
    }

    public void setBattery(float battery) {
        synchronized (this) {
            m_battery = battery;
        }
        updateObservers();
    }

    public synchronized float getDataRate() {
        return m_dataRate;
    }

    public void setDataRate(float dataRate) {
        synchronized (this) {
            this.m_dataRate = dataRate;
        }
        updateObservers();
    }

    public synchronized float getSignalStrength() {
        return m_signalStrength;
    }

    public void setSignalStrength(float signalStrength) {
        synchronized (this) {
            this.m_signalStrength = signalStrength;
        }
        updateObservers();
    }

    public synchronized float getSignalQuality() {
        return m_signalQuality;
    }

    public void setSignalQuality(float signalQuality) {
        synchronized (this) {
            this.m_signalQuality = signalQuality;
        }
        updateObservers();
    }

    public synchronized boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        synchronized (this) {
            m_enabled = enabled;
        }
        updateObservers();
    }

    public synchronized boolean isBypassed() {
        return m_bypassed;
    }

    public void setBypassed(boolean bypassed) {
        synchronized (this) {
            m_bypassed = bypassed;
        }
        updateObservers();
    }

    public synchronized Card getCard() {
        return m_card;
    }

    public void setCard(Card card) {
        synchronized (this) {
            m_card = card;
        }
        updateObservers();
    }

    @Override
    public void registerObserver(IObserver observer) {
        m_observers.add(observer);
    }

    @Override
    public void deregisterObserver(IObserver observer) {
        m_observers.remove(observer);
    }

    private void updateObservers() {
        for (IObserver observer : m_observers) {
            observer.update();
        }
    }
}
