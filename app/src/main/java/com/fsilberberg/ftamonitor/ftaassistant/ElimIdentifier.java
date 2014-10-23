package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.MatchPeriod;

/**
 * Created by Fredric on 10/18/14.
 */
public class ElimIdentifier implements MatchIdentifier {
    private final String m_identifier;
    private final int m_replay;

    public ElimIdentifier(String identifier, int replay) {
        m_identifier = identifier;
        m_replay = replay;
    }

    @Override
    public MatchPeriod getPeriod() {
        return MatchPeriod.ELIM;
    }

    @Override
    public String getIdentifier() {
        return m_identifier;
    }

    @Override
    public int getReplay() {
        return m_replay;
    }

    @Override
    public int compareTo(MatchIdentifier matchIdentifier) {
        // TODO: Parse the identifier string for round and match info
        return 0;
    }
}
