package com.fsilberberg.ftamonitor.ftaassistant;

import org.joda.time.DateTime;

import java.util.Collection;

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
     * Gets the official FIRST-defined full event name
     *
     * @return The event name
     */
    public String getEventName();

    /**
     * Gets the location for where this event will be held.
     *
     * @return The event location.
     */
    public String getEventLoc();

    /**
     * Gets the start date of this event.
     *
     * @return The start date
     */
    public DateTime getStartDate();

    /**
     * Gets the end date of this event
     *
     * @return The end date
     */
    public DateTime getEndDate();

    /**
     * Gets the teams attending this event
     *
     * @return The teams at the event
     */
    public Collection<Team> getTeams();

    /**
     * Gets the matches played at this event
     *
     * @return The match list
     */
    public Collection<Match> getMatches();

    /**
     * Gets the notes associated with this match, if there are any. If there are no notes, this
     * will return the empty list.
     *
     * @return The notes associated with this match
     */
    public Collection<Note> getNotes();
}
