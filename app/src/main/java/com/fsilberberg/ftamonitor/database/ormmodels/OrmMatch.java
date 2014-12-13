package com.fsilberberg.ftamonitor.database.ormmodels;

import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.ftaassistant.*;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

/**
 * OrmLite model for matches
 */
@DatabaseTable(tableName = "matches")
public class OrmMatch implements Match {

    @DatabaseField(columnName = "id")
    private long id;

    @DatabaseField(columnName = "matchId")
    private String matchId;

    @DatabaseField(columnName = "red1", canBeNull = false, foreign = true)
    private OrmTeam red1;

    @DatabaseField(columnName = "red2", canBeNull = false, foreign = true)
    private OrmTeam red2;

    @DatabaseField(columnName = "red3", canBeNull = false, foreign = true)
    private OrmTeam red3;

    @DatabaseField(columnName = "blue1", canBeNull = false, foreign = true)
    private OrmTeam blue1;

    @DatabaseField(columnName = "blue2", canBeNull = false, foreign = true)
    private OrmTeam blue2;

    @DatabaseField(columnName = "blue3", canBeNull = false, foreign = true)
    private OrmTeam blue3;

    @DatabaseField(foreign = true, columnName = "event")
    private OrmEvent event;

    @ForeignCollectionField(columnName = "notes")
    private Collection<OrmNote> notes;

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public MatchIdentifier getMatchId() {
        return null;
    }

    @Override
    public Team getTeam(Alliance alliance, Station station) {
        return null;
    }

    @Override
    public Collection<Team> getTeams() {
        return null;
    }

    @Override
    public Event getEvent() {
        return null;
    }

    @Override
    public Collection<Note> getNotes() {
        return null;
    }
}
