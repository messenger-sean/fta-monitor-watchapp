package com.fsilberberg.ftamonitor.database;

import android.util.Log;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.database.ormmodels.*;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of the {@link com.fsilberberg.ftamonitor.database.Database} interface for OrmLite
 */
public class OrmLiteDatabase implements Database {

    private OrmLiteDatabaseHelper m_helper;

    OrmLiteDatabase(OrmLiteDatabaseHelper helper) {
        m_helper = helper;
    }

    public void release() {
        OpenHelperManager.releaseHelper();
    }


    @Override
    public Optional<Match> getMatch(MatchPeriod period, String matchIdentifier, int replay, Event event) {
        Optional<Match> retVal = Optional.absent();
        try {
            Dao<OrmMatch, Long> matchDao = m_helper.getMatchDao();
            QueryBuilder<OrmMatch, Long> qb = matchDao.queryBuilder();
            qb.where().eq(OrmMatch.MATCH_IDENTIFIER, matchIdentifier)
                    .eq(OrmMatch.PERIOD, period)
                    .eq(OrmMatch.REPLAY, replay)
                    .eq(OrmMatch.EVENT, event.getId());
            Collection<OrmMatch> matches = matchDao.query(qb.prepare());
            if (!matches.isEmpty()) {
                retVal = Optional.of((Match) matches.iterator().next());
            }
        } catch (SQLException e) {
            Log.w(OrmLiteDatabase.class.getName(), "Could not look up match with parameters " +
                    "\nPeriod: " + period +
                    "\nMatch Identifier: " + matchIdentifier +
                    "\nReplay: " + replay +
                    "\nEvent: " + event, e);
        }
        return retVal;
    }

    @Override
    public Optional<Team> getTeam(int number) {
        Optional<Team> retVal = Optional.absent();
        try {
            Dao<OrmTeam, Long> teamDao = m_helper.getTeamDao();
            QueryBuilder<OrmTeam, Long> qb = teamDao.queryBuilder();
            qb.where().eq(OrmTeam.TEAM_NUMBER, number);
            Collection<OrmTeam> teams = teamDao.query(qb.prepare());
            if (!teams.isEmpty()) {
                retVal = Optional.of((Team) teams.iterator().next());
            }
        } catch (SQLException e) {
            Log.w(OrmLiteDatabase.class.getName(), "Could not look up team with number " + number);
        }
        return retVal;
    }

    @Override
    public Optional<Event> getEvent(DateTime year, String eventCode) {
        Optional<Event> retVal = Optional.absent();
        try {
            QueryBuilder<OrmEvent, Long> qb = getEventDateQuery(year);
            qb.where().eq(OrmEvent.EVENT_CODE, eventCode);
            Collection<OrmEvent> events = m_helper.getEventDao().query(qb.prepare());
            if (!events.isEmpty()) {
                retVal = Optional.of((Event) events.iterator().next());
            }
        } catch (SQLException e) {
            Log.w(OrmLiteDatabase.class.getName(), "Could not query for events in year " + year.getYear()
                    + " with code " + eventCode, e);
        }

        return retVal;
    }

    @Override
    public Collection<? extends Event> getEvents(DateTime year) {
        try {
            return m_helper.getEventDao().query(getEventDateQuery(year).prepare());
        } catch (SQLException e) {
            Log.w(OrmLiteDatabase.class.getName(), "Could not query for events in year " + year.getYear(), e);
        }
        return new ArrayList<>();
    }

    private QueryBuilder<OrmEvent, Long> getEventDateQuery(DateTime year) throws SQLException {
        // Get the start of the year
        DateTime yearStart = new DateTime(year.getYear(), 1, 1, 0, 0, 0, 0);
        DateTime yearEnd = new DateTime(year.getYear(), 12, 31, 23, 59, 59, 59);
        QueryBuilder<OrmEvent, Long> qb = m_helper.getEventDao().queryBuilder();
        qb.where().between(OrmEvent.START_DATE, yearStart.toDate(), yearEnd.toDate());
        return qb;
    }

