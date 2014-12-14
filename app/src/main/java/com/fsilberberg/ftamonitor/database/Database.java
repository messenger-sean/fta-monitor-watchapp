package com.fsilberberg.ftamonitor.database;

import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Abstraction for all functions that can be performed on the database.
 *
 * @author Fredric
 */
public interface Database {

    /**
     * Gets the information for a match from the given period, with the given identifier, from the specified event.
     * If the match is not found, null will be returned.
     *
     * @param period          The period of the match
     * @param matchIdentifier The string that identifies the match, such as 1, or Semi 1-1
     * @param event           The event the match is being held at
     * @return The match from the database. If the match is not in the database, null is returned.
     */
    Optional<Match> getMatch(MatchPeriod period, String matchIdentifier, int replay, Event event);

    /**
     * Gets the team for a given number.
     *
     * @param number The number of the team
     * @return The team. If the team is not in the database, null is returned
     */
    Optional<Team> getTeam(int number);

    /**
     * Gets the event from a given year with the given code.
     *
     * @param year      The year of the event
     * @param eventCode The code of the event. This is the FIRST standard, code, such as MAWOR for WPI
     * @return The event. If the year/code combination is not found, null is returned
     */
    Optional<Event> getEvent(DateTime year, String eventCode);

    /**
     * Gets all events for a given year
     *
     * @param year The year to look for events
     * @return All events in the database for that year. If there are no events, the empty collection is returned
     */
    Collection<? extends Event> getEvents(DateTime year);

    /**
     * Saves a given list of matches to the database.
     *
     * @param matches The matches to save
     * @throws DatabaseException If an error occurs while saving the data
     */
    void saveMatch(Match... matches) throws DatabaseException;

    /**
     * Saves a given list of teams to the database
     *
     * @param teams The teams to save
     * @throws DatabaseException If an error occurs while saving the data
     */
    void saveTeam(Team... teams) throws DatabaseException;

    /**
     * Saves a given list of events to the database
     *
     * @param events The events to save
     * @throws DatabaseException If an error occurs while saving the data
     */
    void saveEvent(Event... events) throws DatabaseException;

    /**
     * Saves a given list of notes to the database
     *
     * @param notes The notes to save
     * @throws DatabaseException If an error occurs while saving the data
     */
    void saveNote(Note... notes) throws DatabaseException;

    /**
     * Removes a note from the database, deleting any relationships with the other objects
     *
     * @param notes The notes to remove
     * @throws DatabaseException If an error occurs while removing the data
     */
    void deleteNote(Note... notes) throws DatabaseException;
}
