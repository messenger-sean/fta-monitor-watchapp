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

    // Match stats
    private String m_matchNumber = "999";
    private MatchStatus m_matchStatus = NOT_READY;
    private int m_playNumber = 0;

    public synchronized TeamStatus getRed1() {
        return m_red1;
    }

    public synchronized TeamStatus getRed2() {
        return m_red2;
    }

    public synchronized TeamStatus getRed3() {
        return m_red3;
    }

    public synchronized TeamStatus getBlue1() {
        return m_blue1;
    }

    public synchronized TeamStatus getBlue2() {
        return m_blue2;
    }

    public synchronized TeamStatus getBlue3() {
        return m_blue3;
    }

    @SuppressWarnings("unused")
    @Bindable
    public synchronized String getMatchNumber() {
        return m_matchNumber;
    }

    @SuppressWarnings("unused")
    public void setMatchNumber(String matchNumber) {
        m_matchNumber = matchNumber;
        notifyPropertyChanged(BR.matchNumber);
    }

    @Bindable
    public synchronized MatchStatus getMatchStatus() {
        return m_matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        m_matchStatus = matchStatus;
        notifyPropertyChanged(BR.matchStatus);
    }

    @Bindable
    public synchronized int getPlayNumber() {
        return m_playNumber;
    }

    public void setPlayNumber(int playNumber) {
        m_playNumber = playNumber;
        notifyPropertyChanged(BR.playNumber);
    }
}
