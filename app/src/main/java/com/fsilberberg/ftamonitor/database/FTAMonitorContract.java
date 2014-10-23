package com.fsilberberg.ftamonitor.database;

import android.provider.BaseColumns;

/**
 * This is a contract class that defines the SQL Column information for the database
 */
abstract class FTAMonitorContract {

    public static final String DATABASE_NAME = "ftamonitor.db";
    public static final int VERSION = 1;

    public static String getDatabaseName(Table table) throws DatabaseException {
        switch (table) {
            case TEAM:
                return Team.TABLE_NAME;
            case EVENT:
                return Event.TABLE_NAME;
            case MATCH:
                return Match.TABLE_NAME;
            case NOTE:
                return Notes.TABLE_NAME;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    public static String getDatabaseId(Table table) throws DatabaseException {
        switch (table) {
            case TEAM:
                return Team._ID;
            case EVENT:
                return Event._ID;
            case MATCH:
                return Match._ID;
            case NOTE:
                return Notes._ID;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    public static String getLinkName(Table table, Table link) throws DatabaseException {
        switch (table) {
            case TEAM:
                return getTeamLinkName(link);
            case MATCH:
                return getMatchLinkName(link);
            case EVENT:
                return getEventLinkName(link);
            case NOTE:
                return getNoteLinkName(link);
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getTeamLinkName(Table table) throws DatabaseException {
        switch (table) {
            case EVENT:
                return Teams_Events.TABLE_NAME;
            case MATCH:
                return Matches_Teams.TABLE_NAME;
            case NOTE:
                return Team_Notes.TABLE_NAME;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getMatchLinkName(Table table) throws DatabaseException {
        switch (table) {
            case EVENT:
                return Matches_Event.TABLE_NAME;
            case TEAM:
                return Matches_Teams.TABLE_NAME;
            case NOTE:
                return Match_Notes.TABLE_NAME;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getEventLinkName(Table table) throws DatabaseException {
        switch (table) {
            case TEAM:
                return Teams_Events.TABLE_NAME;
            case MATCH:
                return Matches_Event.TABLE_NAME;
            case NOTE:
                return Event_Notes.TABLE_NAME;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getNoteLinkName(Table table) throws DatabaseException {
        switch (table) {
            case TEAM:
                return Team_Notes.TABLE_NAME;
            case MATCH:
                return Match_Notes.TABLE_NAME;
            case EVENT:
                return Event_Notes.TABLE_NAME;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    public static String getLinkId(Table table, Table link) throws DatabaseException {
        switch (table) {
            case TEAM:
                return getTeamLinkId(link);
            case MATCH:
                return getMatchLinkId(link);
            case EVENT:
                return getEventLinkId(link);
            case NOTE:
                return getNoteLinkId(link);
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getTeamLinkId(Table table) throws DatabaseException {
        switch (table) {
            case EVENT:
                return Teams_Events.EVENT;
            case MATCH:
                return Matches_Teams.MATCH;
            case NOTE:
                return Team_Notes.NOTE;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getMatchLinkId(Table table) throws DatabaseException {
        switch (table) {
            case EVENT:
                return Matches_Event.EVENT;
            case TEAM:
                return Matches_Teams.TEAM;
            case NOTE:
                return Match_Notes.NOTE;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getEventLinkId(Table table) throws DatabaseException {
        switch (table) {
            case TEAM:
                return Teams_Events.TEAM;
            case MATCH:
                return Matches_Event.MATCH;
            case NOTE:
                return Event_Notes.NOTE;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }

    private static String getNoteLinkId(Table table) throws DatabaseException {
        switch (table) {
            case TEAM:
                return Team_Notes.TEAM;
            case MATCH:
                return Match_Notes.MATCH;
            case EVENT:
                return Event_Notes.EVENT;
            default:
                throw new DatabaseException("Unknown database table requested " + table, new IllegalArgumentException());
        }
    }


    public abstract class Team implements BaseColumns {
        public static final String TABLE_NAME = "teams";
        public static final String TEAM_NUMBER = "team_number";
        public static final String TEAM_NAME = "team_name";
        public static final String TEAM_NICK = "team_nick";
    }

    public abstract class Event implements BaseColumns {
        public static final String TABLE_NAME = "events";
        public static final String EVENT_NAME = "event_name";
        public static final String EVENT_CODE = "event_code";
        public static final String EVENT_LOC = "event_loc";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
    }

    public abstract class Match implements BaseColumns {
        public static final String TABLE_NAME = "matches";
        public static final String MATCH_IDENTIFIER = "match_identifier";
        public static final String MATCH_PERIOD = "match_period";
        public static final String REPLAY = "replay";
    }

    public abstract class Notes implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String CONTENT = "content";
    }

    public abstract class Teams_Events implements BaseColumns {
        public static final String TABLE_NAME = "teams_events";
        public static final String TEAM = "team_id";
        public static final String EVENT = "event_id";
    }

    public abstract class Matches_Event implements BaseColumns {
        public static final String TABLE_NAME = "matches_event";
        public static final String MATCH = "match_id";
        public static final String EVENT = "event_id";
    }

    public abstract class Matches_Teams implements BaseColumns {
        public static final String TABLE_NAME = "matches_teams";
        public static final String MATCH = "match_id";
        public static final String TEAM = "team_id";
    }

    public abstract class Team_Notes implements BaseColumns {
        public static final String TABLE_NAME = "team_notes";
        public static final String TEAM = "team_id";
        public static final String NOTE = "note_id";
    }

    public abstract class Event_Notes implements BaseColumns {
        public static final String TABLE_NAME = "event_notes";
        public static final String EVENT = "event_id";
        public static final String NOTE = "note_id";
    }

    public abstract class Match_Notes implements BaseColumns {
        public static final String TABLE_NAME = "match_notes";
        public static final String MATCH = "match_id";
        public static final String NOTE = "note_id";
    }
}
