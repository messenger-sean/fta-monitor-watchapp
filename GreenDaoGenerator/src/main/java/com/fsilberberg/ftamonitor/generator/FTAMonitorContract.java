package com.fsilberberg.ftamonitor.generator;

/**
 * This is a contract class that defines the SQL Column information for the database
 */
abstract class FTAMonitorContract {

    public static final String TABLE_PACKAGE = "com.fsilberberg.ftamonitor.database";

    public abstract class Team {
        public static final String TABLE_NAME = "teams";
        public static final String TEAM_NUMBER = "team_number";
        public static final String TEAM_NAME = "team_name";
        public static final String TEAM_NICK = "team_nick";
        public static final String TEAM_EVENTS = "events";
        public static final String TEAM_MATCHES = "matches";
        public static final String TEAM_NOTES = "notes";
    }

    public abstract class Event {
        public static final String TABLE_NAME = "events";
        public static final String EVENT_NAME = "event_name";
        public static final String EVENT_CODE = "event_code";
        public static final String EVENT_LOC = "event_loc";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String EVENT_TEAMS = "teams";
        public static final String EVENT_MATCHES = "matches";
        public static final String EVENT_NOTES = "notes";
    }

    public abstract class Match {
        public static final String TABLE_NAME = "matches";
        public static final String MATCH_IDENTIFIER = "match_identifier";
        public static final String MATCH_PERIOD = "match_period";
        public static final String REPLAY = "replay";
        public static final String MATCH_EVENT = "events";
        public static final String MATCH_TEAMS = "teams";
        public static final String MATCH_NOTES = "notes";
    }

    public abstract class Notes {
        public static final String TABLE_NAME = "notes";
        public static final String CONTENT = "content";
        public static final String NOTE_EVENT = "event";
        public static final String NOTE_TEAM = "team";
        public static final String NOTE_MATCH = "match";
    }

    public abstract class Teams_Events {
        public static final String TABLE_NAME = "teams_events";
    }

    public abstract class Matches_Event {
        public static final String TABLE_NAME = "matches_event";
    }

    public abstract class Matches_Teams {
        public static final String TABLE_NAME = "matches_teams";
    }

    public abstract class Team_Notes {
        public static final String TABLE_NAME = "team_notes";
    }

    public abstract class Event_Notes {
        public static final String TABLE_NAME = "event_notes";
    }

    public abstract class Match_Notes {
        public static final String TABLE_NAME = "match_notes";
    }
}
