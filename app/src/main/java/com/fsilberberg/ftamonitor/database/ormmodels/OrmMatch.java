package com.fsilberberg.ftamonitor.database.ormmodels;

import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchPeriod;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.database.OrmLiteDatabaseHelper;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
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

    public static final Function<Match, OrmMatch> copyMapper = new Function<Match, OrmMatch>() {
        @Override
        public OrmMatch apply(Match input) {
            if (input == null) {
                return null;
            } else if (input instanceof OrmMatch) {
                return (OrmMatch) input;
            } else {
                return new OrmMatch(input);
            }
        }
    };

    @DatabaseField(generatedId = true, columnName = "id")
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

    /**
     * Default constructor for OrmLite
     */
    public OrmMatch() {
    }

    public OrmMatch(Event event, Collection<Note> notes, Table<Alliance, Station, Team> teams, MatchPeriod period, int replay, String matchId) {
        this.event = OrmEvent.copyMapper.apply(event);
        this.notes = Collections2.transform(notes, OrmNote.copyMapper);
        this.period = period;
        this.replay = replay;
        this.matchId = matchId;
        extractTeams(teams);
    }

    public OrmMatch(Match match) {
        if (match instanceof OrmMatch) {
            OrmMatch m = (OrmMatch) match;
            this.id = m.id;
            this.event = m.event;
            this.notes = m.notes;
            this.period = m.period;
            this.replay = m.replay;
            this.matchId = m.matchId;
            this.red1 = m.red1;
            this.red2 = m.red2;
            this.red3 = m.red3;
            this.blue1 = m.blue1;
            this.blue2 = m.blue2;
            this.blue3 = m.blue3;
        } else {
            this.id = 0;
            this.event = OrmEvent.copyMapper.apply(match.getEvent());
            this.notes = Collections2.transform(match.getNotes(), OrmNote.copyMapper);
            this.period = match.getPeriod();
            this.replay = match.getReplay();
            this.matchId = match.getIdentifier();
            extractTeams(match.getTeams());
        }
    }

    private void extractTeams(Table<Alliance, Station, Team> teams) {
        if (teams != null) {
            red1 = OrmTeam.copyMapper.apply(teams.get(Alliance.RED, Station.STATION1));
            red2 = OrmTeam.copyMapper.apply(teams.get(Alliance.RED, Station.STATION2));
            red3 = OrmTeam.copyMapper.apply(teams.get(Alliance.RED, Station.STATION3));
            blue1 = OrmTeam.copyMapper.apply(teams.get(Alliance.BLUE, Station.STATION1));
            blue2 = OrmTeam.copyMapper.apply(teams.get(Alliance.BLUE, Station.STATION2));
            blue3 = OrmTeam.copyMapper.apply(teams.get(Alliance.BLUE, Station.STATION3));
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getReplay() {
        return replay;
    }

    @Override
    public MatchPeriod getPeriod() {
        return period;
    }

    @Override
    public String getIdentifier() {
        return matchId;
    }

    @Override
    public Table<Alliance, Station, Team> getTeams() {
        Table<Alliance, Station, Team> teams = HashBasedTable.create();
        teams.put(Alliance.RED, Station.STATION1, red1);
        teams.put(Alliance.RED, Station.STATION2, red2);
        teams.put(Alliance.RED, Station.STATION3, red3);
        teams.put(Alliance.BLUE, Station.STATION1, blue1);
        teams.put(Alliance.BLUE, Station.STATION2, blue2);
        teams.put(Alliance.BLUE, Station.STATION3, blue3);
        return teams;
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
