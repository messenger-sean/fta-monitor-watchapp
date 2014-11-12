package com.fsilberberg.ftamonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.ftaassistant.AssistantFactory;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

import static com.fsilberberg.ftamonitor.database.Table.EVENT;
import static com.fsilberberg.ftamonitor.database.Table.MATCH;
import static com.fsilberberg.ftamonitor.database.Table.NOTE;
import static com.fsilberberg.ftamonitor.database.Table.TEAM;

/**
 * Implementation of the Database that uses the SQLite definitions defined by {@link com.fsilberberg.ftamonitor.database.FTAMonitorContract}
 * and the SQLite Helper {@link com.fsilberberg.ftamonitor.database.SQLiteDatabaseHelper}
 */
public class FTAMonitorDatabase implements Database {

    private static final String AS = " AS ";
    private final SQLiteDatabaseHelper m_helper;

    FTAMonitorDatabase(Context context) {
        m_helper = new SQLiteDatabaseHelper(context);
    }

    @Override
    public Collection<Match> getMatches(Team team, Event event, Note note) throws DatabaseException {
        Collection<Match> matches = new ArrayList<>();
        StringBuilder columnBuilder = new StringBuilder();
        String tableName = FTAMonitorContract.getDatabaseName(MATCH);
        columnBuilder.append(tableName).append(".").append(FTAMonitorContract.Match._ID).append(AS).append("ID,")
                .append(tableName).append(".").append(FTAMonitorContract.Match.MATCH_IDENTIFIER).append(AS).append("Identifier,")
                .append(tableName).append(".").append(FTAMonitorContract.Match.MATCH_PERIOD).append(AS).append("Period,")
                .append(tableName).append(".").append(FTAMonitorContract.Match.REPLAY).append(AS).append("Replay");
        Cursor cursor = constructFilteredCursor(team, null, event, note, MATCH, columnBuilder.toString());

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                Match m = cursorToMatch(cursor);
                matches.add(m);
            } catch (Exception ex) {
                throw new DatabaseException("Error when constructing a match from the cursor", ex);
            }
        }
        return matches;
    }

    @Override
    public Collection<Team> getTeams(Event event, Match match, Note note) throws DatabaseException {
        Collection<Team> teams = new ArrayList<>();
        StringBuilder columnBuilder = new StringBuilder();
        String tableName = FTAMonitorContract.getDatabaseName(TEAM);
        columnBuilder.append(tableName).append(".").append(FTAMonitorContract.Team._ID).append(AS).append("ID,")
                .append(tableName).append(".").append(FTAMonitorContract.Team.TEAM_NUMBER).append(AS).append("TeamNumber,")
                .append(tableName).append(".").append(FTAMonitorContract.Team.TEAM_NAME).append(AS).append("TeamName,")
                .append(tableName).append(".").append(FTAMonitorContract.Team.TEAM_NICK).append(AS).append("TeamNick");
        Cursor cursor = constructFilteredCursor(null, match, event, note, TEAM, columnBuilder.toString());

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                Team t = cursorToTeam(cursor);
                teams.add(t);
            } catch (Exception ex) {
                throw new DatabaseException("Error when constructing a team from the cursor", ex);
            }
        }

        return teams;
    }

    @Override
    public Collection<Event> getEvents(Team team, Match match, Note note, DateTime year) throws DatabaseException {
        Collection<Event> events = new ArrayList<>();
        StringBuilder columnBuilder = new StringBuilder();
        String tableName = FTAMonitorContract.getDatabaseName(EVENT);
        columnBuilder.append(tableName).append(".").append(FTAMonitorContract.Event._ID).append(AS).append("ID,")
                .append(tableName).append(".").append(FTAMonitorContract.Event.EVENT_CODE).append(AS).append("Code,")
                .append(tableName).append(".").append(FTAMonitorContract.Event.EVENT_NAME).append(AS).append("EventName,")
                .append(tableName).append(".").append(FTAMonitorContract.Event.EVENT_LOC).append(AS).append("Loc,")
                .append(tableName).append(".").append(FTAMonitorContract.Event.START_DATE).append(AS).append("Start,")
                .append(tableName).append(".").append(FTAMonitorContract.Event.END_DATE).append(AS).append("End");
        Cursor cursor = constructFilteredCursor(team, match, null, note, EVENT, columnBuilder.toString());

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                Event ev = cursorToEvent(cursor);
                if (year != null && ev.getStartDate().getYear() != year.getYear()) {
                    continue;
                }
                events.add(ev);
            } catch (Exception ex) {
                throw new DatabaseException("Error when constructing an event from the cursor", ex);
            }
        }

        return events;
    }

    @Override
    public Collection<Note> getNotes(Team team, Match match, Event event) throws DatabaseException {
        Collection<Note> notes = new ArrayList<>();
        StringBuilder columnBuilder = new StringBuilder();
        String tableName = FTAMonitorContract.getDatabaseName(NOTE);
        columnBuilder.append(tableName).append(".").append(FTAMonitorContract.Notes._ID).append(AS).append("ID,")
                .append(tableName).append(".").append(FTAMonitorContract.Notes.CONTENT).append(AS).append("Content");
        Cursor cursor = constructFilteredCursor(team, match, event, null, NOTE, columnBuilder.toString());

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                Note n = cursorToNote(cursor);
                notes.add(n);
            } catch (Exception ex) {
                throw new DatabaseException("Error when constructing a note from the cursor", ex);
            }
        }

        return notes;
    }

    @Override
    public void saveMatch(Match... matches) throws DatabaseException {
        SQLiteDatabase db = m_helper.getWritableDatabase();
        for (Match m : matches) {
            if (m.getId() == AssistantFactory.NO_ID) {
                // This is a new match, make a new entry. This is the parts that go in the main table
                ContentValues mainCv = new ContentValues();
                mainCv.put(FTAMonitorContract.Match.MATCH_IDENTIFIER, m.getMatchId().getIdentifier());
                mainCv.put(FTAMonitorContract.Match.MATCH_PERIOD, m.getMatchId().getPeriod().ordinal());
                mainCv.put(FTAMonitorContract.Match.REPLAY, m.getMatchId().getReplay());
                long newId = db.insert(FTAMonitorContract.getDatabaseName(MATCH), null, mainCv);
                if (newId == -1) {
                    Log.w(FTAMonitorDatabase.class.getName(), "Unknown error when trying to insert match");
                }

                // If the teams list has content, then add all relationships
                if (m.getTeams().size() > 0) {
                    for (Team t : m.getTeams()) {
                        ContentValues teamCv = new ContentValues();
                        teamCv.put(FTAMonitorContract.Matches_Teams.TEAM, t.getId());
                        teamCv.put(FTAMonitorContract.Matches_Teams.MATCH, newId);
                        db.insert(FTAMonitorContract.getLinkId(MATCH, TEAM), null, teamCv);
                    }
                }

                // If the event list has content, then add all relationships
                if (m.getEvent() != null) {
                    ContentValues eventCv = new ContentValues();
                    eventCv.put(FTAMonitorContract.Matches_Event.EVENT, m.getEvent().getId());
                    eventCv.put(FTAMonitorContract.Matches_Event.MATCH, newId);
                    db.insert(FTAMonitorContract.getLinkName(MATCH, EVENT), null, eventCv);
                }

                // If the notes list has content, then add all relationships
                if (m.getNotes().size() > 0) {
                    for (Note n : m.getNotes()) {
                        ContentValues noteCv = new ContentValues();
                        noteCv.put(FTAMonitorContract.Match_Notes.NOTE, n.getId());
                        noteCv.put(FTAMonitorContract.Match_Notes.MATCH, newId);
                        db.insert(FTAMonitorContract.getLinkId(MATCH, NOTE), null, noteCv);
                    }
                }
            } else {
                // This is an existing match, update the existing entry
                ContentValues mainCv = new ContentValues();
                mainCv.put(FTAMonitorContract.Match.MATCH_PERIOD, m.getMatchId().getPeriod().ordinal());
                mainCv.put(FTAMonitorContract.Match.MATCH_IDENTIFIER, m.getMatchId().getIdentifier());
                mainCv.put(FTAMonitorContract.Match.REPLAY, m.getMatchId().getReplay());
                db.update(FTAMonitorContract.Match.TABLE_NAME,
                        mainCv,
                        FTAMonitorContract.Match._ID + "=?",
                        new String[]{String.valueOf(m.getId())});

                // Get all created TEAM/NOTES links, and verify that all notes have been made


                /*
                TODO: Remove relationships. I don't think this is necessary here:
                    * Relationships with an event will never change
                    * Relationships with a team will never change
                    * Relationships with a note will be deleted when the note is deleted
                 */
            }
        }
    }

    @Override
    public void saveTeam(Team... teams) throws DatabaseException {

    }

    @Override
    public void saveEvent(Event... events) throws DatabaseException {

    }

    @Override
    public void saveNote(Note... notes) throws DatabaseException {

    }

    @Override
    public void deleteNote(Note... notes) throws DatabaseException {
        
    }

    /**
     * Turns the row in the given cursor to a team object
     *
     * @param cursor The cursor pointing at a team row
     * @return The team implementation
     */
    private Team cursorToTeam(Cursor cursor) {
        long id = cursor.getLong(0);
        int num = cursor.getInt(1);
        String name = cursor.getString(2);
        String nick = cursor.getString(3);

        return AssistantFactory.getInstance().makeTeam(id, num, name, nick);
    }

    /**
     * Turns the row in the given cursor to a match object
     *
     * @param cursor The cursor pointing at a match row
     * @return The match implementation
     */
    private Match cursorToMatch(Cursor cursor) {
        long id = cursor.getLong(0);
        String identifier = cursor.getString(1);
        MatchPeriod period = MatchPeriod.fromOrd(cursor.getInt(2));
        int replay = cursor.getInt(3);

        return AssistantFactory.getInstance().makeMatch(id, period, identifier, replay);
    }

    /**
     * Turns the row in the given cursor to an event object
     *
     * @param cursor The cursor pointing at an event row
     * @return The event implementation
     */
    private Event cursorToEvent(Cursor cursor) {
        long id = cursor.getLong(0);
        String code = cursor.getString(1);
        String name = cursor.getString(2);
        String loc = cursor.getString(3);
        DateTime start = DateTime.parse(cursor.getString(4));
        DateTime end = DateTime.parse(cursor.getString(5));

        return AssistantFactory.getInstance().makeEvent(id, code, name, loc, start, end);
    }

    /**
     * Turns the row in the given cursor to a note object
     *
     * @param cursor The cursor pointing at a note row
     * @return The note implementation
     */
    private Note cursorToNote(Cursor cursor) {
        long id = cursor.getLong(0);
        String content = cursor.getString(1);

        return AssistantFactory.getInstance().makeNote(id, content);
    }

    /**
     * Constructs a cursor with a query filtered by the given parameters. If any filters are not
     * required, then leave that field null, and the filter will be ignored.
     *
     * @param team    The team to filter by
     * @param match   The match to filter by
     * @param event   The event to filter by
     * @param note    The note to filter by
     * @param table   The table to construct a query for
     * @param columns The columns to project into the returned cursor
     * @return The initialized cursor with the filtered query on the given table
     * @throws DatabaseException If there is an error when constructing the cursor
     */
    private Cursor constructFilteredCursor(Team team,
                                           Match match,
                                           Event event,
                                           Note note,
                                           Table table,
                                           String columns) throws DatabaseException {
        StringBuilder query = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        Collection<String> whereArgs = new ArrayList<>();

        String SELECT_QUERY = "SELECT %s FROM %s";
        query.append(String.format(SELECT_QUERY, columns, FTAMonitorContract.getDatabaseName(table)));
        if (team != null) {
            constructJoinWhere(query, whereClause, table, TEAM);
            whereArgs.add(String.valueOf(team.getId()));
        }

        if (match != null) {
            constructJoinWhere(query, whereClause, table, MATCH);
            whereArgs.add(String.valueOf(match.getId()));
        }

        if (event != null) {
            constructJoinWhere(query, whereClause, table, EVENT);
            whereArgs.add(String.valueOf(event.getId()));
        }

        if (note != null) {
            constructJoinWhere(query, whereClause, table, NOTE);
            whereArgs.add(String.valueOf(note.getId()));
        }

        if (whereClause.length() != 0) {
            String WHERE_CLAUSE = " WHERE %s";
            query.append(String.format(WHERE_CLAUSE, whereClause.toString()));
        }
        query.append(";");

        try {
            return m_helper.getReadableDatabase().rawQuery(
                    query.toString(),
                    whereArgs.isEmpty() ? null : whereArgs.toArray(new String[whereArgs.size()]));
        } catch (Exception ex) {
            throw new DatabaseException("Error when constructing the database cursor", ex);
        }
    }

    /**
     * Constructs parts of the join and where statements given a reference to the string builders
     * for those statements and the joined tables. The WHERE statement value should be injected
     * via a rawQuery statement, and is using prepared statements to prevent SQL Injection Attacks
     *
     * @param query The builder holding the start of the query so far
     * @param where The builder holding the start of the where clause so far
     * @param main  The main table being joined to
     * @param link  The table being joined from
     * @throws DatabaseException If an error occurs while constructing the query
     */
    private void constructJoinWhere(StringBuilder query,
                                    StringBuilder where,
                                    Table main,
                                    Table link) throws DatabaseException {

        String joinTableName = FTAMonitorContract.getLinkName(main, link);
        String mainTableName = FTAMonitorContract.getDatabaseName(main);
        String joinTableField = FTAMonitorContract.getLinkId(main, link);
        String mainTableField = FTAMonitorContract.getDatabaseId(main);
        String whereField = FTAMonitorContract.getLinkName(link, main);


        String JOIN_CLAUSE = " JOIN %1$s ON %2$3$s.%s=%1$s.%4$s";
        query.append(String.format(JOIN_CLAUSE,
                joinTableName,
                mainTableName,
                mainTableField,
                joinTableField));
        if (where.length() != 0) {
            where.append(", ");
        }
        String WHERE_SELECTOR = "%s.%s=?";
        where.append(String.format(WHERE_SELECTOR,
                joinTableName,
                whereField));
    }
}
