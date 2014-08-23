package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Card;
import com.fsilberberg.ftamonitor.common.IObservable;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.RobotStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchInfo;

import org.apache.http.impl.conn.IdleConnectionHandler;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.fsilberberg.ftamonitor.common.Card.*;
import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;

/**
 * Created by Fredric on 8/17/14.
 */
public class FieldStatus implements IObservable {
    // Teams
    private TeamStatus m_red1 = new TeamStatus();
    private TeamStatus m_red2 = new TeamStatus();
    private TeamStatus m_red3 = new TeamStatus();
    private TeamStatus m_blue1 = new TeamStatus();
    private TeamStatus m_blue2 = new TeamStatus();
    private TeamStatus m_blue3 = new TeamStatus();

    // Match stats
    private String m_matchNumber = "999";
    private MatchStatus m_matchStatus = NOT_READY;
    private Period m_matchTime = Period.millis(0);
    private Period m_autoTime;
    private Period m_teleopTime;

    // Observers
    private final Collection<IObserver> m_observers = new ArrayList<>();

    FieldStatus(int defaultAutoSeconds, int defaultTeleopSeconds) {
        m_autoTime = Period.seconds(defaultAutoSeconds);
        m_teleopTime = Period.seconds(defaultTeleopSeconds);
    }

    public synchronized TeamStatus getRed1() {
        return m_red1;
    }

    public void setRed1(TeamStatus red1) {
        synchronized (this) {
            this.m_red1 = red1;
        }
        updateObservers();
    }

    public synchronized TeamStatus getRed2() {
        return m_red2;
    }

    public void setRed2(TeamStatus red2) {
        synchronized (this) {
            this.m_red2 = red2;
        }
        updateObservers();
    }

    public synchronized TeamStatus getRed3() {
        return m_red3;
    }

    public void setRed3(TeamStatus red3) {
        synchronized (this) {
            this.m_red3 = red3;
        }
        updateObservers();
    }

    public synchronized TeamStatus getBlue1() {
        return m_blue1;
    }

    public void setBlue1(TeamStatus blue1) {
        synchronized (this) {
            this.m_blue1 = blue1;
        }
        updateObservers();
    }

    public synchronized TeamStatus getBlue2() {
        return m_blue2;
    }

    public void setBlue2(TeamStatus blue2) {
        synchronized (this) {
            this.m_blue2 = blue2;
        }
        updateObservers();
    }

    public synchronized TeamStatus getBlue3() {
        return m_blue3;
    }

    public void setBlue3(TeamStatus blue3) {
        synchronized (this) {
            this.m_blue3 = blue3;
        }
        updateObservers();
    }

    public synchronized String getMatchNumber() {
        return m_matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        synchronized (this) {
            this.m_matchNumber = matchNumber;
        }
        updateObservers();
    }

    public synchronized MatchStatus getMatchStatus() {
        return m_matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        synchronized (this) {
            this.m_matchStatus = matchStatus;
        }
        updateObservers();
    }

    public synchronized Period getMatchTime() {
        return m_matchTime;
    }

    public void setMatchTime(Period matchTime) {
        synchronized (this) {
            this.m_matchTime = matchTime;
        }
        updateObservers();
    }

    public synchronized Period getAutoTime() {
        return m_autoTime;
    }

    public void setAutoTime(Period autoTime) {
        synchronized (this) {
            m_autoTime = autoTime;
        }
        updateObservers();
    }

    public synchronized Period getTeleopTime() {
        return m_teleopTime;
    }

    public void setTeleopTime(Period teleopTime) {
        synchronized (this) {
            m_teleopTime = teleopTime;
        }
        updateObservers();
    }

    /**
     * Updates this FieldStatus with information form the transmitted MatchInfo
     *
     * @param info The updated info
     */
    public void updateMatchInfo(MatchInfo info) {
        synchronized (this) {
            // TODO: There's a lot more info, such as team rank, scores, and such. Use it!
            m_matchNumber = info.getMatchIdentifier();
            if (m_autoTime.getSeconds() != info.getAutoStartTime()) {
                m_autoTime = Period.seconds(info.getAutoStartTime());
            }
            if (m_teleopTime.getSeconds() != info.getManualStartTime()) {
                m_teleopTime = Period.seconds(info.getManualStartTime());
            }

            // Team Numbers
            m_red1.setTeamNumber(info.getRed1TeamId());
            m_red2.setTeamNumber(info.getRed2TeamId());
            m_red3.setTeamNumber(info.getRed3TeamId());
            m_blue1.setTeamNumber(info.getBlue1TeamId());
            m_blue2.setTeamNumber(info.getBlue2TeamId());
            m_blue3.setTeamNumber(info.getBlue3TeamId());

            // Team Cards
            m_red1.setCard(parseCard(info.getRed1Card()));
            m_red2.setCard(parseCard(info.getRed2Card()));
            m_red3.setCard(parseCard(info.getRed3Card()));
            m_blue1.setCard(parseCard(info.getBlue1Card()));
            m_blue2.setCard(parseCard(info.getBlue2Card()));
            m_blue3.setCard(parseCard(info.getBlue3Card()));

            // Bypassed Status
            m_red1.setBypassed(info.isRed1IsBypassed());
            m_red2.setBypassed(info.isRed2IsBypassed());
            m_red3.setBypassed(info.isRed3IsBypassed());
            m_blue1.setBypassed(info.isBlue1IsBypassed());
            m_blue2.setBypassed(info.isBlue2IsBypassed());
            m_blue3.setBypassed(info.isBlue3IsBypassed());
        }
        updateObservers();
    }

    private static Card parseCard(int cardNum) {
        switch (cardNum) {
            case 1:
                return YELLOW;
            case 2:
                return RED;
            case 3:
            default:
                return NONE;
        }
    }

    @Override
    public void registerObserver(IObserver observer) {
        m_observers.add(observer);
        m_red1.registerObserver(observer);
        m_red2.registerObserver(observer);
        m_red3.registerObserver(observer);
        m_blue1.registerObserver(observer);
        m_blue2.registerObserver(observer);
        m_blue3.registerObserver(observer);
    }

    @Override
    public void deregisterObserver(IObserver observer) {
        m_observers.remove(observer);
        m_red2.deregisterObserver(observer);
        m_red3.deregisterObserver(observer);
        m_blue1.deregisterObserver(observer);
        m_blue2.deregisterObserver(observer);
        m_blue3.deregisterObserver(observer);
    }

    private void updateObservers() {
        for (IObserver observer : m_observers) {
            observer.update();
        }
    }
}
