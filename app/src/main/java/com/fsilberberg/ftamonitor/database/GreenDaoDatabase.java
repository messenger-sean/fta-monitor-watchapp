package com.fsilberberg.ftamonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.ftaassistant.*;
import de.greenrobot.dao.AbstractDao;
import org.joda.time.DateTime;

import java.util.*;

/**
 * This is a database implementation that uses the GreenDao ORM protocol. The getX filtration methods use an algorithm
 * for retrieving matches from the database by checking all relations in O(n) time. This algorithm works by checking each
 * many-many relation where we have a filter argument and executing a where, retrieving all valid relations. We then add 1
 * to the tableIn map under the relation key. After the filtering is complete, we go through the map. If the integer in the
 * map equals numValidated, we retrieve it from the database and add it to the return collection
 */
class GreenDaoDatabase implements Database {

    /**
     * Implementations of {@link GreenDaoDatabase.MapCall} for each of the db types
     */

    private static final MapCall<Teams, Team> teamMapper = new MapCall<Teams, Team>() {
        @Override
        public Team execute(Teams val) {
            return fromDB(val);
        }
    };

    private static final MapCall<Events, Event> eventMapper = new MapCall<Events, Event>() {
        @Override
        public Event execute(Events val) {
            return fromDB(val);
        }
    };

    private static final MapCall<Matches, Match> matchMapper = new MapCall<Matches, Match>() {
        @Override
        public Match execute(Matches val) {
            return fromDB(val);
        }
    };

    private static final MapCall<Notes, Note> noteMapper = new MapCall<Notes, Note>() {
        @Override
        public Note execute(Notes val) {
            return fromDB(val);
        }
    };

    private final DaoSession m_daoSession;

    public GreenDaoDatabase(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "ftamonitor-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        m_daoSession = master.newSession();
    }

