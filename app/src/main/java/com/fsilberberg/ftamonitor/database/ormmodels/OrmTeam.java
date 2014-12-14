package com.fsilberberg.ftamonitor.database.ormmodels;

import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.database.OrmLiteDatabaseHelper;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
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

    private Collection<OrmEvent> events;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getTeamNumber() {
        return teamNumber;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    @Override
    public String getTeamNick() {
        return teamNick;
    }

    @Override
    public Collection<? extends Event> getEvents() {
        checkRefresh();
        return events;
    }

    @Override
    public Collection<? extends Match> getMatches(Event event) {
        checkRefresh();
        return matches;
    }

    @Override
    public Collection<? extends Note> getNotes() {
        checkRefresh();
        return notes;
    }

    private void checkRefresh() {
        if (events == null) {
            OrmLiteDatabaseHelper helper = OpenHelperManager.getHelper(FTAMonitorApplication.getContext(), OrmLiteDatabaseHelper.class);
            try {
                final Dao<OrmTeamEvent, Long> dao = helper.getTeamEventDao();
                PreparedQuery<OrmTeamEvent> qb = dao.queryBuilder().where().eq(OrmTeamEvent.EVENT_ID, getId()).prepare();
                events = Collections2.transform(dao.query(qb), new Function<OrmTeamEvent, OrmEvent>() {
                    @Override
                    public OrmEvent apply(OrmTeamEvent input) {
                        return input.getEvent();
                    }
                });
            } catch (SQLException e) {
                Log.w(OrmEvent.class.getName(), "Could not get the team list for this event", e);
            } finally {
                OpenHelperManager.releaseHelper();

                // Ensure that teams is not null, if there was any errors
                if (events == null) {
                    events = new ArrayList<>();
                }
            }
        }

        if (notes == null || matches == null) {
            OrmLiteDatabaseHelper helper = OpenHelperManager.getHelper(FTAMonitorApplication.getContext(), OrmLiteDatabaseHelper.class);
            try {
                helper.getTeamDao().refresh(this);
            } catch (SQLException e) {
                Log.w(OrmTeam.class.getName(), "Could not refresh the team information", e);
            } finally {
                OpenHelperManager.releaseHelper();
            }
        }
    }
}
