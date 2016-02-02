package com.fsilberberg.ftamonitor.fieldmonitor;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.common.MatchStatus;

import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;

/**
 * The field status maintains the current status of the field via updates from signalr, so that there
 * is one centralized location for all classes to get information on the current state of the field.
 * It implements the observer pattern for classes that need to get periodic updates on what the field
 * is doing
 *
 * @author Fredric
 */
public class FieldStatus extends BaseObservable {
    // Teams
    private final TeamStatus m_red1 = new TeamStatus();
    private final TeamStatus m_red2 = new TeamStatus();
    private final TeamStatus m_red3 = new TeamStatus();
    private final TeamStatus m_blue1 = new TeamStatus();
    private final TeamStatus m_blue2 = new TeamStatus();
    private final TeamStatus m_blue3 = new TeamStatus();
    private final Object m_lock = new Object();

    // Match stats
    private String m_matchNumber = "999";
    private MatchStatus m_matchStatus = NOT_READY;
    private int m_playNumber = 0;

    public TeamStatus getRed1() {
        return m_red1;
    }

    public TeamStatus getRed2() {
        return m_red2;
    }

    public TeamStatus getRed3() {
        return m_red3;
    }

    public TeamStatus getBlue1() {
        return m_blue1;
    }

    public TeamStatus getBlue2() {
        return m_blue2;
    }

    public TeamStatus getBlue3() {
        return m_blue3;
    }

    @SuppressWarnings("unused")
    @Bindable
    public String getMatchNumber() {
        synchronized (m_lock) {
            return m_matchNumber;
        }
    }

    @SuppressWarnings("unused")
    public void setMatchNumber(String matchNumber) {
        synchronized (m_lock) {
            m_matchNumber = matchNumber;
        }
        notifyPropertyChanged(BR.matchNumber);
    }

    @Bindable
    public MatchStatus getMatchStatus() {
        synchronized (m_lock) {
            return m_matchStatus;
        }
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        synchronized (m_lock) {
            m_matchStatus = matchStatus;
        }
        notifyPropertyChanged(BR.matchStatus);
    }

    @Bindable
    public int getPlayNumber() {
        synchronized (m_lock) {
            return m_playNumber;
        }
    }

    public void setPlayNumber(int playNumber) {
        synchronized (m_lock) {
            m_playNumber = playNumber;
        }
        notifyPropertyChanged(BR.playNumber);
    }
}
