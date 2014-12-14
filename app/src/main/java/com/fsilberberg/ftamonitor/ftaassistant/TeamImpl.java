package com.fsilberberg.ftamonitor.ftaassistant;

import java.util.Collection;

/**
 * Created by Fredric on 10/18/14.
 */
public class TeamImpl implements Team {

    private final long m_id;
    private final int m_teamNumber;
    private final String m_teamName;
    private final String m_teamNick;

    public TeamImpl(long id, int teamNumber, String teamName, String teamNick) {
        m_id = id;
        m_teamNumber = teamNumber;
        m_teamName = teamName;
        m_teamNick = teamNick;
    }

    @Override
    public long getId() {
        return m_id;
    }

    @Override
    public int getTeamNumber() {
        return m_teamNumber;
    }

    @Override
    public String getTeamName() {
        return m_teamName;
    }

    @Override
    public String getTeamNick() {
        return m_teamNick;
    }

    @Override
    public Collection<? extends Event> getEvents() {
        return null;
    }

    @Override
    public Collection<? extends Match> getMatches(Event event) {
        return null;
    }

    @Override
    public Collection<? extends Note> getNotes() {
        return null;
    }
}
