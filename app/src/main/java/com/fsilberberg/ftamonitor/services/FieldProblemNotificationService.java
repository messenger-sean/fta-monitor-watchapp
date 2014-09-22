package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;

import java.util.Arrays;
import java.util.Collection;

/**
 * This class is responsible for watching the field for problems with robots and notifying the user
 * when an error occurs during a match
 */
public class FieldProblemNotificationService implements IForegroundService, IObserver<TeamUpdateType> {

    // Intent extra for the settings to tell the service to update the notification settings
    public static final String UPDATE_NOTIFICATION_SETTINGS_INTENT_EXTRA = "UPDATE_NOTIFICATION_SETTINGS";

    private Context m_context;
    private boolean m_isSetup = false;
    private boolean m_alwaysNotify = false;
    private SharedPreferences m_sharedPreferences;
    private String m_notifyKey;
    private String m_notifyAlwaysKey;

    @Override
    public void update(TeamUpdateType updateType) {

    }

    @Override
    public void startService(Context context, Intent intent) {
        m_context = context;
        m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        m_notifyKey = context.getString(R.string.notification_key);
        m_notifyAlwaysKey = context.getString(R.string.notify_always_key);

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

        for (TeamStatus status : getAllTeams()) {
            status.registerObserver(this);
        }
    }

    @Override
    public void stopService() {
        for (TeamStatus status : getAllTeams()) {
            status.deregisterObserver(this);
        }
    }

    private static Collection<TeamStatus> getAllTeams() {
        FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
        return Arrays.asList(
                field.getBlue1(),
                field.getBlue2(),
                field.getBlue3(),
                field.getRed1(),
                field.getRed2(),
                field.getRed3()
        );
    }
}
