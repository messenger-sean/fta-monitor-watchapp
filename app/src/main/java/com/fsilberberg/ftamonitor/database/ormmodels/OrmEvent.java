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
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * OrmLite model for an event
 */
@DatabaseTable(tableName = "events")
public class OrmEvent implements Event {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(columnName = "eventCode", canBeNull = false)
    private String eventCode;

    @DatabaseField(columnName = "eventName")
    private String eventName;

    @DatabaseField(columnName = "eventLoc")
    private String eventLoc;

    @DatabaseField(columnName = "startDate")
    private Date startDate;

    @DatabaseField(columnName = "endDate")
    private Date endDate;

    @ForeignCollectionField(columnName = "matches")
    private Collection<OrmMatch> matches;

    @ForeignCollectionField(columnName = "notes")
    private Collection<OrmNote> notes;

    // The list of teams going to this event. This is not stored in the table for this object, as OrmLite doesn't
    // support many-many relations. Rather, the TeamEvent table stores those relations
    private Collection<OrmTeam> teams;

    public OrmEvent(Event event) {
        id = event.getId();
        eventCode = event.getEventCode();
        eventName = event.getEventName();
        eventLoc = event.getEventLoc();
        startDate = event.getStartDate().toDate();
        endDate = event.getEndDate().toDate();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getEventCode() {
        return eventCode;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public String getEventLoc() {
        return eventLoc;
    }

    @Override
    public DateTime getStartDate() {
        return new DateTime(startDate);
    }

    @Override
    public DateTime getEndDate() {
        return new DateTime(endDate);
    }

    @Override
    public Collection<? extends Team> getTeams() {
        if (teams == null) {
            OrmLiteDatabaseHelper helper = getHelper();
            try {
                final Dao<OrmTeamEvent, Long> dao = helper.getTeamEventDao();
                PreparedQuery<OrmTeamEvent> qb = dao.queryBuilder().where().eq(OrmTeamEvent.EVENT_ID, getId()).prepare();
                teams = Collections2.transform(dao.query(qb), new Function<OrmTeamEvent, OrmTeam>() {
                    @Override
                    public OrmTeam apply(OrmTeamEvent input) {
                        return input.getTeam();
                    }
                });
            } catch (SQLException e) {
                Log.w(OrmEvent.class.getName(), "Could not get the team list for this event", e);
            } finally {
                releaseHelper();

                // Ensure that teams is not null, if there was any errors
                if (teams == null) {
                    teams = new ArrayList<>();
                }
            }
        }

        return teams;
    }

    @Override
    public Collection<? extends Match> getMatches() {
        if (matches == null) {
            OrmLiteDatabaseHelper helper = getHelper();
            try {
                helper.getEventDao().refresh(this);
            } catch (SQLException e) {
                Log.w(OrmEvent.class.getName(), "Could not refresh match list", e);
            } finally {
                // Ensure the helper is released
                releaseHelper();

                // Ensure that matches was refreshed. If it's still null, set it to the empty list
                if (matches == null) {
                    matches = new ArrayList<>();
                }
            }

        }

        return matches;
    }

    @Override
    public Collection<? extends Note> getNotes() {
        if (notes == null) {
            OrmLiteDatabaseHelper helper = getHelper();
            try {
                helper.getEventDao().refresh(this);
            } catch (SQLException e) {
                Log.w(OrmEvent.class.getName(), "Could not refresh notes", e);
            } finally {
                // ensure that the helper is released
                releaseHelper();

                // Ensure that the notes were refreshed. If it's still null, set it to the empty list
                if (notes == null) {
                    notes = new ArrayList<>();
                }
            }
        }

        return notes;
    }

    private OrmLiteDatabaseHelper getHelper() {
        return OpenHelperManager.getHelper(FTAMonitorApplication.getContext(), OrmLiteDatabaseHelper.class);
    }

    private void releaseHelper() {
        OpenHelperManager.releaseHelper();
    }
}
