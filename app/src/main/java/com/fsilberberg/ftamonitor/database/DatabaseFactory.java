package com.fsilberberg.ftamonitor.database;

import android.content.Context;

/**
 * Factory for getting database objects
 */
public class DatabaseFactory {

    private static DatabaseFactory instance;

    public static void initializeDatabase(Context context) {
        instance = new DatabaseFactory(context);
    }

    public static DatabaseFactory getInstance() {
        if (instance == null) {
            throw new RuntimeException("Error: You must call DatabaseFactory.Initialze before attempting to get the database factory instance");
        }

        return instance;
    }

    private final Context m_context;
    private final Database m_database;

    private DatabaseFactory(Context context) {
        m_context = context;
        m_database = new FTAMonitorDatabase(m_context);
    }

    /**
     * Gets the {@link Database} implementation used by this program
     *
     * @return The Database
     */
    public Database getDatabase() {
        return m_database;
    }
}
