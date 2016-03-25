package com.fsilberberg.ftamonitor.fieldmonitor;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;

import com.fsilberberg.ftamonitor.BR;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Encapsulates the current status of one of the alliance team members. There are however many of these
 * per alliance accessible through the {@link com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory}.
 * This class should never be instantiated directly outside this package
 */
public class TeamStatus extends BaseObservable {
    /**
     * Defines the 6 possible connection states that a robot can be in. Rather than be constantly
     * calculated in multiple places throughout the code, they are calculated here as a
     * meta-property.
     */
    public enum RobotConnectionStatus {
        NO_DS_ETH, NO_DS, NO_RADIO, NO_RIO, NO_CODE, GOOD
    }

    private final AtomicInteger m_teamNumber = new AtomicInteger(1);
    private final AtomicInteger m_droppedPackets = new AtomicInteger(0);
    private final AtomicInteger m_roundTrip = new AtomicInteger(1);
    private final AtomicBoolean m_dsEth = new AtomicBoolean(false);
    private final AtomicBoolean m_ds = new AtomicBoolean(false);
    private final AtomicBoolean m_radio = new AtomicBoolean(false);
    private final AtomicBoolean m_rio = new AtomicBoolean(false);
    private final AtomicBoolean m_estop = new AtomicBoolean(false);
    private final AtomicBoolean m_code = new AtomicBoolean(false);
    private final AtomicReference<Float> m_battery = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> m_dataRate = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> m_signalStrength = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> m_signalQuality = new AtomicReference<>(0.0f);
    private final AtomicBoolean m_enabled = new AtomicBoolean(false);
    private final AtomicBoolean m_bypassed = new AtomicBoolean(false);
    private final AtomicReference<RobotConnectionStatus> m_robotStatus = new AtomicReference<>(RobotConnectionStatus.NO_DS_ETH);

    public TeamStatus() {
        RobotConnectionStatusObserver statusObserver = new RobotConnectionStatusObserver();
        addOnPropertyChangedCallback(statusObserver);
    }

    @Bindable
    public int getTeamNumber() {
        return m_teamNumber.get();
    }

    public void setTeamNumber(int teamNumber) {
        m_teamNumber.set(teamNumber);
        notifyPropertyChanged(BR.teamNumber);
    }

    @Bindable
    public int getDroppedPackets() {
        return m_droppedPackets.get();
    }

    public void setDroppedPackets(int droppedPackets) {
        m_droppedPackets.set(droppedPackets);
        notifyPropertyChanged(BR.droppedPackets);
    }

    @Bindable
    public int getRoundTrip() {
        return m_roundTrip.get();
    }

    public void setRoundTrip(int roundTrip) {
        m_roundTrip.set(roundTrip);
        notifyPropertyChanged(BR.roundTrip);
    }

    @Bindable
    public boolean isDsEth() {
        return m_dsEth.get();
    }

    public void setDsEth(boolean dsEth) {
        m_dsEth.set(dsEth);
        notifyPropertyChanged(BR.dsEth);
    }

    @Bindable
    public boolean isDs() {
        return m_ds.get();
    }

    public void setDs(boolean ds) {
        m_ds.set(ds);
        notifyPropertyChanged(BR.ds);
    }

    @Bindable
    public boolean isRadio() {
        return m_radio.get();
    }

    public void setRadio(boolean radio) {
        m_radio.set(radio);
        notifyPropertyChanged(BR.radio);
    }

    @Bindable
    public boolean isRio() {
        return m_rio.get();
    }

    public void setRio(boolean rio) {
        m_rio.set(rio);
        notifyPropertyChanged(BR.rio);
    }

    @Bindable
    public boolean isEstop() {
        return m_estop.get();
    }

    public void setEstop(boolean estop) {
        m_estop.set(estop);
        notifyPropertyChanged(BR.estop);
    }

    @Bindable
    public boolean isCode() {
        return m_code.get();
    }

    public void setCode(boolean code) {
        m_code.set(code);
        notifyPropertyChanged(BR.code);
    }

    @Bindable
    public float getBattery() {
        return m_battery.get();
    }

    public void setBattery(float battery) {
        m_battery.set(battery);
        notifyPropertyChanged(BR.battery);
    }

    @Bindable
    public float getDataRate() {
        return m_dataRate.get();
    }

    public void setDataRate(float dataRate) {
        m_dataRate.set(dataRate);
        notifyPropertyChanged(BR.dataRate);
    }

    @Bindable
    public float getSignalStrength() {
        return m_signalStrength.get();
    }

    public void setSignalStrength(float signalStrength) {
        m_signalStrength.set(signalStrength);
        notifyPropertyChanged(BR.signalStrength);
    }

    @Bindable
    public float getSignalQuality() {
        return m_signalQuality.get();
    }

    public void setSignalQuality(float signalQuality) {
        m_signalQuality.set(signalQuality);
        notifyPropertyChanged(BR.signalQuality);
    }

    @Bindable
    public boolean isEnabled() {
        return m_enabled.get();
    }

    public void setEnabled(boolean enabled) {
        m_enabled.set(enabled);
        notifyPropertyChanged(BR.enabled);
    }

    @Bindable
    public boolean isBypassed() {
        return m_bypassed.get();
    }

    public void setBypassed(boolean bypassed) {
        m_bypassed.set(bypassed);
        notifyPropertyChanged(BR.bypassed);
    }

    @Bindable
    public RobotConnectionStatus getRobotStatus() {
        return m_robotStatus.get();
    }

    public void setRobotStatus(RobotConnectionStatus status) {
        m_robotStatus.set(status);
        notifyPropertyChanged(BR.robotStatus);
    }

    @Override
    public void notifyPropertyChanged(int fieldId) {
        super.notifyPropertyChanged(fieldId);
    }

    private class RobotConnectionStatusObserver extends OnPropertyChangedCallback {
        Collection<Integer> statusProperties = Arrays.asList(BR.dsEth, BR.ds, BR.radio, BR.rio, BR.code);

        public void onPropertyChanged(Observable observable, int propertyChanged) {
            if (statusProperties.contains(propertyChanged)) {
                TeamStatus status = TeamStatus.this;
                RobotConnectionStatus robotStatus = RobotConnectionStatus.GOOD;
                if (!status.isDsEth()) {
                    robotStatus = RobotConnectionStatus.NO_DS_ETH;
                } else if (!status.isDs()) {
                    robotStatus = RobotConnectionStatus.NO_DS;
                } else if (!status.isRadio()) {
                    robotStatus = RobotConnectionStatus.NO_RADIO;
                } else if (!status.isRio()) {
                    robotStatus = RobotConnectionStatus.NO_RIO;
                } else if (!status.isCode()) {
                    robotStatus = RobotConnectionStatus.NO_CODE;
                }
                setRobotStatus(robotStatus);
            }
        }
    }
}
