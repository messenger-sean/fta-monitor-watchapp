package com.fsilberberg.ftamonitor.database;

import android.provider.BaseColumns;

/**
 * This is a contract class that defines the SQL Column information for the database
 */
abstract class FTAMonitorContract {

    public static final String DATABASE_NAME = "ftamonitor.db";
    public static final int VERSION = 1;

    public abstract class Team implements BaseColumns {
        public static final String TABLE_NAME = "teams";
        public static final String TEAM_NUMBER = "team_number";
        public static final String TEAM_NAME = "team_name";
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
