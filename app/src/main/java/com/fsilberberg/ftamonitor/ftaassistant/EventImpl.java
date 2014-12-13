package com.fsilberberg.ftamonitor.ftaassistant;

import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Implementation of the {@link com.fsilberberg.ftamonitor.ftaassistant.Event} interface that uses
 * lazy initialization from the {@link com.fsilberberg.ftamonitor.database.Database} implementation
 *
 * @author Fredric
 */
public class EventImpl implements Event {

    private final long m_id;
    private final String m_eventCode;
    private final String m_eventName;
    private final String m_eventLoc;
    private final DateTime m_startDate;
    private final DateTime m_endDate;

    public EventImpl(long id, String eventCode, String eventName, String eventLoc, DateTime startDate, DateTime endDate) {
        m_id = id;
        m_eventCode = eventCode;
        m_eventName = eventName;
        m_eventLoc = eventLoc;
        m_startDate = startDate;
        m_endDate = endDate;
    }

    @Override
    public long getId() {
        return m_id;
    }

    @Override
    public String getEventCode() {
        return m_eventCode;
    }

    @Override
    public String getEventName() {
        return m_eventName;
    }

    @Override
    public String getEventLoc() {
        return m_eventLoc;
    }

    @Override
    public DateTime getStartDate() {
        return m_startDate;
    }

    @Override
    public DateTime getEndDate() {
        return m_endDate;
    }

    @Override
    public Collection<? extends Team> getTeams() {
        return null;
    }

    @Override
    public Collection<? extends Match> getMatches() {
        return null;
    }

    @Override
    public Collection<? extends Note> getNotes() {
        return null;
    }
}
