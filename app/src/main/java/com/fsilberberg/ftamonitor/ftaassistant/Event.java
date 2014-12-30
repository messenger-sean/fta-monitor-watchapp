package com.fsilberberg.ftamonitor.ftaassistant;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;

/**
 * Represents an FRC match
 */
public interface Event {

    /**
     * Unique Identifier for the event. This is suitable for use in a database as a primary key.
     *
     * @return The unique id of the event
     */
    public long getId();

    /**
     * Gets the official FIRST-defined event code for this event.
     *
     * @return The event code
     */
    public String getEventCode();

    /**
     * Sets the official FIRST-defined event code for this event.
     *
     * @param eventCode The new event code for this event
     */
    public void setEventCode(String eventCode);

    /**
     * Gets the official FIRST-defined full event name
     *
     * @return The event name
     */
    public String getEventName();

    /**
     * Sets the official FIRST-defined full event name
     *
     * @param eventName The new event name
     */
    public void setEventName(String eventName);

    /**
     * Gets the location for where this event will be held.
     *
     * @return The event location.
     */
    public String getEventLoc();

    /**
     * Sets the location for where this event will be held.
     *
     * @param eventLoc The new location of the event
     */
    public void setEventLoc(String eventLoc);

    /**
     * Gets the start date of this event.
     *
     * @return The start date
     */
    public DateTime getStartDate();

    /**
     * Sets the start date of this event.
     *
     * @param startDate The new start date
     */
    public void setStartDate(DateTime startDate);

    /**
     * Gets the end date of this event
     *
     * @return The end date
     */
    public DateTime getEndDate();

    /**
     * Sets the end date of this event
     *
     * @return The end date
     */
    public void setEndDate(DateTime endDate);

    /**
     * Gets the teams attending this event
     *
     * @return The teams at the event
     */
    public Collection<? extends Team> getTeams();

    /**
     * Sets the teams attending this event
     *
     * @param teams The new set of teams
     */
    public void setTeams(Collection<? extends Team> teams);

    /**
     * Gets the matches played at this event
     *
     * @return The match list
     */
    public Collection<? extends Match> getMatches();

    /**
     * Gets the matches played at this event
     *
     * @return The match list
     */
    public void setMatches(Collection<? extends Match> matches);

    /**
     * Gets the notes associated with this match, if there are any. If there are no notes, this
     * will return the empty list.
     *
     * @return The notes associated with this match
     */
    public Collection<? extends Note> getNotes();

    /**
     * Gets the notes associated with this match, if there are any. If there are no notes, this
     * will return the empty list.
     *
     * @return The notes associated with this match
     */
    public void setNotes(Collection<? extends Note> notes);
}
