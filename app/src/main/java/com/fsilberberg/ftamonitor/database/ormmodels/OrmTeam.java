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
    public static final String TEAM_NUMBER = "teamNumber";
    public static final String TEAM_NAME = "teamName";
    public static final String TEAM_NICK = "teamNick";
    public static final String MATCHES = "matches";
    public static final String NOTES = "notes";

    public static final Function<Team, OrmTeam> copyMapper = new Function<Team, OrmTeam>() {
        @Override
        public OrmTeam apply(Team input) {
            if (input == null) {
                return null;
            } else if (input instanceof OrmTeam) {
                return (OrmTeam) input;
            } else {
                return new OrmTeam(input);
            }
        }
    };

    @DatabaseField(generatedId = true, columnName = "id")
    private long id;

    @DatabaseField(columnName = TEAM_NUMBER, canBeNull = false)
    private int teamNumber;

    @DatabaseField(columnName = TEAM_NAME)
    private String teamName;

    @DatabaseField(columnName = TEAM_NICK)
    private String teamNick;

    @ForeignCollectionField(columnName = MATCHES)
    private Collection<OrmMatch> matches;

    @ForeignCollectionField(columnName = NOTES)
    private Collection<OrmNote> notes;

    private Collection<OrmEvent> events;

    /**
     * Default constructor for OrmLite
     */
    public OrmTeam() {
    }

    public OrmTeam(int teamNumber, String teamName, String teamNick, Collection<Match> matches, Collection<Note> notes, Collection<Event> events) {
        this.teamNumber = teamNumber;
        this.teamName = teamName;
        this.teamNick = teamNick;
        this.matches = matches != null ? Collections2.transform(matches, OrmMatch.copyMapper) : new ArrayList<OrmMatch>();
        this.notes = notes != null ? Collections2.transform(notes, OrmNote.copyMapper) : new ArrayList<OrmNote>();
        this.events = events != null ? Collections2.transform(events, OrmEvent.copyMapper) : new ArrayList<OrmEvent>();
    }

    public OrmTeam(Team team) {
        this.teamNumber = team.getTeamNumber();
        this.teamName = team.getTeamName();
        this.teamNick = team.getTeamNick();
        if (team instanceof OrmTeam) {
            this.matches = ((OrmTeam) team).matches;
            this.notes = ((OrmTeam) team).notes;
            this.events = ((OrmTeam) team).events;
        } else {
            this.events = Collections2.transform(team.getEvents(), OrmEvent.copyMapper);
            this.matches = new ArrayList<>();
            for (Event e : team.getEvents()) {
                matches.addAll(Collections2.transform(team.getMatches(e), OrmMatch.copyMapper));
            }
            this.notes = Collections2.transform(team.getNotes(), OrmNote.copyMapper);
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getTeamNumber() {
        return teamNumber;
    }

    @Override
    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    @Override
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public String getTeamNick() {
        return teamNick;
    }

    @Override
    public void setTeamNick(String teamNick) {
        this.teamNick = teamNick;
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

    /**
     * Gets the events from this model without doing any refresh, if the events list hasn't been accessed yet
     * This is really only intended for database use, not for general consumption
     *
     * @return The events list, if it has already been fetched.
     */
    public Collection<OrmEvent> getEventsNoRefresh() {
        return events;
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
