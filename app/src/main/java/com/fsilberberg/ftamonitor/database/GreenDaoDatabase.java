package com.fsilberberg.ftamonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.ftaassistant.*;
import de.greenrobot.dao.query.QueryBuilder;
import org.joda.time.DateTime;

import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        List<Teams> dbTeams = m_daoSession.getTeamsDao().loadAll();
        Collection<Team> teams = new ArrayList<>();

        for (Teams team : dbTeams) {
            boolean eventValid = event == null, matchValid = match == null, noteValid = note == null;
            if (!eventValid) {
                for (Teams_Events eventsDb : team.getTeam_events_id()) {
                    if (eventsDb.getTeam_event_event_id() == event.getId()) {
                        eventValid = true;
                        break;
                    }
                }
            }

            if (!matchValid) {
                for (Matches_Teams matchDb : team.getTeam_matches_id()) {
                    if (matchDb.getTeam_match_match_id() == match.getId()) {
                        matchValid = true;
                        break;
                    }
                }
            }

            if (!noteValid) {
                Notes noteDb = m_daoSession.getNotesDao().load(note.getId());
                noteValid = noteDb != null && noteDb.getNote_team_id() == team.getId();
            }

            if (eventValid && matchValid && noteValid) {
                teams.add(fromDB(team));
            }
        }

        return teams;
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

}
