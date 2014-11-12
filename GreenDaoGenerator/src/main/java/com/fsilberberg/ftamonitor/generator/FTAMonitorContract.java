package com.fsilberberg.ftamonitor.generator;

/**
 * This is a contract class that defines the SQL Column information for the database
 */
abstract class FTAMonitorContract {

    public static final String TABLE_PACKAGE = "com.fsilberberg.ftamonitor.database";

    public abstract class Team {
        public static final String TABLE_NAME = "Teams";
        public static final String TEAM_NUMBER = "team_number";
        public static final String TEAM_NAME = "team_name";
        public static final String TEAM_NICK = "team_nick";
        public static final String TEAM_EVENTS = "team_events_id";
        public static final String TEAM_MATCHES = "team_matches_id";
        public static final String TEAM_NOTES = "team_notes_id";
    }

    public abstract class Event {
        public static final String TABLE_NAME = "Events";
        public static final String EVENT_NAME = "event_name";
        public static final String EVENT_CODE = "event_code";
        public static final String EVENT_LOC = "event_loc";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String EVENT_TEAMS = "event_teams_id";
        public static final String EVENT_MATCHES = "event_matches_id";
        public static final String EVENT_NOTES = "event_notes_id";
    }

    public abstract class Match {
        public static final String TABLE_NAME = "Matches";
        public static final String MATCH_IDENTIFIER = "match_identifier";
        public static final String MATCH_PERIOD = "match_period";
        public static final String REPLAY = "replay";
        public static final String MATCH_EVENT = "match_events_id";
        public static final String MATCH_TEAMS = "match_teams_id";
        public static final String MATCH_NOTES = "match_notes_id";
    }

    public abstract class Notes {
        public static final String TABLE_NAME = "Notes";
        public static final String CONTENT = "content";
        public static final String NOTE_EVENT = "note_event_id";
        public static final String NOTE_TEAM = "note_team_id";
        public static final String NOTE_MATCH = "note_match_id";
    }

    public abstract class Teams_Events {
        public static final String TABLE_NAME = "Teams_Events";
        public static final String TEAM_ID = "team_event_team_id";
        public static final String EVENT_ID = "team_event_event_id";
    }

    public abstract class Matches_Teams {
        public static final String TABLE_NAME = "Matches_Teams";
        public static final String TEAM_ID = "team_match_team_id";
        public static final String MATCH_ID = "team_match_match_id";
    }
}
