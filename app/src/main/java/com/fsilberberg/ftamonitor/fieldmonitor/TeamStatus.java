package com.fsilberberg.ftamonitor.fieldmonitor;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.common.Observer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Encapsulates the current status of one of the alliance team members. There are however many of these
 * per alliance accessible through the {@link com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory}.
 * This class should never be instantiated directly outside this package
 */
public class TeamStatus extends BaseObservable {
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

    @Bindable
    public synchronized int getTeamNumber() {
        return m_teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        m_teamNumber = teamNumber;
        notifyPropertyChanged(BR.teamNumber);
    }

    @Bindable
    public synchronized int getDroppedPackets() {
        return m_droppedPackets;
    }

    public void setDroppedPackets(int droppedPackets) {
        m_droppedPackets = droppedPackets;
        notifyPropertyChanged(BR.droppedPackets);
    }

    @Bindable
    public synchronized int getRoundTrip() {
        return m_roundTrip;
    }

    public void setRoundTrip(int roundTrip) {
        m_roundTrip = roundTrip;
        notifyPropertyChanged(BR.roundTrip);
    }

    @Bindable
    public synchronized boolean isDsEth() {
        return m_dsEth;
    }

    public void setDsEth(boolean dsEth) {
        m_dsEth = dsEth;
        notifyPropertyChanged(BR.dsEth);
    }

    @Bindable
    public synchronized boolean isDs() {
        return m_ds;
    }

    public void setDs(boolean ds) {
        m_ds = ds;
        notifyPropertyChanged(BR.ds);
    }

    @Bindable
    public synchronized boolean isRadio() {
        return m_radio;
    }

    public void setRadio(boolean radio) {
        m_radio = radio;
        notifyPropertyChanged(BR.radio);
    }

    @Bindable
    public synchronized boolean isRio() {
        return m_rio;
    }

    public void setRio(boolean rio) {
        m_rio = rio;
        notifyPropertyChanged(BR.rio);
    }

    @Bindable
    public synchronized boolean isEstop() {
        return m_estop;
    }

    public void setEstop(boolean estop) {
        m_estop = estop;
        notifyPropertyChanged(BR.estop);
    }

    @Bindable
    public synchronized boolean isCode() {
        return m_code;
    }

    public void setCode(boolean code) {
        m_code = code;
        notifyPropertyChanged(BR.code);
    }

    @Bindable
    public synchronized float getBattery() {
        return m_battery;
    }

    public void setBattery(float battery) {
        m_battery = battery;
        notifyPropertyChanged(BR.battery);
    }

    @Bindable
    public synchronized float getDataRate() {
        return m_dataRate;
    }

    public void setDataRate(float dataRate) {
        m_dataRate = dataRate;
        notifyPropertyChanged(BR.dataRate);
    }

    @Bindable
    public synchronized float getSignalStrength() {
        return m_signalStrength;
    }

    public void setSignalStrength(float signalStrength) {
        m_signalStrength = signalStrength;
        notifyPropertyChanged(BR.signalStrength);
    }

    @Bindable
    public synchronized float getSignalQuality() {
        return m_signalQuality;
    }

    public void setSignalQuality(float signalQuality) {
        m_signalQuality = signalQuality;
        notifyPropertyChanged(BR.signalQuality);
    }

    @Bindable
    public synchronized boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
        notifyPropertyChanged(BR.enabled);
    }

    @Bindable
    public synchronized boolean isBypassed() {
        return m_bypassed;
    }

    public void setBypassed(boolean bypassed) {
        m_bypassed = bypassed;
        notifyPropertyChanged(BR.bypassed);
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
