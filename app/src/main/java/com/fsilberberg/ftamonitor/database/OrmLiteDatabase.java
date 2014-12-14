package com.fsilberberg.ftamonitor.database;

import android.util.Log;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmEvent;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmMatch;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmTeam;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Optional;
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
        for (Match match : matches) {
            if (match instanceof OrmMatch) {
                OrmMatch m = (OrmMatch) match;
                try {
                    m_helper.getMatchDao().createOrUpdate(m);
                } catch (SQLException e) {
                    Log.w(OrmLiteDatabase.class.getName(), "Could not save match", e);
                }
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
}
