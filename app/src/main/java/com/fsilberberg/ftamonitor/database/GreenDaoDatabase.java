package com.fsilberberg.ftamonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.ftaassistant.*;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;
import org.joda.time.DateTime;

import java.net.ContentHandler;
import java.util.*;

/**
 * This is a database implementation that uses the GreenDao ORM protocol
 */
class GreenDaoDatabase implements Database {

    private final DaoSession m_daoSession;

    public GreenDaoDatabase(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "ftamonitor-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        m_daoSession = master.newSession();
    }

    @Override
    public Collection<Match> getMatches(Team team, Event event, Note note) throws DatabaseException {
        List<Matches> databaseMatches = m_daoSession.getMatchesDao().loadAll();
        Collection<Match> matches = new ArrayList<>();
        for (Matches match : databaseMatches) {
            // If any of the inputs aren't null, then we must evaluate for matches. If none of the inputs
            // are valid, then we're retrieving the whole database, so we'll just add everything to the list
            boolean teamValid = team == null, eventValid = event == null, noteValid = note == null;
            if (!teamValid) {
                for (Matches_Teams matchTeamDb : match.getMatch_teams_id()) {
                    if (matchTeamDb.getTeam_match_team_id() == team.getId()) {
                        teamValid = true;
                        break;
                    }
                }
            }

            if (!eventValid) {
                eventValid = match.getEvents().getId() == event.getId();
            }

            if (!noteValid) {
                Notes noteDb = m_daoSession.getNotesDao().load(note.getId());
                noteValid = noteDb != null && noteDb.getNote_match_id() == match.getId();
            }

            if (teamValid && eventValid && noteValid) {
                matches.add(fromDB(match));
            }
        }

        return matches;
    }

    @Override
    public Collection<Team> getTeams(Event event, Match match, Note note) throws DatabaseException {
        // If there is nothing to filter by, then just return all events
        if (event == null && match == null && note == null) {
            return mapDb(m_daoSession.getTeamsDao(), new MapCall<Teams, Team>() {
                @Override
                public Team execute(Teams val) {
                    return fromDB(val);
                }
            });
        } else {
            // This algorithm works by checking each many-many relation where we have a filter argument
            // and executing a where, retrieving all valid relations. We then add 1 to the tableIn map under
            // the relation key. After the filtering is complete, we go through the map. If the integer in the
            // map equals numValidated, we retrieve it from the database and add it to the return collection
            int numValidated = 0;
            Map<Long, Integer> tableIn = new HashMap<>();

            // Events
            if (event != null) {
                numValidated++;

                // Load all relations from the team-event relation table
                List<Teams_Events> teamEventRelations = m_daoSession.getTeams_EventsDao()._queryEvents_Event_teams_id(event.getId());
                for (Teams_Events teamEventRelation : teamEventRelations) {
                    increaseKey(tableIn, teamEventRelation.getTeam_event_team_id());
                }
            }
            
            // Matches
            if (match != null) {
                numValidated++;
                
                // Load all relations from the team-match relation table
                List<Matches_Teams> teamMatchRelations = m_daoSession.getMatches_TeamsDao()._queryMatches_Match_teams_id(match.getId());
                for (Matches_Teams teamMatchRelation : teamMatchRelations) {
                    increaseKey(tableIn, teamMatchRelation.getTeam_match_team_id());
                }
            }

            // Note has a 1 to many, so we can just check the team of the note
            if (note != null) {
                numValidated++;
                Notes dbNote = m_daoSession.getNotesDao().load(note.getId());
                if (dbNote.getNote_team_id() != null) {
                    increaseKey(tableIn, dbNote.getNote_team_id());
                }
            }

            // Now loop through the found keys. Any that have been increased to equal numValidated have been matched, so
            // Include them in the return val
            Collection<Team> teams = new ArrayList<>();
            TeamsDao teamsDao = m_daoSession.getTeamsDao();
            for (Map.Entry<Long, Integer> pair : tableIn.entrySet()) {
                if (pair.getValue() == numValidated) {
                    teams.add(fromDB(teamsDao.load(pair.getKey())));
                }
            }

            return teams;
        }
    }

