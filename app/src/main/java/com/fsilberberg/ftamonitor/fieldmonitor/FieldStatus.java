package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.MatchStatus;

/**
 * Created by Fredric on 8/17/14.
 */
public class FieldStatus {

    private static final FieldStatus instance = new FieldStatus();

    public static FieldStatus getInstance() {
        return instance;
    }

    // Teams
    private TeamStatus red1;
    private TeamStatus red2;
    private TeamStatus red3;
    private TeamStatus blue1;
    private TeamStatus blue2;
    private TeamStatus blue3;

    // Match stats
    private int matchNumber;
    private MatchStatus matchStatus;

    private FieldStatus() {

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
}
