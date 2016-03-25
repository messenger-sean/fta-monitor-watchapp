package com.fsilberberg.ftamonitor.fieldmonitor;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.TournamentLevel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;
import static com.fsilberberg.ftamonitor.common.TournamentLevel.PRACTICE;

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
    private AtomicReference<String> m_matchNumber = new AtomicReference<>("999");
    private AtomicReference<MatchStatus> m_matchStatus = new AtomicReference<>(NOT_READY);
    private AtomicInteger m_playNumber = new AtomicInteger(0);
    private AtomicReference<TournamentLevel> m_tournamentLevel = new AtomicReference<>(PRACTICE);

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
        return m_matchNumber.get();
    }

    @SuppressWarnings("unused")
    public void setMatchNumber(String matchNumber) {
        m_matchNumber.set(matchNumber);
        notifyPropertyChanged(BR.matchNumber);
    }

    @Bindable
    public MatchStatus getMatchStatus() {
        return m_matchStatus.get();
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        m_matchStatus.set(matchStatus);
        notifyPropertyChanged(BR.matchStatus);
    }

    @Bindable
    public int getPlayNumber() {
        return m_playNumber.get();
    }

    public void setPlayNumber(int playNumber) {
        m_playNumber.set(playNumber);
        notifyPropertyChanged(BR.playNumber);
    }

    @Bindable
    public TournamentLevel getTournamentLevel() {
        return m_tournamentLevel.get();
    }

    public void setTournamentLevel(TournamentLevel level) {
        m_tournamentLevel.set(level);
        notifyPropertyChanged(BR.tournamentLevel);
    }
}
