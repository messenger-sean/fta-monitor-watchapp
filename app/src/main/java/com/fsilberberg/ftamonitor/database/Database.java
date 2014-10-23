package com.fsilberberg.ftamonitor.database;

import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;

import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Abstraction for all functions that can be performed on the database.
 *
 * @author Fredric
 */
public interface Database {

    /**
     * Gets a collection of matches that match the given filters. If no matches are found, the
     * empty collection will be returned. This will never return null. If no filters are specified,
     * all matches in the database will be returned. This could take a long time for all matches,
     * so please use this sparingly.
     *
     * @param team  The team to filter by. If no team filter is required, use null
     * @param event The event to filter by. If no event filter is required, use null
     * @param note  The note to filter by. If no note filter is required, use null
     * @return The list of matches that match the given criteria.
     */
    Collection<Match> getMatches(Team team, Event event, Note note) throws DatabaseException;

    /**
     * Gets a collection of teams that match the given filters. If no teams are found, the
     * empty collection will be returned. This will never return null. If no filters are specified,
     * all teams in the database will be returned. This could take a long time for all teams,
     * so please use this sparingly.
     *
     * @param event The event to filter by. If no event filter is required, use null
     * @param match The match to filter by. If no match filter is required, use null
     * @param note  The note to filter by. If no note filter is required, use null
     * @return The list of matches that match the given criteria.
     */
    Collection<Team> getTeams(Event event, Match match, Note note) throws DatabaseException;

    /**
     * Gets a collection of events that match the given filters. If no events are found, the
     * empty collection will be returned. This will never return null. If no filters are specified,
     * all events in the database will be returned. This could take a long time for all events,
     * so please use this sparingly.
     *
     * @param team  The team to filter by. If no team filter is required, use null
     * @param match The match to filter by. If no match filter is required, use null
     * @param note  The note to filter by. If no note filter is required, use null
     * @param year  The event year to filter by. If no year is required, use null
     * @return The list of matches that match the given criteria.
     */
    Collection<Event> getEvents(Team team, Match match, Note note, DateTime year) throws DatabaseException;

    /**
     * Gets a collection of notes that match the given filters. If no notes are found, the
     * empty collection will be returned. This will never return null. If no filters are specified,
     * all notes in the database will be returned. This could take a long time for all notes,
     * so please use this sparingly.
     *
     * @param team  The team to filter by. If no team filter is required, use null
     * @param match The match to filter by. If no match filter is required, use null
     * @param event The event to filter by. If no event filter is required, use null
     * @return The list of matches that match the given criteria.
     */
    Collection<Note> getNotes(Team team, Match match, Event event) throws DatabaseException;

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
