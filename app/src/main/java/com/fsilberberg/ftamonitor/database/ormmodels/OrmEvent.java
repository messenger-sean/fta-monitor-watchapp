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
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.PreparedQuery;
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

    public static final String EVENT_CODE = "eventCode";
    public static final String EVENT_NAME = "eventName";
    public static final String EVENT_LOC = "eventLoc";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String MATCHES = "matches";
    public static final String NOTES = "notes";

    public static final Function<Event, OrmEvent> copyMapper = new Function<Event, OrmEvent>() {
        @Override
        public OrmEvent apply(Event input) {
            if (input == null) {
                return null;
            } else if (input instanceof OrmEvent) {
                return (OrmEvent) input;
            } else {
                return new OrmEvent(input);
            }
        }
    };

    /**
     * Default constructor for OrmLite
     */
    public OrmEvent() {
    }

    /**
     * Copy constructor for OrmEvent
     *
     * @param event The event to copy
     */
    public OrmEvent(Event event) {
        if (event instanceof OrmEvent) {
            OrmEvent e = (OrmEvent) event;
            this.eventCode = e.eventCode;
            this.eventName = e.eventName;
            this.eventLoc = e.eventLoc;
            this.startDate = e.startDate;
            this.endDate = e.endDate;
            // If there are no matches and teams specified, then this doesn't have any yet, since this is being made
            // with no id. Initialize these lists to empty
            this.matches = e.matches;
            this.notes = e.notes;
            this.teams = e.teams;
        } else {
            this.id = 0;
            this.eventCode = event.getEventCode();
            this.eventName = event.getEventName();
            this.eventLoc = event.getEventLoc();
            this.startDate = event.getStartDate().toDate();
            this.endDate = event.getEndDate().toDate();
            this.matches = Collections2.transform(event.getMatches(), OrmMatch.copyMapper);
            this.notes = Collections2.transform(event.getNotes(), OrmNote.copyMapper);
            this.teams = Collections2.transform(event.getTeams(), OrmTeam.copyMapper);
        }
    }

    public OrmEvent(String eventCode, String eventName, String eventLoc, Date startDate, Date endDate,
                    Collection<Match> matches, Collection<Note> notes, Collection<Team> teams) {
        this.eventCode = eventCode;
        this.eventName = eventName;
        this.eventLoc = eventLoc;
        this.startDate = startDate;
        this.endDate = endDate;
        // If there are no matches and teams specified, then this doesn't have any yet, since this is being made
        // with no id. Initialize these lists to empty
        this.matches = matches == null ? new ArrayList<OrmMatch>() : Collections2.transform(matches, OrmMatch.copyMapper);
        this.notes = notes == null ? new ArrayList<OrmNote>() : Collections2.transform(notes, OrmNote.copyMapper);
        this.teams = teams == null ? new ArrayList<OrmTeam>() : Collections2.transform(teams, OrmTeam.copyMapper);
    }

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(columnName = EVENT_CODE, canBeNull = false)
    private String eventCode;

    @DatabaseField(columnName = EVENT_NAME)
    private String eventName;

    @DatabaseField(columnName = EVENT_LOC)
    private String eventLoc;

    @DatabaseField(columnName = START_DATE, dataType = DataType.DATE_LONG)
    private Date startDate;

    @DatabaseField(columnName = END_DATE, dataType = DataType.DATE_LONG)
    private Date endDate;

    @ForeignCollectionField(columnName = MATCHES)
    private Collection<OrmMatch> matches;

    @ForeignCollectionField(columnName = NOTES)
    private Collection<OrmNote> notes;

    // The list of teams going to this event. This is not stored in the table for this object, as OrmLite doesn't
    // support many-many relations. Rather, the TeamEvent table stores those relations
    private Collection<OrmTeam> teams;

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

    /**
     * Gets the team list without attempting to refresh if the teams list is currently null. This is intended for use
     * by the database.
     *
     * @return The list of teams
     */
    public Collection<OrmTeam> getTeamsNoRefresh() {
        return teams;
    }

    private OrmLiteDatabaseHelper getHelper() {
        return OpenHelperManager.getHelper(FTAMonitorApplication.getContext(), OrmLiteDatabaseHelper.class);
    }

    private void releaseHelper() {
        OpenHelperManager.releaseHelper();
    }
}
