package com.fsilberberg.ftamonitor.database.ormmodels;

import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

/**
 * OrmLite model for the information in a team
 */
@DatabaseTable(tableName = "teams")
public class OrmTeam implements Team {
    @DatabaseField(generatedId = true, columnName = "id")
    private long id;

    @DatabaseField(columnName = "teamNumber", canBeNull = false)
    private int teamNumber;

    @DatabaseField(columnName = "teamName")
    private String teamName;

    @DatabaseField(columnName = "teamNick")
    private String teamNick;

    @ForeignCollectionField(columnName = "matches")
    private Collection<OrmMatch> matches;

    @ForeignCollectionField(columnName = "notes")
    private Collection<OrmNote> notes;

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public int getTeamNumber() {
        return 0;
    }

    @Override
    public String getTeamName() {
        return null;
    }

    @Override
    public String getTeamNick() {
        return null;
    }

    @Override
    public Collection<Event> getEvents() {
        return null;
    }

    @Override
    public Collection<Match> getMatches(Event event) {
        return null;
    }

    @Override
    public Collection<Note> getNotes() {
        return null;
    }
}
