package com.fsilberberg.ftamonitor.database;

import android.util.Log;
import com.fsilberberg.ftamonitor.database.ormmodels.OrmMatch;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import org.joda.time.DateTime;

import java.sql.SQLException;
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
    public Collection<Match> getMatches(Team team, Event event, Note note) throws DatabaseException {
        if (team == null && event == null && note == null) {
            return m_helper.getMatchDao().queryForAll();
        }

        return null;
    }

    @Override
    public Collection<Team> getTeams(Event event, Match match, Note note) throws DatabaseException {
        return null;
    }

    @Override
    public Collection<Event> getEvents(Team team, Match match, Note note, DateTime year) throws DatabaseException {
        return null;
    }

    @Override
    public Collection<Note> getNotes(Team team, Match match, Event event) throws DatabaseException {
        return null;
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
