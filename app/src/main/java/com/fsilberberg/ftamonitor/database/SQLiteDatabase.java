package com.fsilberberg.ftamonitor.database;

import android.content.Context;

import java.sql.SQLClientInfoException;

/**
 * Implementation of the Database that uses the SQLite definitions defined by {@link com.fsilberberg.ftamonitor.database.FTAMonitorContract}
 * and the SQLite Helper {@link com.fsilberberg.ftamonitor.database.SQLiteDatabaseHelper}
 */
public class SQLiteDatabase implements IDatabase {

    private final Context m_context;
    private final SQLiteDatabaseHelper m_helper;

    SQLiteDatabase(Context context) {
        m_context = context;
        m_helper = new SQLiteDatabaseHelper(context);
    }

}
