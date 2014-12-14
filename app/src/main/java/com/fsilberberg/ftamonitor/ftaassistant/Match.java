package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.common.Station;
import com.google.common.collect.Table;

import java.util.Collection;

/**
 * This is an interface that represents a match for the FTA Assistant side of the app.
 */
public interface Match {

    /**
     * Unique Identifier for the match. This is suitable for use in a database as a primary key.
     *
     * @return The unique id of the match
     */
    public long getId();

    /**
     * Gets the replay number of this match
     *
     * @return The match replay
     */
    public int getReplay();

    /**
     * Gets the period of the match
     *
     * @return What period this match is playing in
     */
    public MatchPeriod getPeriod();

    /**
     * Gets the string representation of the match identifier
     *
     * @return The identifier
     */
    public String getIdentifier();


    /**
     * Gets the teams in this match. The table allows for access to a specific team
     *
     * @return The team table
     */
    public Table<Alliance, Station, Team> getTeams();

    /**
     * Gets the event where this match is being held
     *
     * @return The match's event
     */
    public Event getEvent();

    /**
     * Gets the notes for this match. If there are no notes, the empty list will be returned.
     *
     * @return The match notes
     */
    public Collection<? extends Note> getNotes();
}
