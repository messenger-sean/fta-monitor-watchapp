package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.fsilberberg.ftamonitor.common.Alliance.*;

/**
 * This class is responsible for watching the field for problems with robots and notifying the user
 * when an error occurs during a match
 */
public class FieldProblemNotificationService implements IForegroundService {

    // Intent extra for the settings to tell the service to update the notification settings
    public static final String UPDATE_NOTIFICATION_SETTINGS_INTENT_EXTRA = "UPDATE_NOTIFICATION_SETTINGS";

    // References to the teams
    private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_blue1 = m_field.getBlue1();
    private final TeamStatus m_blue2 = m_field.getBlue2();
    private final TeamStatus m_blue3 = m_field.getBlue3();
    private final TeamStatus m_red1 = m_field.getRed1();
    private final TeamStatus m_red2 = m_field.getRed2();
    private final TeamStatus m_red3 = m_field.getRed3();

    private Context m_context;
    private boolean m_isSetup = false;
    private boolean m_alwaysNotify = false;
    private final Collection<ProblemObserver> m_observers = new ArrayList<>();

    public void update(TeamUpdateType updateType, int stationNum, Alliance alliance) {
        // We only care about the states in which there could be an error
        switch (updateType) {
            case DS_ETH:
            case DS:
            case RADIO:
            case ROBOT:
            case CODE:
                // If we should always notify or if a match is playing, then process an update
                if (m_alwaysNotify || isMatchPlaying()) {

                }
        }
    }

    @Override
    public void startService(Context context, Intent intent) {
        m_context = context;
        SharedPreferences m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String m_notifyKey = context.getString(R.string.notification_key);
        String m_notifyAlwaysKey = context.getString(R.string.notify_always_key);

        // We need to update the notifications on the first run or if the intent has our update
        // extra
        if (!m_isSetup || intent.hasExtra(UPDATE_NOTIFICATION_SETTINGS_INTENT_EXTRA)) {
            boolean notify = m_sharedPreferences.getBoolean(m_notifyKey, true);
            if (!notify) {
                // The user doesn't want any notifications, call stop service to ensure that
                // we are unsubscribed and return
                stopService();
                return;
            }
            m_alwaysNotify = m_sharedPreferences.getBoolean(m_notifyAlwaysKey, false);
            m_isSetup = true;
        }

        // Register an observer for each team
        m_observers.add(new ProblemObserver(1, BLUE, m_blue1));
        m_observers.add(new ProblemObserver(2, BLUE, m_blue2));
        m_observers.add(new ProblemObserver(3, BLUE, m_blue3));
        m_observers.add(new ProblemObserver(1, RED, m_red1));
        m_observers.add(new ProblemObserver(2, RED, m_red2));
        m_observers.add(new ProblemObserver(3, RED, m_red3));
    }

    @Override
    public void stopService() {
        // Deregister from all observation
        for (ProblemObserver observer : m_observers) {
            observer.deregister();
        }
    }

    private boolean isMatchPlaying() {
        MatchStatus status = m_field.getMatchStatus();
        switch (status) {
            case AUTO:
            case AUTO_END:
            case AUTO_PAUSED:
            case TELEOP:
            case TELEOP_PAUSED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Convenience method for getting all teams
     *
     * @return All teams
     */
    private Collection<TeamStatus> getAllTeams() {
        FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
        return Arrays.asList(
                m_blue1, m_blue2, m_blue3, m_red1, m_red2, m_red3
        );
    }

    private class ProblemObserver implements IObserver<TeamUpdateType> {

        private int m_stationNumber;
        private Alliance m_alliance;
        private TeamStatus m_team;

        private ProblemObserver(int stationNumber, Alliance alliance, TeamStatus team) {
            m_stationNumber = stationNumber;
            m_alliance = alliance;
            m_team = team;
            m_team.registerObserver(this);
        }

        @Override
        public void update(TeamUpdateType updateType) {
            FieldProblemNotificationService.this.update(updateType, m_stationNumber, m_alliance);
        }

        public void deregister() {
            m_team.deregisterObserver(this);
        }
    }
}
