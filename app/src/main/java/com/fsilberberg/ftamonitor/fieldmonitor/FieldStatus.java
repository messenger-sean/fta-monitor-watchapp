package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Card;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.RobotStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchInfo;

import org.joda.time.Period;

import static com.fsilberberg.ftamonitor.common.Card.*;
import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;

/**
 * Created by Fredric on 8/17/14.
 */
public class FieldStatus {
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

    FieldStatus(int defaultAutoSeconds, int defaultTeleopSeconds) {
        m_autoTime = Period.seconds(defaultAutoSeconds);
        m_teleopTime = Period.seconds(defaultTeleopSeconds);
    }

    public synchronized TeamStatus getRed1() {
        return m_red1;
    }

    public synchronized void setRed1(TeamStatus red1) {
        this.m_red1 = red1;
    }

    public synchronized TeamStatus getRed2() {
        return m_red2;
    }

    public synchronized void setRed2(TeamStatus red2) {
        this.m_red2 = red2;
    }

    public synchronized TeamStatus getRed3() {
        return m_red3;
    }

    public synchronized void setRed3(TeamStatus red3) {
        this.m_red3 = red3;
    }

    public synchronized TeamStatus getBlue1() {
        return m_blue1;
    }

    public synchronized void setBlue1(TeamStatus blue1) {
        this.m_blue1 = blue1;
    }

    public synchronized TeamStatus getBlue2() {
        return m_blue2;
    }

    public synchronized void setBlue2(TeamStatus blue2) {
        this.m_blue2 = blue2;
    }

    public synchronized TeamStatus getBlue3() {
        return m_blue3;
    }

    public synchronized void setBlue3(TeamStatus blue3) {
        this.m_blue3 = blue3;
    }

    public synchronized String getMatchNumber() {
        return m_matchNumber;
    }

    public synchronized void setMatchNumber(String matchNumber) {
        this.m_matchNumber = matchNumber;
    }

    public synchronized MatchStatus getMatchStatus() {
        return m_matchStatus;
    }

    public synchronized void setMatchStatus(MatchStatus matchStatus) {
        this.m_matchStatus = matchStatus;
    }

    public synchronized Period getMatchTime() {
        return m_matchTime;
    }

    public synchronized void setMatchTime(Period matchTime) {
        this.m_matchTime = matchTime;
    }

    public synchronized Period getAutoTime() {
        return m_autoTime;
    }

    public synchronized void setAutoTime(Period autoTime) {
        m_autoTime = autoTime;
    }

    public synchronized Period getTeleopTime() {
        return m_teleopTime;
    }

    public synchronized void setTeleopTime(Period teleopTime) {
        m_teleopTime = teleopTime;
    }

    /**
     * Updates this FieldStatus with information form the transmitted MatchInfo
     *
     * @param info The updated info
     */
    public synchronized void updateMatchInfo(MatchInfo info) {
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
        m_red1.setRobotStatus(parseStatus(info.isRed1IsBypassed(), m_red1.getRobotStatus()));
        m_red2.setRobotStatus(parseStatus(info.isRed2IsBypassed(), m_red2.getRobotStatus()));
        m_red3.setRobotStatus(parseStatus(info.isRed3IsBypassed(), m_red3.getRobotStatus()));
        m_blue1.setRobotStatus(parseStatus(info.isBlue1IsBypassed(), m_blue1.getRobotStatus()));
        m_blue2.setRobotStatus(parseStatus(info.isBlue2IsBypassed(), m_blue2.getRobotStatus()));
        m_blue3.setRobotStatus(parseStatus(info.isBlue3IsBypassed(), m_blue3.getRobotStatus()));
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

    private static RobotStatus parseStatus(boolean isBypassed, RobotStatus oldStatus) {
        return isBypassed ? RobotStatus.BYPASSED : oldStatus;
    }
}
