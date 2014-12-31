package com.fsilberberg.ftamonitor.database.ormmodels;

import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.database.OrmLiteDatabaseHelper;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Function;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;

/**
 * OrmLite model for notes
 */
@DatabaseTable(tableName = "notes")
public class OrmNote implements Note {

    public static final String CONTENT = "content";
    public static final String TEAM = "team";
    public static final String EVENT = "event";
    public static final String MATCH = "match";

    public static final Function<Note, OrmNote> copyMapper = new Function<Note, OrmNote>() {
        @Override
        public OrmNote apply(Note input) {
            if (input == null) {
                return null;
            } else if (input instanceof OrmNote) {
                return (OrmNote) input;
            } else {
                return new OrmNote(input);
            }
        }
    };

    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    @DatabaseField(columnName = CONTENT)
    private String content;

    @DatabaseField(columnName = TEAM, foreign = true)
    private OrmTeam team;

    @DatabaseField(columnName = EVENT, foreign = true)
    private OrmEvent event;

    @DatabaseField(columnName = MATCH, foreign = true)
    private OrmMatch match;

    /**
     * Default constructor for OrmLite
     */
    public OrmNote() {
    }

    public OrmNote(String content, Team team, Event event, Match match) {
        this.content = content;
        this.team = OrmTeam.copyMapper.apply(team);
        this.event = OrmEvent.copyMapper.apply(event);
        this.match = OrmMatch.copyMapper.apply(match);
    }

    public OrmNote(Note note) {
        if (note instanceof OrmNote) {
            this.content = ((OrmNote) note).content;
            this.team = ((OrmNote) note).team;
            this.event = ((OrmNote) note).event;
            this.match = ((OrmNote) note).match;
        } else {
            this.content = note.getContent();
            this.team = OrmTeam.copyMapper.apply(note.getTeam());
            this.event = OrmEvent.copyMapper.apply(note.getEvent());
            this.match = OrmMatch.copyMapper.apply(note.getMatch());
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Team getTeam() {
        checkRefresh();
        return team;
    }

    @Override
    public Event getEvent() {
        checkRefresh();
        return event;
    }

    @Override
    public Match getMatch() {
        checkRefresh();
        return match;
    }

    private void checkRefresh() {
        if (team == null || event == null || match == null) {
            OrmLiteDatabaseHelper helper = OpenHelperManager.getHelper(FTAMonitorApplication.getContext(), OrmLiteDatabaseHelper.class);
            try {
                helper.getNoteDao().refresh(this);
            } catch (SQLException e) {
                Log.w(OrmNote.class.getName(), "Could not refresh note data", e);
            } finally {
                OpenHelperManager.releaseHelper();
            }
        }
    }
}
