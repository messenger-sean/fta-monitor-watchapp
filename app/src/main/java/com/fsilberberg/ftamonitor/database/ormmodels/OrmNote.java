package com.fsilberberg.ftamonitor.database.ormmodels;

import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * OrmLite model for notes
 */
@DatabaseTable(tableName = "notes")
public class OrmNote implements Note {

    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    @DatabaseField(columnName = "content")
    private String content;

    @DatabaseField(columnName = "team", foreign = true)
    private OrmTeam team;

    @DatabaseField(columnName = "event", foreign = true)
    private OrmEvent event;

    @DatabaseField(columnName = "match", foreign = true)
    private OrmMatch match;

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public Team getTeam() {
        return null;
    }

    @Override
    public Event getEvent() {
        return null;
    }

    @Override
    public Match getMatch() {
        return null;
    }
}