    @Override
    public void saveMatch(Match... matches) throws DatabaseException {
        for (Match m : matches) {
            OrmMatch match = OrmMatch.copyMapper.apply(m);
            try {
                m_helper.getMatchDao().createOrUpdate(match);
            } catch (SQLException e) {
                Log.w(OrmLiteDatabase.class.getName(), "Could not save match", e);
            }
        }
    }

    @Override
    public void saveTeam(Team... teams) throws DatabaseException {
        for (Team t : teams) {
            OrmTeam team = OrmTeam.copyMapper.apply(t);
            try {
                m_helper.getTeamDao().createOrUpdate(team);

                Collection<OrmEvent> events = team.getEventsNoRefresh();
                if (events != null) {
                    // Get all relations from the table involving this team, and loop through them. If they're in the
                    // events list, do nothing. If they're not, remove them from the database. Remove them from the
                    // events list, and at the end, if there are still events in the events list, add them in
                    for (final OrmTeamEvent te : getTeamEventList(OrmTeamEvent.TEAM_ID, team.getId())) {
                        final Predicate<OrmEvent> testPredicate = new Predicate<OrmEvent>() {
                            @Override
                            public boolean apply(OrmEvent input) {
                                return te.getEvent().getId() == input.getId();
                            }
                        };
                        if (Iterables.any(events, testPredicate)) {
                            events.removeAll(Collections2.filter(events, testPredicate));
                        } else {
                            m_helper.getTeamEventDao().delete(te);
                        }
                    }

                    for (OrmEvent event : events) {
                        OrmTeamEvent te = new OrmTeamEvent(team, event);
                        m_helper.getTeamEventDao().create(te);
                    }
                }
            } catch (SQLException e) {
                Log.w(OrmLiteDatabase.class.getName(), "Could not save team", e);
            }
        }
    }

    @Override
    public void saveEvent(Event... events) throws DatabaseException {
        for (Event e : events) {
            OrmEvent event = OrmEvent.copyMapper.apply(e);
            try {
                m_helper.getEventDao().createOrUpdate(event);

                // Loop through all teamevent pairs. If the team in the pair is in this collection, remove it from
                // this collection. Otherwise, remove the pair from the database. At the end, if there are any new
                // teams, add them to the database
                Collection<OrmTeam> teams = event.getTeamsNoRefresh();
                if (teams != null) {
                    for (final OrmTeamEvent te : getTeamEventList(OrmTeamEvent.EVENT_ID, event.getId())) {
                        Predicate<OrmTeam> testPredicate = new Predicate<OrmTeam>() {
                            @Override
                            public boolean apply(OrmTeam input) {
                                return input.getId() == te.getTeam().getId();
                            }
                        };

                        if (Iterables.any(teams, testPredicate)) {
                            teams.removeAll(Collections2.filter(teams, testPredicate));
                        } else {
                            m_helper.getTeamEventDao().delete(te);
                        }
                    }

                    for (OrmTeam team : teams) {
                        OrmTeamEvent te = new OrmTeamEvent(team, event);
                        m_helper.getTeamEventDao().create(te);
                    }
                }
            } catch (SQLException ex) {
                Log.w(OrmLiteDatabase.class.getName(), "Could not save event", ex);
            }
        }
    }

    @Override
    public void saveNote(Note... notes) throws DatabaseException {
        for (Note n : notes) {
            OrmNote note = OrmNote.copyMapper.apply(n);
            try {
                m_helper.getNoteDao().createOrUpdate(note);
            } catch (SQLException e) {
                Log.w(OrmLiteDatabase.class.getName(), "Could not save note", e);
            }
        }
    }

    @Override
    public void deleteNote(Note... notes) throws DatabaseException {
        for (Note n : notes) {
            OrmNote note = OrmNote.copyMapper.apply(n);
            try {
                m_helper.getNoteDao().delete(note);
            } catch (SQLException e) {
                Log.w(OrmLiteDatabase.class.getName(), "Could not delete note", e);
            }
        }
    }

    private Collection<OrmTeamEvent> getTeamEventList(String col, Long id) throws SQLException {
        QueryBuilder<OrmTeamEvent, Long> qb = m_helper.getTeamEventDao().queryBuilder();
        qb.where().eq(col, id);
        return m_helper.getTeamEventDao().query(qb.prepare());
    }
}
