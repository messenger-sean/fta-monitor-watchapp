package com.fsilberberg.ftamonitor.ftaassistant;

import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Represents an FRC match
 */
public interface IEvent {

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
    public Collection<ITeam> getTeams();

    /**
     * Gets the matches played at this event
     *
     * @return The match list
     */
    public Collection<IMatch> getMatches();

    /**
     * Gets the notes associated with this match, if there are any. If there are no notes, this
     * will return the empty list.
     *
     * @return The notes associated with this match
     */
    public Collection<INote> getNotes();
}
