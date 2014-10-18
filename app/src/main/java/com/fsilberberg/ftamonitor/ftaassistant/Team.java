package com.fsilberberg.ftamonitor.ftaassistant;

import java.util.Collection;

/**
 * This is an interface that represents a team for the FTA Assistant side of the app.
 */
public interface Team {

    /**
     * Unique Identifier for the team. This is suitable for use in a database as a primary key.
     *
     * @return The team's unique identifier
     */
    public long getId();

    /**
     * Gets the number of this team
     *
     * @return The team number
     */
    public int getTeamNumber();

    /**
     * Gets the name of this team as registered with FIRST.
     *
     * @return The team name
     */
    public String getTeamName();

    /**
     * Gets the team nickname as registered with FIRST.
     *
     * @return The team nickname
     */
    public String getTeamNick();

    /**
     * Gets the events attended by this team
     *
     * @return The team's events
     */
    public Collection<Event> getEvents();

    /**
     * Gets the matches a team participated in at a given event.
     *
     * @param event The event to search for matches in
     * @return The matches the team participated in
     */
    public Collection<Match> getMatches(Event event);

    /**
     * Gets the notes that have been made about the team
     *
     * @return The notes made on a team
     */
    public Collection<Note> getNotes();
}
