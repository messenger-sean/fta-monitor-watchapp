package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Station;

import java.util.Collection;

/**
 * Created by Fredric on 10/18/14.
 */
public class MatchImpl implements Match {

    private final long m_id;
    private final MatchIdentifier m_matchIdentifier;

    public MatchImpl(long id, MatchIdentifier matchIdentifier) {
        m_id = id;
        m_matchIdentifier = matchIdentifier;
    }

    @Override
    public long getId() {
        return m_id;
    }

    @Override
    public MatchIdentifier getMatchId() {
        return m_matchIdentifier;
    }

    @Override
    public Team getTeam(Alliance alliance, Station station) {
        return null;
    }

    @Override
    public Collection<Team> getTeams() {
        return null;
    }

    @Override
    public Event getEvent() {
        return null;
    }

    @Override
    public Collection<Note> getNotes() {
        return null;
    }
}
