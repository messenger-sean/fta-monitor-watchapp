package com.fsilberberg.ftamonitor.ftaassistant;

/**
 * This is an interface that represents a notes for the FTA Assistant side of the app.
 */
public interface Note {

    /**
     * Unique Identifier for the note. This is suitable for use in a database as a primary key.
     *
     * @return The unique id of the note
     */
    public long getId();

    /**
     * Gets the content of this note
     *
     * @return The note text
     */
    public String getContent();

    /**
     * Gets the team associated with this note, if it exists. If the team does not, this method
     * will return null
     *
     * @return The note's team
     */
    public Team getTeam();

    /**
     * Gets the event associated with this note, if it exists. If the event does not exist, this
     * method will return null.
     *
     * @return The note's event
     */
    public Event getEvent();

    /**
     * Gets the match associated with this note, if it exists. If the match does not exist, this
     * method will return null.
     *
     * @return The note's match
     */
    public Match getMatch();
}
