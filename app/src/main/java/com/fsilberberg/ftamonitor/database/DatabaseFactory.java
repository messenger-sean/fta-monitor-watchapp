package com.fsilberberg.ftamonitor.database;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Factory for getting database objects
 */
public class DatabaseFactory {

    private static DatabaseFactory instance;
    private static DaoSession session;

    public static DatabaseFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseFactory();
        }

        return instance;
    }


    private DatabaseFactory() {
    }

    /**
     * Gets the {@link Database} implementation used by this program. This Database <b>MUST</b> be released after use,
     * with the {@link #release(Database)} method.
     *
     * @return The Database
     */
    public Database getDatabase(Context context) {
        return new OrmLiteDatabase(OpenHelperManager.getHelper(context, OrmLiteDatabaseHelper.class));
    }

    /**
     * Releases the db instance, so that the usage counter is decremented. After release is called, you should call
     * getDatabase again before attempting to use the database
     *
     * @param db The db to release.
     */
    public void release(Database db) {
        if (db instanceof OrmLiteDatabase) {
            OrmLiteDatabase ormDb = (OrmLiteDatabase) db;
            ormDb.release();
        }
    }
}
