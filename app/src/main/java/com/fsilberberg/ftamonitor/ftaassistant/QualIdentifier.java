package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.MatchPeriod;

/**
 * Created by Fredric on 10/18/14.
 */
public class QualIdentifier implements MatchIdentifier {

    private final int m_matchNum;
    private final int m_replay;

    public QualIdentifier(int matchNum, int replay) {
        m_matchNum = matchNum;
        m_replay = replay;
    }

    @Override
    public MatchPeriod getPeriod() {
        return MatchPeriod.QUAL;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Q");
        sb.append(m_matchNum);
        if (m_replay != 0) {
            sb.append(" R");
            sb.append(m_replay);
        }
        return sb.toString();
    }

    @Override
    public int compareTo(MatchIdentifier matchIdentifier) {
        int comparison = 0;
        if (matchIdentifier == null) {
            throw new NullPointerException("Given null match identifier!");
        } else if (matchIdentifier instanceof ElimIdentifier) {
            comparison = -1;
        } else if (matchIdentifier instanceof PracticeIdentifier) {
            comparison = 1;
        } else if (matchIdentifier instanceof QualIdentifier) {
            QualIdentifier identifier = (QualIdentifier) matchIdentifier;
            comparison = Integer.valueOf(m_matchNum).compareTo(identifier.m_matchNum);
            if (comparison == 0) {
                comparison = Integer.valueOf(m_replay).compareTo(identifier.m_replay);
            }
        } else {
            throw new IllegalArgumentException("Given argument of unknown type " + matchIdentifier.getClass().getName());
        }

        return comparison;
    }
}
