package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.MatchPeriod;

/**
 * Created by Fredric on 10/18/14.
 */
public class PracticeIdentifier implements MatchIdentifier {

    private final int m_matchNumber;

    public PracticeIdentifier(int matchNumber) {
        m_matchNumber = matchNumber;
    }

    @Override
    public MatchPeriod getPeriod() {
        return MatchPeriod.PRAC;
    }

    @Override
    public String getIdentifier() {
        return String.valueOf(m_matchNumber);
    }

    @Override
    public int getReplay() {
        return 0; // Practice matches aren't replayed
    }

    @Override
    public String toString() {
        return "P" + String.valueOf(m_matchNumber);
    }

    @Override
    public int compareTo(MatchIdentifier matchIdentifier) {
        return 0;
    }
}
