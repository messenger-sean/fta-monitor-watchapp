package com.fsilberberg.ftamonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fsilberberg.ftamonitor.database.FTAMonitorContract.*;

/**
 * SQLite open helper that defines the database schema based on {@link com.fsilberberg.ftamonitor.database.FTAMonitorContract}
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    // Constants
    private static final String CREATE_TABLE = "CREATE_TABLE";
    private static final String COMMA = ", ";
    private static final String OPEN_PAREN = " (";
    private static final String CLOSE_PAREN = ")";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String FOREIGN_KEY = "FOREIGN KEY (";
    private static final String REFERENCES = ") REFERENCES ";
    private static final String NL = "\n";
    private static final String NOT_NULL = " NOT NULL";
    private static final String AUTO_INC = " AUTO INCREMENT";
    private static final String DELETE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    // SQL Types
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";

    // Team Table
    private static final String CREATE_TEAM_TABLE =
            CREATE_TABLE + Team.TABLE_NAME + OPEN_PAREN + NL +
                    Team._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    Team.TEAM_NUMBER + INT_TYPE + NOT_NULL + COMMA + NL +
                    Team.TEAM_NAME + TEXT_TYPE + NOT_NULL + CLOSE_PAREN;

    // Event Table
    private static final String CREATE_EVENT_TABLE =
            CREATE_TABLE + Event.TABLE_NAME + OPEN_PAREN + NL +
                    Event._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    Event.EVENT_CODE + TEXT_TYPE + NOT_NULL + COMMA + NL +
                    Event.EVENT_NAME + TEXT_TYPE + NOT_NULL + COMMA + NL +
                    Event.EVENT_LOC + TEXT_TYPE + COMMA + NL +
                    Event.START_DATE + TEXT_TYPE + COMMA + NL +
                    Event.END_DATE + TEXT_TYPE + CLOSE_PAREN;

    // Matches table
    private static final String CREATE_MATCH_TABLE =
            CREATE_TABLE + Match.TABLE_NAME + OPEN_PAREN + NL +
                    Match._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    Match.MATCH_IDENTIFIER + TEXT_TYPE + NOT_NULL + COMMA + NL +
                    Match.MATCH_PERIOD + INT_TYPE + NOT_NULL + COMMA + NL +
                    Match.REPLAY + INT_TYPE + NOT_NULL + CLOSE_PAREN;

    // Notes table
    private static final String CREATE_NOTES_TABLE =
            CREATE_TABLE + Notes.TABLE_NAME + OPEN_PAREN + NL +
                    Notes._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    Notes.CONTENT + TEXT_TYPE + NOT_NULL + CLOSE_PAREN;

    // Team Events table
    private static final String CREATE_TEAM_EVENTS_TABLE =
            CREATE_TABLE + Teams_Events.TABLE_NAME + OPEN_PAREN + NL +
                    Teams_Events._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    FOREIGN_KEY + Teams_Events.TEAM + REFERENCES +
                    Team.TABLE_NAME + OPEN_PAREN + Team._ID + CLOSE_PAREN + COMMA + NL +
                    FOREIGN_KEY + Teams_Events.EVENT + REFERENCES +
                    Event.TABLE_NAME + OPEN_PAREN + Event._ID + CLOSE_PAREN + CLOSE_PAREN;

    // Match Events table
    private static final String CREATE_MATCHES_EVENT_TABLE =
            CREATE_TABLE + Matches_Event.TABLE_NAME + OPEN_PAREN + NL +
                    Matches_Event._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    FOREIGN_KEY + Matches_Event.MATCH + REFERENCES +
                    Match.TABLE_NAME + OPEN_PAREN + Match._ID + CLOSE_PAREN + COMMA + NL +
                    FOREIGN_KEY + Matches_Event.EVENT + REFERENCES +
                    Event.TABLE_NAME + OPEN_PAREN + Event._ID + CLOSE_PAREN + CLOSE_PAREN;

    // Matches Teams table
    private static final String CREATE_MATCHES_TEAMS_TABLE =
            CREATE_TABLE + Matches_Teams.TABLE_NAME + OPEN_PAREN + NL +
                    Matches_Teams._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    FOREIGN_KEY + Matches_Teams.TEAM + REFERENCES +
                    Team.TABLE_NAME + OPEN_PAREN + Team._ID + CLOSE_PAREN + COMMA + NL +
                    FOREIGN_KEY + Matches_Teams.MATCH + REFERENCES +
                    Match.TABLE_NAME + OPEN_PAREN + Match._ID + CLOSE_PAREN + CLOSE_PAREN;

    // Team Notes table
    private static final String CREATE_TEAM_NOTES_TABLE =
            CREATE_TABLE + Team_Notes.TABLE_NAME + OPEN_PAREN + NL +
                    Team_Notes._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    FOREIGN_KEY + Team_Notes.TEAM + REFERENCES +
                    Team.TABLE_NAME + OPEN_PAREN + Team._ID + CLOSE_PAREN + COMMA + NL +
                    FOREIGN_KEY + Team_Notes.NOTE + REFERENCES +
                    Notes.TABLE_NAME + OPEN_PAREN + Notes._ID + CLOSE_PAREN + CLOSE_PAREN;

    // Event Notes table
    private static final String CREATE_EVENT_NOTES_TABLE =
            CREATE_TABLE + Event_Notes.TABLE_NAME + OPEN_PAREN + NL +
                    Event_Notes._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    FOREIGN_KEY + Event_Notes.EVENT + REFERENCES +
                    Event.TABLE_NAME + OPEN_PAREN + Event._ID + CLOSE_PAREN + COMMA + NL +
                    FOREIGN_KEY + Event_Notes.NOTE + REFERENCES +
                    Notes.TABLE_NAME + OPEN_PAREN + Notes._ID + CLOSE_PAREN + CLOSE_PAREN;

    // Match Notes table
    private static final String CREATE_MATCH_NOTES_TABLE =
            CREATE_TABLE + Match_Notes.TABLE_NAME + OPEN_PAREN + NL +
                    Match_Notes._ID + INT_TYPE + PRIMARY_KEY + AUTO_INC + COMMA + NL +
                    FOREIGN_KEY + Match_Notes.MATCH + REFERENCES +
                    Match.TABLE_NAME + OPEN_PAREN + Match._ID + CLOSE_PAREN + COMMA + NL +
                    FOREIGN_KEY + Match_Notes.NOTE + REFERENCES +
                    Notes.TABLE_NAME + OPEN_PAREN + Notes._ID + CLOSE_PAREN + CLOSE_PAREN;

    // Delete table statements
    private static final String DELETE_TEAM_TABLE = DELETE_IF_EXISTS + Team.TABLE_NAME;
    private static final String DELETE_EVENT_TABLE = DELETE_IF_EXISTS + Event.TABLE_NAME;
    private static final String DELETE_MATCH_TABLE = DELETE_IF_EXISTS + Match.TABLE_NAME;
    private static final String DELETE_NOTES_TABLE = DELETE_IF_EXISTS + Notes.TABLE_NAME;
    private static final String DELETE_TEAM_EVENTS_TABLE = DELETE_IF_EXISTS + Teams_Events.TABLE_NAME;
    private static final String DELETE_MATCHES_EVENT_TABLE = DELETE_IF_EXISTS + Matches_Event.TABLE_NAME;
    private static final String DELETE_MATCHES_TEAMS_TABLE = DELETE_IF_EXISTS + Matches_Teams.TABLE_NAME;
    private static final String DELETE_TEAM_NOTES_TABLE = DELETE_IF_EXISTS + Team_Notes.TABLE_NAME;
    private static final String DELETE_EVENT_NOTES_TABLE = DELETE_IF_EXISTS + Event_Notes.TABLE_NAME;
    private static final String DELETE_MATCH_NOTES_TABLE = DELETE_IF_EXISTS + Match_Notes.TABLE_NAME;


    public SQLiteDatabaseHelper(Context context) {
        super(context, FTAMonitorContract.DATABASE_NAME, null, FTAMonitorContract.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TEAM_TABLE);
        db.execSQL(CREATE_EVENT_TABLE);
        db.execSQL(CREATE_MATCH_TABLE);
        db.execSQL(CREATE_NOTES_TABLE);
        db.execSQL(CREATE_TEAM_EVENTS_TABLE);
        db.execSQL(CREATE_MATCHES_EVENT_TABLE);
        db.execSQL(CREATE_MATCHES_TEAMS_TABLE);
        db.execSQL(CREATE_TEAM_NOTES_TABLE);
        db.execSQL(CREATE_EVENT_NOTES_TABLE);
        db.execSQL(CREATE_MATCH_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Figure out how to actually update the tables
        db.execSQL(DELETE_TEAM_TABLE);
        db.execSQL(DELETE_EVENT_TABLE);
        db.execSQL(DELETE_MATCH_TABLE);
        db.execSQL(DELETE_NOTES_TABLE);
        db.execSQL(DELETE_TEAM_EVENTS_TABLE);
        db.execSQL(DELETE_MATCHES_EVENT_TABLE);
        db.execSQL(DELETE_MATCHES_TEAMS_TABLE);
        db.execSQL(DELETE_TEAM_NOTES_TABLE);
        db.execSQL(DELETE_EVENT_NOTES_TABLE);
        db.execSQL(DELETE_MATCH_NOTES_TABLE);
    }
}
