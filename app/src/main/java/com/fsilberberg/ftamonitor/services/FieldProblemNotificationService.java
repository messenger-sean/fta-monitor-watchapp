package com.fsilberberg.ftamonitor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.UpdateType;
import com.fsilberberg.ftamonitor.view.old.DrawerActivity;

import java.util.ArrayList;
import java.util.Collection;

import static com.fsilberberg.ftamonitor.common.Alliance.BLUE;
import static com.fsilberberg.ftamonitor.common.Alliance.RED;

/**
 * This class is responsible for watching the field for problems with robots and notifying the user
 * when an error occurs during a match
 */
public class FieldProblemNotificationService {
    // Notification ID. Again no significance, other than 3 is my favorite number, and 3x3 is 9
    private static final int ID = 9;
    private static final int MAIN_ACTIVITY_ID = 4;
    private static FieldProblemNotificationService instance;

    public static void start() {
        if (instance != null) {
            instance.stopService();
            instance = null;
        }

        instance = new FieldProblemNotificationService();
        instance.startService(FTAMonitorApplication.getContext());
    }

    // References to the teams
    private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_blue1 = m_field.getBlue1();
    private final TeamStatus m_blue2 = m_field.getBlue2();
    private final TeamStatus m_blue3 = m_field.getBlue3();
    private final TeamStatus m_red1 = m_field.getRed1();
    private final TeamStatus m_red2 = m_field.getRed2();
    private final TeamStatus m_red3 = m_field.getRed3();
    private int m_maxBandwidth;

    private Context m_context;
    private boolean m_alwaysNotify = false;
    private final Collection<ProblemObserver> m_observers = new ArrayList<>();

    public void startService(Context context) {
        m_context = context;
        SharedPreferences m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String notifyAlwaysKey = context.getString(R.string.notify_always_key);
        String bandwidthKey = m_context.getResources().getString(R.string.bandwidth_key);
        m_alwaysNotify = m_sharedPreferences.getBoolean(notifyAlwaysKey, false);
        m_maxBandwidth = Integer.parseInt(m_sharedPreferences.getString(bandwidthKey, "10"));

        // Register an observer for each team
        m_observers.add(new ProblemObserver(1, RED, m_red1));
        m_observers.add(new ProblemObserver(2, RED, m_red2));
        m_observers.add(new ProblemObserver(3, RED, m_red3));
        m_observers.add(new ProblemObserver(1, BLUE, m_blue1));
        m_observers.add(new ProblemObserver(2, BLUE, m_blue2));
        m_observers.add(new ProblemObserver(3, BLUE, m_blue3));
    }

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
            case TELEOP:
                return true;
            default:
                return false;
        }
    }

    private void updateNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(m_context);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        int notifications = 0;
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (ProblemObserver observer : m_observers) {
            if (observer.shouldDisplay()) {
                notifications++;
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(getShortName(observer.getAlliance()));
                sb.append(observer.getStationNumber());
                inboxStyle.addLine(observer.getText());
            }
        }

        NotificationManager manager = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifications == 0) {
            manager.cancel(ID);
            return;
        }

        Intent mainIntent = new Intent(m_context, DrawerActivity.class);
        PendingIntent pi = PendingIntent.getActivity(m_context, MAIN_ACTIVITY_ID, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(notifications + " team issues")
                .setContentText(sb.toString())
                .setContentIntent(pi)
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        manager.notify(ID, builder.build());
    }

    private String getShortName(Alliance alliance) {
        switch (alliance) {
            case BLUE:
                return "B";
            case RED:
                return "R";
            default:
                return "";
        }
    }

    private class ProblemObserver implements Observer<UpdateType> {

        private int m_stationNumber;
        private Alliance m_alliance;
        private TeamStatus m_team;
        private boolean m_display = false;
        private String m_errorString = "";

        private ProblemObserver(int stationNumber, Alliance alliance, TeamStatus team) {
            m_stationNumber = stationNumber;
            m_alliance = alliance;
            m_team = team;
            m_team.registerObserver(this);
        }

        @Override
        public void update(UpdateType updateType) {
            // We only care about the states in which there could be an error
            switch (updateType) {
                case TEAM:
                    // If we should always notify or if a match is playing, then process an update
                    if (m_alwaysNotify || isMatchPlaying()) {
                        updateErrorText();
                    }
            }
        }

        public void deregister() {
            m_team.unregisterObserver(this);
        }

        public boolean shouldDisplay() {
            return m_display;
        }

        public String getText() {
            return m_errorString;
        }

        public Alliance getAlliance() {
            return m_alliance;
        }

        public int getStationNumber() {
            return m_stationNumber;
        }

        /**
         * Checks all of the error conditions and sets any necessary changes to the error text and
         * display variable
         */
        private void updateErrorText() {
            String prevText = m_errorString;
            boolean prevDisplay = m_display;
            if (m_team.isEstop()) {
                setError(R.string.estop_error);
            } else if (m_team.isBypassed()) {
                // If the team is bypassed, we're done here
                m_display = false;
            } else if (!m_team.isDsEth()) {
                setError(R.string.ds_ethernet_error);
            } else if (!m_team.isDs()) {
                setError(R.string.ds_error);
            } else if (!m_team.isRadio()) {
                setError(R.string.radio_error);
            } else if (!m_team.isRobot()) {
                setError(R.string.robot_error);
            } else if (!m_team.isCode()) {
                setError(R.string.code_error);
            } else if (m_team.getDataRate() > m_maxBandwidth) {
                setError(R.string.bandwidth_error);
            } else {
                m_display = false;
            }

            // If any of the factors affecting the notification have changed, update. Otherwise, don't
            if (!m_errorString.equals(prevText) || m_display != prevDisplay) {
                updateNotification();
            }
        }

        /**
         * Sets the display to true and set the error to the given string id
         *
         * @param errorTextId The id of the error text to format and display
         */
        private void setError(int errorTextId) {
            m_display = true;
            String textFormat = m_context.getString(R.string.main_error_string);
            String errorText = m_context.getString(errorTextId);
            m_errorString = String.format(
                    textFormat,
                    m_alliance.toString(),
                    m_stationNumber,
                    m_team.getTeamNumber(),
                    errorText);
        }

        @Override
        public String toString() {
            return "ProblemObserver{" +
                    "m_stationNumber=" + m_stationNumber +
                    ", m_alliance=" + m_alliance +
                    ", m_team=" + m_team +
                    ", m_display=" + m_display +
                    ", m_errorString='" + m_errorString + '\'' +
                    '}';
        }
    }
}