    @Override
    public Collection<Match> getMatches(Team team, Event event, Note note) throws DatabaseException {
        if (team == null && event == null && note == null) {
            return mapDb(m_daoSession.getMatchesDao(), matchMapper);
        } else {
            int numValidated = 0;
            Map<Long, Integer> matchIn = new HashMap<>();

            if (team != null) {
                numValidated++;

                List<Matches_Teams> matchesTeamsRelations = m_daoSession.getMatches_TeamsDao()._queryTeams_Team_matches_id(team.getId());
                for (Matches_Teams relation : matchesTeamsRelations) {
                    increaseKey(matchIn, relation.getTeam_match_match_id());
                }
            }

            if (event != null) {
                numValidated++;

                Events events = m_daoSession.getEventsDao().load(event.getId());
                for (Matches match : events.getEvent_matches_id()) {
                    increaseKey(matchIn, match.getId());
                }
            }

            // Note is a 1 to many relation, so just load the note and check the relations
            if (note != null) {
                numValidated++;
                Notes dbNote = m_daoSession.getNotesDao().load(note.getId());
                if (dbNote != null && dbNote.getNote_team_id() != null) {
                    increaseKey(matchIn, dbNote.getNote_match_id());
                }
            }

            return loadMatching(m_daoSession.getMatchesDao(), matchMapper, matchIn, numValidated);
        }
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
            int numValidated = 0;
            Map<Long, Integer> tableIn = new HashMap<>();

            // Events
            if (event != null) {
                numValidated++;

                // Load all relations from the team-event relation table
                List<Teams_Events> teamEventRelations = m_daoSession.getTeams_EventsDao()._queryTeams_Team_events_id(event.getId());
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
                if (dbNote != null && dbNote.getNote_team_id() != null) {
                    increaseKey(tableIn, dbNote.getNote_team_id());
                }
            }

            return loadMatching(m_daoSession.getTeamsDao(), teamMapper, tableIn, numValidated);
        }
    }

    @Override
    public Collection<Event> getEvents(Team team, Match match, Note note, DateTime year) throws DatabaseException {
        Collection<Event> unfilteredYear = new ArrayList<>();
        if (team == null && match == null && note == null) {
            unfilteredYear = mapDb(m_daoSession.getEventsDao(), eventMapper);
        } else {
            int numValidated = 0;
            Map<Long, Integer> tableIn = new HashMap<>();

            if (team != null) {
                numValidated++;

                // Load all relations from the team-event relation table
                List<Teams_Events> teamEventRelations = m_daoSession.getTeams_EventsDao()._queryEvents_Event_teams_id(team.getId());
                for (Teams_Events teamEventRelation : teamEventRelations) {
                    increaseKey(tableIn, teamEventRelation.getTeam_event_event_id());
                }
            }

            // This is a 1 to many relation, so here we just load the match and increase its key
            if (match != null) {
                numValidated++;
                Matches dbMatch = m_daoSession.getMatchesDao().load(match.getId());
                if (dbMatch != null && dbMatch.getMatch_events_id() != null) {
                    increaseKey(tableIn, dbMatch.getMatch_events_id());
                }
            }

            // This is also a 1 to many relation, so same thing
            if (note != null) {
                numValidated++;
                Notes dbNotes = m_daoSession.getNotesDao().load(note.getId());
                if (dbNotes != null && dbNotes.getNote_event_id() != null) {
                    increaseKey(tableIn, dbNotes.getNote_event_id());
                }
            }

            unfilteredYear = loadMatching(m_daoSession.getEventsDao(), eventMapper, tableIn, numValidated);
        }

        // Filter out any events that don't match the year
        Collection<Event> retVal = new ArrayList<>();
        for (Event event : unfilteredYear) {
            if (event.getStartDate().year().equals(year.year())) {
                retVal.add(event);
            }
        }
        return retVal;
    }

    @Override
    public Collection<Note> getNotes(Team team, Match match, Event event) throws DatabaseException {
        if (team == null && match == null && event == null) {
            return mapDb(m_daoSession.getNotesDao(), noteMapper);
        } else {
            int numValidated = 0;
            Map<Long, Integer> tableIn = new HashMap<>();

            if (team != null) {
                numValidated++;
                Teams dbTeam = m_daoSession.getTeamsDao().load(team.getId());
                if (dbTeam != null) {
                    for (Notes note : dbTeam.getTeam_notes_id()) {
                        increaseKey(tableIn, note.getId());
                    }
                }
            }

            if (event != null) {
                numValidated++;
                Events dbEvent = m_daoSession.getEventsDao().load(event.getId());
                if (dbEvent != null) {
                    for (Notes note : dbEvent.getEvent_notes_id()) {
                        increaseKey(tableIn, note.getId());
                    }
                }
            }

            if (match != null) {
                numValidated++;
                Matches dbMatches = m_daoSession.getMatchesDao().load(match.getId());
                if (dbMatches != null) {
                    for (Notes note : dbMatches.getMatch_notes_id()) {
                        increaseKey(tableIn, note.getId());
                    }
                }
            }

            return loadMatching(m_daoSession.getNotesDao(), noteMapper, tableIn, numValidated);
        }
    }

    @Override
    public void saveMatch(Match... matches) throws DatabaseException {
        for (Match match : matches) {
            Matches dbMatch = toDB(match);
            if (match.getId() == AssistantFactory.NO_ID) {
                m_daoSession.getMatchesDao().insert(dbMatch);
            } else {
                m_daoSession.getMatchesDao().update(dbMatch);
            }
        }
    }

    @Override
    public void saveTeam(Team... teams) throws DatabaseException {
        for (Team team : teams) {
            Teams dbTeam = toDB(team);
            if (team.getId() == AssistantFactory.NO_ID) {
                m_daoSession.getTeamsDao().insert(dbTeam);
            } else {
                m_daoSession.getTeamsDao().update(dbTeam);
            }
        }
    }

    @Override
    public void saveEvent(Event... events) throws DatabaseException {
        for (Event event : events) {
            Events dbEvent = toDB(event);
            if (event.getId() == AssistantFactory.NO_ID) {
                m_daoSession.getEventsDao().insert(dbEvent);
            } else {
                m_daoSession.getEventsDao().update(dbEvent);
            }
        }
    }

    @Override
    public void saveNote(Note... notes) throws DatabaseException {
        for (Note note : notes) {
            Notes dbNote = toDB(note);
            if (note.getId() == AssistantFactory.NO_ID) {
                m_daoSession.getNotesDao().insert(dbNote);
            } else {
                m_daoSession.getNotesDao().update(dbNote);
            }
        }
    }

    @Override
    public void deleteNote(Note... notes) throws DatabaseException {
        for (Note note : notes) {
            m_daoSession.getNotesDao().deleteByKey(note.getId());
        }
    }

    private static Match fromDB(Matches match) {
        return AssistantFactory.getInstance().makeMatch(
                match.getId(),
                MatchPeriod.fromOrd(match.getMatch_period()),
                match.getMatch_identifier(),
                match.getReplay());
    }

    private static Matches toDB(Match match) {
        return new Matches(
                match.getId(),
                match.getMatchId().getIdentifier(),
                match.getMatchId().getPeriod().ordinal(),
                match.getMatchId().getReplay(),
                match.getEvent() != null ? match.getEvent().getId() : null);
    }

    private static Event fromDB(Events event) {
        return AssistantFactory.getInstance().makeEvent(
                event.getId(),
                event.getEvent_code(),
                event.getEvent_name(),
                event.getEvent_loc(),
                new DateTime(event.getStart_date()),
                new DateTime(event.getEnd_date()));
    }

    private static Events toDB(Event event) {
        return new Events(
                event.getId(),
                event.getEventCode(),
                event.getEventName(),
                event.getStartDate().toDate(),
                event.getEndDate().toDate(),
                event.getEventLoc());
    }

    private static Team fromDB(Teams team) {
        return AssistantFactory.getInstance().makeTeam(
                team.getId(),
                team.getTeam_number(),
                team.getTeam_name(),
                team.getTeam_nick());
    }

    private static Teams toDB(Team team) {
        return new Teams(
                team.getId(),
                team.getTeamNumber(),
                team.getTeamName(),
                team.getTeamNick());
    }

    private static Note fromDB(Notes note) {
        return AssistantFactory.getInstance().makeNote(note.getId(), note.getContent());
    }

    private static Notes toDB(Note note) {
        return new Notes(
                note.getId(),
                note.getContent(),
                note.getTeam() == null ? null : note.getTeam().getId(),
                note.getMatch() == null ? null : note.getMatch().getId(),
                note.getEvent() == null ? null : note.getMatch().getId());
    }

    /**
     * Executes the map-if-validated algorithm, that loops through the entries in a given map and returns the loaded value
     * if the entry has been matched numValidated number of times
     *
     * @param db           The db to query
     * @param mapper       The transformation function from Arg to Ret
     * @param map          The map to loop through
     * @param numValidated The cutoff number for the algorithm
     * @param <Ret>        The return type of the Collection
     * @param <Arg>        The argument type to load from the db
     * @return
     */
    private <Ret, Arg> Collection<Ret> loadMatching(AbstractDao<Arg, Long> db,
                                                    MapCall<Arg, Ret> mapper,
                                                    Map<Long, Integer> map,
                                                    int numValidated) {
        Collection<Ret> retList = new ArrayList<>();
        for (Map.Entry<Long, Integer> pair : map.entrySet()) {
            if (pair.getValue() == numValidated) {
                retList.add(mapper.execute(db.load(pair.getKey())));
            }
        }

        return retList;
    }

    /**
     * Implementation of map for transforming a list of DB objects to a list of real objects by calling a function.
     *
     * @param db     The db object to query
     * @param mapper The {@link GreenDaoDatabase.MapCall} implementation to call
     * @param <Ret>  The type of collection to that MapCall returns
     * @param <Arg>  The db type
     * @return The mapped list
     */
    private <Ret, Arg> Collection<Ret> mapDb(AbstractDao<Arg, Long> db, MapCall<Arg, Ret> mapper) {
        Collection<Ret> returnVal = new ArrayList<>();
        for (Arg arg : db.loadAll()) {
            returnVal.add(mapper.execute(arg));
        }

        return returnVal;
    }

    /**
     * Increases the value of a key in the given map. If the key does not yet exist, it adds it to the map
     *
     * @param map The map to increase
     * @param key The key to increase
     */
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
