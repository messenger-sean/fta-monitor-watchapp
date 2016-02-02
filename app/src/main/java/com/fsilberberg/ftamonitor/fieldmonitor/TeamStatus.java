package com.fsilberberg.ftamonitor.fieldmonitor;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;

import com.fsilberberg.ftamonitor.BR;

import java.util.Arrays;
import java.util.Collection;

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

    private final Object m_lock = new Object();
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
    private RobotConnectionStatus m_robotStatus = RobotConnectionStatus.NO_DS_ETH;

    public TeamStatus() {
        RobotConnectionStatusObserver statusObserver = new RobotConnectionStatusObserver();
        addOnPropertyChangedCallback(statusObserver);
    }

    @Bindable
    public int getTeamNumber() {
        synchronized (m_lock) {
            return m_teamNumber;
        }
    }

    public void setTeamNumber(int teamNumber) {
        synchronized (m_lock) {
            m_teamNumber = teamNumber;
        }
        notifyPropertyChanged(BR.teamNumber);
    }

    @Bindable
    public int getDroppedPackets() {
        synchronized (m_lock) {
            return m_droppedPackets;
        }
    }

    public void setDroppedPackets(int droppedPackets) {
        synchronized (m_lock) {
            m_droppedPackets = droppedPackets;
        }
        notifyPropertyChanged(BR.droppedPackets);
    }

    @Bindable
    public int getRoundTrip() {
        synchronized (m_lock) {
            return m_roundTrip;
        }
    }

    public void setRoundTrip(int roundTrip) {
        synchronized (m_lock) {
            m_roundTrip = roundTrip;
        }
        notifyPropertyChanged(BR.roundTrip);
    }

    @Bindable
    public boolean isDsEth() {
        synchronized (m_lock) {
            return m_dsEth;
        }
    }

    public void setDsEth(boolean dsEth) {
        synchronized (m_lock) {
            m_dsEth = dsEth;
        }
        notifyPropertyChanged(BR.dsEth);
    }

    @Bindable
    public boolean isDs() {
        synchronized (m_lock) {
            return m_ds;
        }
    }

    public void setDs(boolean ds) {
        synchronized (m_lock) {
            m_ds = ds;
        }
        notifyPropertyChanged(BR.ds);
    }

    @Bindable
    public boolean isRadio() {
        synchronized (m_lock) {
            return m_radio;
        }
    }

    public void setRadio(boolean radio) {
        synchronized (m_lock) {
            m_radio = radio;
        }
        notifyPropertyChanged(BR.radio);
    }

    @Bindable
    public boolean isRio() {
        synchronized (m_lock) {
            return m_rio;
        }
    }

    public void setRio(boolean rio) {
        synchronized (m_lock) {
            m_rio = rio;
        }
        notifyPropertyChanged(BR.rio);
    }

    @Bindable
    public boolean isEstop() {
        synchronized (m_lock) {
            return m_estop;
        }
    }

    public void setEstop(boolean estop) {
        synchronized (m_lock) {
            m_estop = estop;
        }
        notifyPropertyChanged(BR.estop);
    }

    @Bindable
    public boolean isCode() {
        synchronized (m_lock) {
            return m_code;
        }
    }

    public void setCode(boolean code) {
        synchronized (m_lock) {
            m_code = code;
        }
        notifyPropertyChanged(BR.code);
    }

    @Bindable
    public float getBattery() {
        synchronized (m_lock) {
            return m_battery;
        }
    }

    public void setBattery(float battery) {
        synchronized (m_lock) {
            m_battery = battery;
        }
        notifyPropertyChanged(BR.battery);
    }

    @Bindable
    public float getDataRate() {
        synchronized (m_lock) {
            return m_dataRate;
        }
    }

    public void setDataRate(float dataRate) {
        synchronized (m_lock) {
            m_dataRate = dataRate;
        }
        notifyPropertyChanged(BR.dataRate);
    }

    @Bindable
    public float getSignalStrength() {
        synchronized (m_lock) {
            return m_signalStrength;
        }
    }

    public void setSignalStrength(float signalStrength) {
        synchronized (m_lock) {
            m_signalStrength = signalStrength;
        }
        notifyPropertyChanged(BR.signalStrength);
    }

    @Bindable
    public float getSignalQuality() {
        synchronized (m_lock) {
            return m_signalQuality;
        }
    }

    public void setSignalQuality(float signalQuality) {
        synchronized (m_lock) {
            m_signalQuality = signalQuality;
        }
        notifyPropertyChanged(BR.signalQuality);
    }

    @Bindable
    public boolean isEnabled() {
        synchronized (m_lock) {
            return m_enabled;
        }
    }

    public void setEnabled(boolean enabled) {
        synchronized (m_lock) {
            m_enabled = enabled;
        }
        notifyPropertyChanged(BR.enabled);
    }

    @Bindable
    public boolean isBypassed() {
        synchronized (m_lock) {
            return m_bypassed;
        }
    }

    public void setBypassed(boolean bypassed) {
        synchronized (m_lock) {
            m_bypassed = bypassed;
        }
        notifyPropertyChanged(BR.bypassed);
    }

    @Bindable
    public RobotConnectionStatus getRobotStatus() {
        synchronized (m_lock) {
            return m_robotStatus;
        }
    }

    public void setRobotStatus(RobotConnectionStatus status) {
        synchronized (m_lock) {
            m_robotStatus = status;
        }
        notifyPropertyChanged(BR.robotStatus);
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