    @Override
    public Collection<Event> getEvents(Team team, Match match, Note note, DateTime year) throws DatabaseException {
        List<Events> dbEvents = m_daoSession.getEventsDao().loadAll();
        Collection<Event> events = new ArrayList<>();

        for (Events event : dbEvents) {
            boolean teamValid = team == null, matchValid = match == null, noteValid = note == null;
            if (!teamValid) {
                List<Teams_Events> teamEventsDb = event.getEvent_teams_id();
                for (Teams_Events teamEventDb : teamEventsDb) {
                    if (teamEventDb.getTeam_event_team_id() == team.getId()) {
                        teamValid = true;
                        break;
                    }
                }
            }

            if (!matchValid) {
                List<Matches> matchesDb = event.getEvent_matches_id();
                for (Matches matchDb : matchesDb) {
                    if (matchDb.getId() == match.getId()) {
                        matchValid = true;
                        break;
                    }
                }
            }

            if (!noteValid) {
                Notes noteDb = m_daoSession.getNotesDao().load(note.getId());
                noteValid = noteDb != null && noteDb.getId() == note.getId();
            }

            if (teamValid && matchValid && noteValid) {
                events.add(fromDB(event));
            }
        }

        return events;
    }

    @Override
    public Collection<Note> getNotes(Team team, Match match, Event event) throws DatabaseException {
        List<Notes> notesDb = m_daoSession.getNotesDao().loadAll();
        Collection<Note> notes = new ArrayList<>();

        for (Notes note : notesDb) {
            boolean teamValid = team == null, matchValid = match == null, eventValid = event == null;

            if (!teamValid) {
                teamValid = note.getNote_team_id() == team.getId();
            }

            if (!eventValid) {
                eventValid = note.getNote_event_id() == event.getId();
            }

            if (!matchValid) {
                matchValid = note.getNote_match_id() == match.getId();
            }

            if (teamValid && eventValid && matchValid) {
                notes.add(fromDB(note));
            }
        }

        return notes;
    }

    @Override
    public void saveMatch(Match... matches) throws DatabaseException {

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
        for (Note note : notes) {
            m_daoSession.getNotesDao().deleteByKey(note.getId());
        }
    }

    private Match fromDB(Matches match) {
        return AssistantFactory.getInstance().makeMatch(
                match.getId(),
                MatchPeriod.fromOrd(match.getMatch_period()),
                match.getMatch_identifier(),
                match.getReplay());
    }

    private Event fromDB(Events event) {
        return AssistantFactory.getInstance().makeEvent(
                event.getId(),
                event.getEvent_code(),
                event.getEvent_name(),
                event.getEvent_loc(),
                new DateTime(event.getStart_date()),
                new DateTime(event.getEnd_date()));
    }

    private Team fromDB(Teams team) {
        return AssistantFactory.getInstance().makeTeam(
                team.getId(),
                team.getTeam_number(),
                team.getTeam_name(),
                team.getTeam_nick());
    }

    private Note fromDB(Notes note) {
        return AssistantFactory.getInstance().makeNote(note.getId(), note.getContent());
    }

    /**
     * Implementation of map for transforming a list of DB objects to a list of real objects by calling a function.
     *
     * @param db     The db object to query
     * @param mapper The {@link GreenDaoDatabase.MapCall} implementation to call
     * @param <Ret>  The type of collection to that MapCall returns
     * @param <Arg>  The db type
     * @param <Dao>  The DAO that returns Arg type
     * @param <M>    The type of the mapcall that maps from arg to ret
     * @return The mapped list
     */
    private <Ret, Arg, Dao extends AbstractDao<Arg, Long>, M extends MapCall<Arg, Ret>> Collection<Ret> mapDb(Dao db, M mapper) {
        Collection<Ret> returnVal = new ArrayList<>();
        for (Arg arg : db.loadAll()) {
            returnVal.add(mapper.execute(arg));
        }

        return returnVal;
    }

    private void increaseKey(Map<Long, Integer> map, Long key) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    /**
     * Defines a call that maps from T to U
     *
     * @param <T> The argument type
     * @param <U> The return type
     */
    private interface MapCall<T, U> {
        U execute(T val);
    }
}
