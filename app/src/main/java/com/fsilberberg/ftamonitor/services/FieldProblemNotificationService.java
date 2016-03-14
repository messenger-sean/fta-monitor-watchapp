package com.fsilberberg.ftamonitor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.Observable;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.view.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Object s_lock = new Object();

    public static void start() {
        synchronized (s_lock) {
            if (instance != null) {
                instance.stopService();
                instance = null;
            }

            instance = new FieldProblemNotificationService();
            instance.startService(FTAMonitorApplication.getContext());
        }
    }

    // References to the teams
    private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
    private final TeamStatus m_blue1 = m_field.getBlue1();
    private final TeamStatus m_blue2 = m_field.getBlue2();
    private final TeamStatus m_blue3 = m_field.getBlue3();
    private final TeamStatus m_red1 = m_field.getRed1();
    private final TeamStatus m_red2 = m_field.getRed2();
    private final TeamStatus m_red3 = m_field.getRed3();
    private boolean m_bandwidthNotify;
    private float m_maxBandwidth;
    private boolean m_lowBatteryNotify;
    private float m_lowBattery;

    private Context m_context;
    private boolean m_alwaysNotify = false;
    private final Collection<ProblemObserver> m_observers = new ArrayList<>();

    public void startService(Context context) {
        m_context = context;
        SharedPreferences m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String enabledKey = context.getString(R.string.notification_key);
        boolean enabled = m_sharedPreferences.getBoolean(enabledKey, false);

        if (enabled) {
            String notifyAlwaysKey = context.getString(R.string.notify_always_key);
            String bandwidthNotifyKey = context.getString(R.string.bandwidth_notify_key);
            String bandwidthKey = m_context.getString(R.string.bandwidth_key);
            String lowBatteryNotifyKey = m_context.getString(R.string.low_battery_notify_key);
            String lowBatteryKey = context.getString(R.string.low_battery_key);
            m_alwaysNotify = m_sharedPreferences.getBoolean(notifyAlwaysKey, false);
            m_bandwidthNotify = m_sharedPreferences.getBoolean(bandwidthNotifyKey, true);
            m_maxBandwidth = Float.parseFloat(m_sharedPreferences.getString(bandwidthKey, "7"));
            m_lowBatteryNotify = m_sharedPreferences.getBoolean(lowBatteryNotifyKey, true);
            m_lowBattery = Float.parseFloat(m_sharedPreferences.getString(lowBatteryKey, "6.5"));

            // Register an observer for each team
            m_observers.add(new ProblemObserver(1, RED, m_red1).init());
            m_observers.add(new ProblemObserver(2, RED, m_red2).init());
            m_observers.add(new ProblemObserver(3, RED, m_red3).init());
            m_observers.add(new ProblemObserver(1, BLUE, m_blue1).init());
            m_observers.add(new ProblemObserver(2, BLUE, m_blue2).init());
            m_observers.add(new ProblemObserver(3, BLUE, m_blue3).init());

            // Update the notification for initial state
            for (ProblemObserver observer : m_observers) {
                observer.updateErrorText();
            }
        } else {
            if (instance != null) {
                instance.stopService();
            }
        }
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
        Collection<ProblemObserver> displayedObservers = new ArrayList<>();

        // If there's only one team, only one line is displayed, so have that line be the full
        // line. Otherwise, use inbox style.
        for (ProblemObserver observer : m_observers) {
            if (observer.shouldDisplay()) {
                notifications++;
                displayedObservers.add(observer);
            }
        }

        for (ProblemObserver observer : displayedObservers) {
            if (notifications > 1) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(getShortName(observer.getAlliance()));
                sb.append(observer.getStationNumber());
                inboxStyle.addLine(observer.getText());
            } else {
                sb.append(observer.getText());
            }
        }

        NotificationManager manager = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifications == 0) {
            manager.cancel(ID);
            return;
        }

        Intent mainIntent = new Intent(m_context, MainActivity.class);
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

    private class ProblemObserver extends Observable.OnPropertyChangedCallback {
        // robotStatus, estop, bypassed, battery, and dataRate all used directly in calculations.
        // For matchStatus, if the out-of-match notify is turned off, notifications won't be updated
        // in match until something changes, so we make sure that match status state changes rechecks
        // the notification.
        private final Collection<Integer> updateValues = Arrays.asList(BR.robotStatus,
                BR.estop, BR.bypassed, BR.battery, BR.dataRate, BR.matchStatus);

        private volatile int m_stationNumber;
        private volatile Alliance m_alliance;
        private volatile TeamStatus m_team;
        private volatile boolean m_display = false;
        private volatile String m_errorString = "";

        private ProblemObserver(int stationNumber, Alliance alliance, TeamStatus team) {
            m_stationNumber = stationNumber;
            m_alliance = alliance;
            m_team = team;
        }

        public ProblemObserver init() {
            m_team.addOnPropertyChangedCallback(this);
            return this;
        }

        public void deregister() {
            m_team.removeOnPropertyChangedCallback(this);
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
            if (!(m_alwaysNotify || isMatchPlaying())) {
                return;
            }

            String prevText = m_errorString;
            boolean prevDisplay = m_display;
            if (m_team.isEstop()) {
                setError(R.string.estop_error);
            } else if (m_team.isBypassed()) {
                // If the team is bypassed, we're done here
                m_display = false;
            } else {
                switch (m_team.getRobotStatus()) {
                    case NO_DS_ETH:
                        setError(R.string.ds_ethernet_error);
                        break;
                    case NO_DS:
                        setError(R.string.ds_error);
                        break;
                    case NO_RADIO:
                        setError(R.string.radio_error);
                        break;
                    case NO_RIO:
                        setError(R.string.robot_error);
                        break;
                    case NO_CODE:
                        setError(R.string.code_error);
                        break;
                    case GOOD:
                    default:
                        if (m_lowBatteryNotify && m_team.getBattery() < m_lowBattery) {
                            setError(R.string.low_battery_error);
                        } else if (m_bandwidthNotify && m_team.getDataRate() > m_maxBandwidth) {
                            setError(R.string.bandwidth_error);
                        } else {
                            m_display = false;
                        }
                        break;
                }
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
        private void setError(@StringRes int errorTextId) {
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

        @Override
        public void onPropertyChanged(Observable observable, int propertyChanged) {
            if (updateValues.contains(propertyChanged)) {
                updateErrorText();
            }
        }
    }
}
