package com.fsilberberg.ftamonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.fsilberberg.ftamonitor.database.ormmodels.*;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * OrmLite DAO helper
 */
public class OrmLiteDatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DB_NAME = "ftamonitor.db";
    private static final int DB_VERSION = 1;

    private Dao<OrmEvent, Long> m_eventDao;
    private Dao<OrmMatch, Long> m_matchDao;
    private Dao<OrmNote, Long> m_noteDao;
    private Dao<OrmTeam, Long> m_teamDao;
    private Dao<OrmTeamEvent, Long> m_teamEventDao;

    public OrmLiteDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.i(OrmLiteDatabaseHelper.class.getName(), "Creating OrmLite database");
        try {
            TableUtils.createTable(connectionSource, OrmEvent.class);
            TableUtils.createTable(connectionSource, OrmMatch.class);
            TableUtils.createTable(connectionSource, OrmNote.class);
            TableUtils.createTable(connectionSource, OrmTeam.class);
            TableUtils.createTable(connectionSource, OrmTeamEvent.class);
            Log.i(OrmLiteDatabaseHelper.class.getName(), "Created database");
        } catch (SQLException e) {
            Log.e(OrmLiteDatabaseHelper.class.getName(), "Could not create database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // TODO: If the database is upgraded, create the ALTER_TABLE instructions here
    }

    public Dao<OrmEvent, Long> getEventDao() throws SQLException {
        if (m_eventDao == null) {
            m_eventDao = getDao(OrmEvent.class);
        }

        return m_eventDao;
    }

    public Dao<OrmMatch, Long> getMatchDao() throws SQLException {
        if (m_matchDao == null) {
            m_matchDao = getDao(OrmMatch.class);
        }

        return m_matchDao;
    }

    public Dao<OrmNote, Long> getNoteDao() throws SQLException {
        if (m_noteDao == null) {
            m_noteDao = getDao(OrmNote.class);
        }

        return m_noteDao;
    }

    public Dao<OrmTeam, Long> getTeamDao() throws SQLException {
        if (m_teamDao == null) {
            m_teamDao = getDao(OrmTeam.class);
        }

        return m_teamDao;
    }

    public Dao<OrmTeamEvent, Long> getTeamEventDao() throws SQLException {
        if (m_teamEventDao == null) {
            m_teamEventDao = getDao(OrmTeamEvent.class);
        }

        return m_teamEventDao;
    }

}
