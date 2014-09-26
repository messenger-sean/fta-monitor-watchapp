package com.fsilberberg.ftamonitor.ftaassistant;

import android.support.annotation.Nullable;

/**
 * This is an interface that represents a notes for the FTA Assistant side of the app.
 */
public interface INote {
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
    @Nullable
    public ITeam getTeam();

    /**
     * Gets the event associated with this note, if it exists. If the event does not exist, this
     * method will return null.
     *
     * @return The note's event
     */
    @Nullable
    public IEvent getEvent();

    /**
     * Gets the match associated with this note, if it exists. If the match does not exist, this
     * method will return null.
     *
     * @return The note's match
     */
    @Nullable
    public IMatch getMatch();
}
