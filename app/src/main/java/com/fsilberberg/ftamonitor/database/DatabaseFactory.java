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
    private final IDatabase m_database;

    private DatabaseFactory(Context context) {
        m_context = context;
        m_database = new SQLiteDatabase(m_context);
    }

    /**
     * Gets the {@link com.fsilberberg.ftamonitor.database.IDatabase} implementation used by this program
     *
     * @return The Database
     */
    public IDatabase getDatabase() {
        return m_database;
    }
}
