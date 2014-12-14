package com.fsilberberg.ftamonitor.database.ormmodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * OrmLite join model for the ManyToMany relationship between Teams and events
 */
@DatabaseTable(tableName = "team_event")
public class OrmTeamEvent {

    public static final String TEAM_ID = "team_id";
    public static final String EVENT_ID = "event_id";

    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    @DatabaseField(columnName = TEAM_ID, foreign = true, foreignAutoRefresh = true)
    private OrmTeam team;

    @DatabaseField(columnName = EVENT_ID, foreign = true, foreignAutoRefresh = true)
    private OrmEvent event;

    public OrmTeamEvent() {
    }

    public OrmTeamEvent(OrmTeam team, OrmEvent event) {
        this.team = team;
        this.event = event;
    }

    public OrmTeam getTeam() {
        return team;
    }

    public OrmEvent getEvent() {
        return event;
    }
}
