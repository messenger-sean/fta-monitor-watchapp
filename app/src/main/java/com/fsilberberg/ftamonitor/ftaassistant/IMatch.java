package com.fsilberberg.ftamonitor.ftaassistant;

import java.util.Collection;

/**
 * This is an interface that represents a match for the FTA Assistant side of the app.
 */
public interface IMatch {

    /**
     * Gets the match identifier
     *
     * @return The identifier of this match
     */
    public IMatchIdentifier getMatchId();

    /**
     * Gets the teams that are participating in this match
     *
     * @return The teams in the match
     */
    public Collection<ITeam> getTeams();

    /**
     * Gets the event where this match is being held
     *
     * @return The match's event
     */
    public IEvent getEvent();

    /**
     * Gets the notes for this match. If there are no notes, the empty list will be returned.
     *
     * @return The match notes
     */
    public Collection<INote> getNotes();
}
