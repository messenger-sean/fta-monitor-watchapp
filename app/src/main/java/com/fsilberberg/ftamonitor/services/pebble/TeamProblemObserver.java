package com.fsilberberg.ftamonitor.services.pebble;

import android.databinding.Observable;
import android.util.Log;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.services.pebble.PebbleSender.PebbleMessage;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class TeamProblemObserver extends Observable.OnPropertyChangedCallback implements DeletableObserver {
    private final String TAG = TeamProblemObserver.class.getName();
    private final Collection<Integer> TEAM_PROPERTIES = Arrays.asList(BR.teamNumber,
            BR.robotStatus, BR.bypassed, BR.estop, BR.dataRate, BR.battery);
    // Constants for the different statuses
    private static final byte ETH = 0;
    private static final byte DS = 1;
    private static final byte RADIO = 2;
    private static final byte RIO = 3;
    private static final byte CODE = 4;
    private static final byte ESTOP = 5;
    private static final byte GOOD = 6;
    private static final byte BWU = 7;
    private static final byte BYP = 8;
    private static final byte BAT = 9;

    private static final float DEFAULT_BATTERY = 37.37f;

    private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_teamStatus;
    private final PebbleSender m_sender;
    private final boolean m_bandwidthNotify;
    private final float m_maxBandwidth;
    private final boolean m_lowBatteryNotify;
    private final float m_lowBattery;
    private final boolean m_updateOutOfMatch;
    private final int m_vibeInterval;
    private final int m_keyOffset;
    private boolean m_inMatch = false;
    private byte m_lastStatus = ETH;
    private int m_teamNum;
    private int m_lastTeamNum;
    private float m_battery = DEFAULT_BATTERY;
    private float m_lastBattery = DEFAULT_BATTERY;
    private DateTime m_lastVibeTime = DateTime.now();
    private MatchStatus m_lastMatchStatus;

    public TeamProblemObserver(TeamStatus teamStatus, PebbleSender sender, boolean bandwidthNotify,
                               float maxBandwidth, boolean lowBatteryNotify, float lowBattery,
                               boolean updateOutOfMatch, int vibeInterval, int keyOffset) {
        m_teamStatus = teamStatus;
        m_sender = sender;
        m_bandwidthNotify = bandwidthNotify;
        m_maxBandwidth = maxBandwidth;
        m_lowBattery = lowBattery;
        m_lowBatteryNotify = lowBatteryNotify;
        m_updateOutOfMatch = updateOutOfMatch;
        m_vibeInterval = vibeInterval;
        m_teamNum = m_teamStatus.getTeamNumber();
        m_lastMatchStatus = m_fieldStatus.getMatchStatus();
        m_keyOffset = keyOffset;
    }

    /**
     * Initializes the team problem observer to start listening for updatess from the field and
     * team statuses. We make sure not to do this in the constructor to avoid passing around an
     * object that has not been fully initialized.
     *
     * @return This observer, fully initialized.
     */
    public TeamProblemObserver init() {
        m_teamStatus.addOnPropertyChangedCallback(this);
        m_fieldStatus.addOnPropertyChangedCallback(this);
        return this;
    }

    @Override
    public void onPropertyChanged(Observable observable, int property) {
        if (TEAM_PROPERTIES.contains(property)) {
            updateTeamStatus();
        } else if (property == BR.matchStatus) {
            batteryCheckUpdate();
            updateTeamStatus();
        }
    }

    @Override
    public void checkUpdate() {
        batteryCheckUpdate();
        updateTeamStatus();
    }

    /**
     * Updates the current battery monitor checking state. If we're in between matches, then
     * we don't check the battery. If auto just started, reset the battery status.
     */
    private void batteryCheckUpdate() {
        MatchStatus newStatus = m_fieldStatus.getMatchStatus();
        if (newStatus.equals(m_lastMatchStatus)) {
            return;
        }

        m_lastMatchStatus = newStatus;
        switch (m_lastMatchStatus) {
            case AUTO:
            case READY_TO_PRESTART:
                // In both these cases, we want to reset the battery. We could have gotten
                // some spurious values from during setup, so we want to make sure that everything
                // is reset for the actual match. However, the end of match values might also be
                // useful, so we reset after the competitors have left the field.
                m_battery = DEFAULT_BATTERY;
                break;
            default:
                break;
        }

        switch (m_lastMatchStatus) {
            case AUTO:
            case TELEOP:
            case TRANSITION:
                m_inMatch = true;
                break;
            default:
                m_inMatch = false;
                break;
        }
    }

    /**
     * Updates the status of the current team. Normally, this will optimize by not updating the status if the
     * status is the same as the previous status. If force is true, however, then the status will be updated
     * regardless of whether or not the new status is the same as the last status
     */
    private void updateTeamStatus() {
        Collection<PebbleMessage> messages = new ArrayList<>(3);

        byte status;
        if (m_teamStatus.isEstop()) {
            status = ESTOP;
        } else if (m_teamStatus.isBypassed()) {
            status = BYP;
        } else {
            switch (m_teamStatus.getRobotStatus()) {
                case NO_DS_ETH:
                    status = ETH;
                    break;
                case NO_DS:
                    status = DS;
                    break;
                case NO_RADIO:
                    status = RADIO;
                    break;
                case NO_RIO:
                    status = RIO;
                    break;
                case NO_CODE:
                    status = CODE;
                    break;
                case GOOD:
                default:
                    // Battery is more important than bandwidth, imo
                    if (m_lowBatteryNotify && m_teamStatus.getBattery() < m_lowBattery) {
                        status = BAT;
                    } else if (m_bandwidthNotify && m_teamStatus.getDataRate() > m_maxBandwidth) {
                        status = BWU;
                    } else {
                        status = GOOD;
                    }
                    break;
            }
        }

        if (m_lastStatus != status) {
            m_lastStatus = status;
            // Only vibrate if the status has been updated. Don't do it for team number or
            // battery updates
            boolean vibrate = false;
            if ((m_updateOutOfMatch || m_inMatch) &&
                    m_lastVibeTime.plusSeconds(m_vibeInterval).isBeforeNow()) {
                vibrate = true;
                m_lastVibeTime = DateTime.now();
            }
            PebbleMessage message =
                    new PebbleMessage(PebbleSender.STATUS_START + m_keyOffset, status, vibrate);
            messages.add(message);
        }

        m_teamNum = m_teamStatus.getTeamNumber();
        Log.d(TAG, "updateTeamStatus: Current number is " + m_teamNum + ", last is " + m_lastTeamNum);
        if (m_teamNum != m_lastTeamNum) {
            m_lastTeamNum = m_teamNum;
            PebbleMessage message = new PebbleMessage(PebbleSender.NUMBER_START + m_keyOffset, m_teamNum, false);
            messages.add(message);
        }

        // Only update the battery status if it's now less than the previous battery value.
        float battery = m_teamStatus.getBattery();
        if (battery < m_battery) {
            m_battery = battery;
        }

        if (m_battery != m_lastBattery) {
            m_lastBattery = m_battery;
            PebbleMessage message = new PebbleMessage(PebbleSender.BATTERY_START + m_keyOffset, (int) m_battery * 100, false);
            messages.add(message);
        }

        if (!messages.isEmpty()) {
            Log.d(TAG, "updateTeamStatus: Sending messages: " + messages.toString());
            m_sender.addMessage(messages.toArray(new PebbleMessage[messages.size()]));
        }
    }

    @Override
    public void unregister() {
        m_teamStatus.removeOnPropertyChangedCallback(this);
        m_fieldStatus.removeOnPropertyChangedCallback(this);
    }
}
