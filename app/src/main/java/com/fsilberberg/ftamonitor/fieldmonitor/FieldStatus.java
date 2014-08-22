package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.MatchStatus;

import org.joda.time.Period;

import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;

/**
 * Created by Fredric on 8/17/14.
 */
public class FieldStatus {
    // Teams
    private TeamStatus red1 = new TeamStatus();
    private TeamStatus red2 = new TeamStatus();
    private TeamStatus red3 = new TeamStatus();
    private TeamStatus blue1 = new TeamStatus();
    private TeamStatus blue2 = new TeamStatus();
    private TeamStatus blue3 = new TeamStatus();

    // Match stats
    private int matchNumber = 1;
    private MatchStatus matchStatus = NOT_READY;
    private Period matchTime = Period.millis(0);

    FieldStatus() {
        // Intentionally package local so that only the factory
        // can construct new statuses
    }

    public synchronized TeamStatus getRed1() {
        return red1;
    }

    public synchronized void setRed1(TeamStatus red1) {
        this.red1 = red1;
    }

    public synchronized TeamStatus getRed2() {
        return red2;
    }

    public synchronized void setRed2(TeamStatus red2) {
        this.red2 = red2;
    }

    public synchronized TeamStatus getRed3() {
        return red3;
    }

    public synchronized void setRed3(TeamStatus red3) {
        this.red3 = red3;
    }

    public synchronized TeamStatus getBlue1() {
        return blue1;
    }

    public synchronized void setBlue1(TeamStatus blue1) {
        this.blue1 = blue1;
    }

    public synchronized TeamStatus getBlue2() {
        return blue2;
    }

    public synchronized void setBlue2(TeamStatus blue2) {
        this.blue2 = blue2;
    }

    public synchronized TeamStatus getBlue3() {
        return blue3;
    }

    public synchronized void setBlue3(TeamStatus blue3) {
        this.blue3 = blue3;
    }

    public synchronized int getMatchNumber() {
        return matchNumber;
    }

    public synchronized void setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
    }

    public synchronized MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public synchronized void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

    public synchronized Period getMatchTime() {
        return matchTime;
    }

    public synchronized void setMatchTime(Period matchTime) {
        this.matchTime = matchTime;
    }
}
