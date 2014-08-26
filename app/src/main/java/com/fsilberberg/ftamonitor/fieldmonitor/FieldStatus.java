package com.fsilberberg.ftamonitor.fieldmonitor;

import android.util.Log;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Card;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchInfo;

import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collection;

import static com.fsilberberg.ftamonitor.common.Alliance.*;
import static com.fsilberberg.ftamonitor.common.Card.*;
import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;
import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_INITIATED;
import static com.fsilberberg.ftamonitor.fieldmonitor.FieldUpdateType.*;

/**
 * Created by Fredric on 8/17/14.
 */
public class FieldStatus {
    // Teams
    private final TeamStatus m_red1 = new TeamStatus(1, Alliance.RED);
    private final TeamStatus m_red2 = new TeamStatus(2, Alliance.RED);
    private final TeamStatus m_red3 = new TeamStatus(3, Alliance.RED);
    private final TeamStatus m_blue1 = new TeamStatus(1, BLUE);
    private final TeamStatus m_blue2 = new TeamStatus(2, BLUE);
    private final TeamStatus m_blue3 = new TeamStatus(3, BLUE);

    // Match stats
    private String m_matchNumber = "999";
    private MatchStatus m_matchStatus = NOT_READY;
    private Period m_autoTime;
    private Period m_teleopTime;

    // Observers
    private final Collection<IFieldMonitorObserver> m_observers = new ArrayList<>();

    FieldStatus(int defaultAutoSeconds, int defaultTeleopSeconds) {
        m_autoTime = Period.seconds(defaultAutoSeconds);
        m_teleopTime = Period.seconds(defaultTeleopSeconds);
    }

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

    public synchronized String getMatchNumber() {
        return m_matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        boolean set = false;

        // Update the variable if necessary
        synchronized (this) {
            if (!m_matchNumber.equals(matchNumber)) {
                m_matchNumber = matchNumber;
                set = true;
            }
        }

        // If we set it, update the observer. This ensures that this is not operating in a lock
        if (set) {
            updateObservers(MATCH_NUMBER);
        }
    }

    public synchronized MatchStatus getMatchStatus() {
        return m_matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        boolean set = false;

        // Update the variable if necessary
        synchronized (this) {
            if (!m_matchStatus.equals(matchStatus)) {
                m_matchStatus = matchStatus;
                set = true;
            }
        }

        // If we set it, update the observer. This ensures that this is not operating in a lock
        if (set) {
            updateObservers(MATCH_STATUS);
        }
    }

    public synchronized Period getAutoTime() {
        return m_autoTime;
    }

    public void setAutoTime(Period autoTime) {
        boolean set = false;

        // Update the variable if necessary
        synchronized (this) {
            if (!m_autoTime.equals(autoTime)) {
                m_autoTime = autoTime;
                set = true;
            }
        }

        // If we set it, update the observer. This ensures that this is not operating in a lock
        if (set) {
            updateObservers(AUTO_TIME);
        }
    }

    public synchronized Period getTeleopTime() {
        return m_teleopTime;
    }

    public void setTeleopTime(Period teleopTime) {
        boolean set = false;

        // Update the variable if necessary
        synchronized (this) {
            if (!m_teleopTime.equals(teleopTime)) {
                m_teleopTime = teleopTime;
                set = true;
            }
        }

        // If we set it, update the observer. This ensures that this is not operating in a lock
        if (set) {
            updateObservers(TELEOP_TIME);
        }
    }

    /**
     * Updates this FieldStatus with information form the transmitted MatchInfo
     *
     * @param info The updated info
     */
    public void updateMatchInfo(MatchInfo info) {
        // TODO: There's a lot more info, such as team rank, scores, and such. Use it!
        setMatchNumber(info.getMatchIdentifier());
        setAutoTime(Period.seconds(info.getAutoStartTime()));
        setTeleopTime(Period.seconds(info.getManualStartTime()));

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

    private static Card parseCard(int cardNum) {
        switch (cardNum) {
            case 1:
                return YELLOW;
            case 2:
                return Card.RED;
            case 3:
            default:
                return NONE;
        }
    }

    public void registerObserver(IFieldMonitorObserver observer) {
        m_observers.add(observer);
        m_red1.registerObserver(observer);
        m_red2.registerObserver(observer);
        m_red3.registerObserver(observer);
        m_blue1.registerObserver(observer);
        m_blue2.registerObserver(observer);
        m_blue3.registerObserver(observer);
    }

    public void deregisterObserver(IFieldMonitorObserver observer) {
        m_observers.remove(observer);
        m_red2.deregisterObserver(observer);
        m_red3.deregisterObserver(observer);
        m_blue1.deregisterObserver(observer);
        m_blue2.deregisterObserver(observer);
        m_blue3.deregisterObserver(observer);
    }

    private void updateObservers(FieldUpdateType update) {
        for (IFieldMonitorObserver observer : m_observers) {
            observer.update(update);
        }
    }
}
