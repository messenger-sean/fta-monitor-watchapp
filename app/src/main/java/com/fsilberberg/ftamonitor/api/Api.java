package com.fsilberberg.ftamonitor.api;

import com.fsilberberg.ftamonitor.ftaassistant.Event;

/**
 * API interface for updating and retrieving information about FRC events
 */
public interface Api {
    /**
     * Retrieves all defined events from the server. This does <b>NOT</b> update the team list or match list. If an event
     * is already defined in the database, it is updated, not removed.
     *
     * @param year The year to retrieve events for
     */
    void retrieveAllEvents(int year);

    /**
     * Updates the event, <b>INCLUDING</b> match and team list.
     *
     * @param event The event to update
     */
    void updateEvent(Event event);

    /**
     * Updates the information in a team. This does <b>NOT</b> update the team's event or match list. To update those,
     * use the {@link #updateEvent(com.fsilberberg.ftamonitor.ftaassistant.Event)} method for the event the team
     * participates in
     * @param team
     */
    void updateTeam(int team);
}
