package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmEvent;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmMatch;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmNote;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmTeam;
import com.google.common.collect.Table;
import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Factory for obtaining implementations of objects in the {@link com.fsilberberg.ftamonitor.ftaassistant}
 * package
 */
public class AssistantFactory {

    private static AssistantFactory instance = new AssistantFactory();

    public static AssistantFactory getInstance() {
        return instance;
    }

    /**
     * Creates a new implementation of {@link Team} with the given number, name, and nick. This
     * should be used when the team has not yet been saved in the database, as it will cause
     * a new entry to be made in the database when saved
     *
     * @param teamNumber The team number
     * @param teamName   The full name of the team
     * @param teamNick   The short name of the team
     * @param matches    The matches the team is participating it
     * @param notes      The notes to add to the team
     * @param events     The events to add to the team
     * @return The team implementation
     */
    public Team makeTeam(int teamNumber, String teamName, String teamNick, Collection<Match> matches, Collection<Note> notes, Collection<Event> events) {
        return new OrmTeam(teamNumber, teamName, teamNick, matches, notes, events);
    }

    /**
     * Creates a new implementation of {@link Event} with the given
     * event info. This should be used when the event has not yet been saved in the database,
     * as it will cause a new entry to be made in the database when saved
     *
     * @param eventCode The FIRST code for the event
     * @param eventName The name of the event
     * @param eventLoc  The location for the event
     * @param startTime The starting time of the event
     * @param endTime   The ending time of the event
     * @param matches   The matches being played at the event
     * @param notes     The notes to add to the event
     * @param teams     The teams to add to the event
     * @return The event implementation
     */
    public Event makeEvent(String eventCode, String eventName, String eventLoc, DateTime startTime, DateTime endTime,
                           Collection<Match> matches, Collection<Note> notes, Collection<Team> teams) {
        return new OrmEvent(eventCode, eventName, eventLoc, startTime.toDate(), endTime.toDate(), matches, notes, teams);
    }

    /**
     * Creates a new implementation of {@link com.fsilberberg.ftamonitor.ftaassistant.Match} with
     * the given id, period, identifier, and replay. The identifier will be turned into the correct
     * parsed version depending on the match period. This should be used when creating a new
     * match not yet saved in the database: saving it will cause a new entry to be made
     *
     * @param event   The event of the match
     * @param notes   The notes to add to the match
     * @param teams   The teams playing in the match
     * @param period  The period of the match
     * @param replay  The replay number of this match
     * @param matchId the string identifier of the match
     * @return The match implementation
     */
    public Match makeMatch(Event event, Collection<Note> notes, Table<Alliance, Station, Team> teams, MatchPeriod period, int replay, String matchId) {
        return new OrmMatch(event, notes, teams, period, replay, matchId);
    }

    /**
     * Creates a new implementation of {@link com.fsilberberg.ftamonitor.ftaassistant.Note} with
     * the given id and content. This should be used when creating a new note not yet in the
     * database: when saved, this will create a new note
     *
     * @param content The content of the note
     * @param team    The team the note applies to
     * @param event   The event the note applies to
     * @param match   The match the note applies to
     * @return The note implementation
     */
    public Note makeNote(String content, Team team, Event event, Match match) {
        return new OrmNote(content, team, event, match);
    }
}
