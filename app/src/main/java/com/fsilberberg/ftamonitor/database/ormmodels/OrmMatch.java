package com.fsilberberg.ftamonitor.database.ormmodels;

import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.database.OrmLiteDatabaseHelper;
import com.fsilberberg.ftamonitor.ftaassistant.*;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * OrmLite model for matches
 */
@DatabaseTable(tableName = "matches")
public class OrmMatch implements Match {

    public static final String MATCH_IDENTIFIER = "matchId";
    public static final String REPLAY = "replay";
    public static final String PERIOD = "period";
    public static final String RED1 = "red1";
    public static final String RED2 = "red2";
    public static final String RED3 = "red3";
    public static final String BLUE1 = "blue1";
    public static final String BLUE2 = "blue2";
    public static final String BLUE3 = "blue3";
    public static final String EVENT = "event";
    public static final String NOTES = "notes";

    @DatabaseField(columnName = "id")
    private long id;

    @DatabaseField(columnName = MATCH_IDENTIFIER)
    private String matchId;

    @DatabaseField(columnName = REPLAY)
    private int replay;

    @DatabaseField(columnName = PERIOD)
    private MatchPeriod period;

    @DatabaseField(columnName = RED1, canBeNull = false, foreign = true)
    private OrmTeam red1;

    @DatabaseField(columnName = RED2, canBeNull = false, foreign = true)
    private OrmTeam red2;

    @DatabaseField(columnName = RED3, canBeNull = false, foreign = true)
    private OrmTeam red3;

    @DatabaseField(columnName = BLUE1, canBeNull = false, foreign = true)
    private OrmTeam blue1;

    @DatabaseField(columnName = BLUE2, canBeNull = false, foreign = true)
    private OrmTeam blue2;

    @DatabaseField(columnName = BLUE3, canBeNull = false, foreign = true)
    private OrmTeam blue3;

    @DatabaseField(foreign = true, columnName = EVENT)
    private OrmEvent event;

    @ForeignCollectionField(columnName = NOTES)
    private Collection<OrmNote> notes;

    private MatchIdentifier matchIdentifierObject;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public MatchIdentifier getMatchId() {
        if (matchIdentifierObject == null) {
            matchIdentifierObject = AssistantFactory.getInstance().makeMatchIdentifier(period, matchId, replay);
        }

        return matchIdentifierObject;
    }

    @Override
    public Team getTeam(Alliance alliance, Station station) {
        checkRefresh();
        switch (alliance) {
            case RED:
                switch (station) {
                    case STATION1:
                        return red1;
                    case STATION2:
                        return red2;
                    case STATION3:
                        return red3;
                }
                break;
            case BLUE:
                switch (station) {
                    case STATION1:
                        return blue1;
                    case STATION2:
                        return blue2;
                    case STATION3:
                        return blue3;
                }
                break;
        }
        return null;
    }

    @Override
    public Collection<? extends Team> getTeams() {
        checkRefresh();
        return Arrays.asList(red1, red2, red3, blue1, blue2, blue3);
    }

    @Override
    public Event getEvent() {
        checkRefresh();
        return event;
    }

    @Override
    public Collection<? extends Note> getNotes() {
        checkRefresh();
        return notes;
    }

    private void checkRefresh() {
        if (blue1 == null ||
                blue2 == null ||
                blue3 == null ||
                red1 == null ||
                red2 == null ||
                red3 == null ||
                event == null ||
                notes == null) {
            OrmLiteDatabaseHelper helper = OpenHelperManager.getHelper(FTAMonitorApplication.getContext(), OrmLiteDatabaseHelper.class);
            try {
                helper.getMatchDao().refresh(this);
            } catch (SQLException e) {
                Log.w(OrmMatch.class.getName(), "Could not refresh the match information", e);
                notes = new ArrayList<>();
            } finally {
                OpenHelperManager.releaseHelper();
            }
        }
    }
}
