package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Station;

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
     * Gets the match identifier
     *
     * @return The identifier of this match
     */
    public MatchIdentifier getMatchId();

    /**
     * Gets a specific team at a specific station
     *
     * @param alliance The alliance of the team
     * @param station  The station of the team
     * @return The team at that station on that alliance
     */
    public Team getTeam(Alliance alliance, Station station);

    /**
     * Gets the teams that are participating in this match
     *
     * @return The teams in the match
     */
    public Collection<? extends Team> getTeams();

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
