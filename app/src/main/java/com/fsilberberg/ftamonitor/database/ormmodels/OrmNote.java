package com.fsilberberg.ftamonitor.database.ormmodels;

import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.database.OrmLiteDatabaseHelper;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Match;
import com.fsilberberg.ftamonitor.ftaassistant.Note;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;

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
