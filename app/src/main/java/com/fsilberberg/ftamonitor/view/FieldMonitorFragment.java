package com.fsilberberg.ftamonitor.view;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldUpdateType;
import com.fsilberberg.ftamonitor.fieldmonitor.IFieldMonitorObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;

import org.w3c.dom.Text;

import java.lang.reflect.Field;

import static com.fsilberberg.ftamonitor.common.Alliance.*;

/**
 * A simple {@link Fragment} subclass.
 * TODO: Support Landscape
 */
public class FieldMonitorFragment extends Fragment implements IFieldMonitorObserver {

    // The width of the columns
    private int m_teamWidth;
    private int m_dsWidth;
    private int m_robotWidth;
    private int m_voltageWidth;
    private int m_bandwidthWidth;
    private int m_signalWidth;
    private int m_enableWidth;
    private View m_fragView;

    // The team fragments
    private FieldMonitorTeamRow blue1;
    private FieldMonitorTeamRow blue2;
    private FieldMonitorTeamRow blue3;
    private FieldMonitorTeamRow red1;
    private FieldMonitorTeamRow red2;
    private FieldMonitorTeamRow red3;
    private boolean updateFragment = false;

    // The UI elements that we update in app
    private TextView m_matchNumber;
    private TextView m_fieldStatus;

    // The countdown timer for the match time
    // TODO: Implement class
    private CountDownTimer m_matchTimer;
    private int m_remainingSeconds;
    private SharedPreferences m_sharedPreferences;

    public FieldMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_fragView = inflater.inflate(R.layout.fragment_field_monitor, container, false);
        m_fragView.post(new SetupRunnable());
        m_matchNumber = (TextView) m_fragView.findViewById(R.id.field_monitor_match_number);
        m_fieldStatus = (TextView) m_fragView.findViewById(R.id.field_monitor_status);
        return m_fragView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(getString(R.string.action_field_monitor));

        if (updateFragment) {
            setupFragments();
            updateFragment = false;
        }

        startMonitor();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopMonitor();
    }

    private FieldMonitorTeamRow getRow(Alliance alliance) {
        return FieldMonitorTeamRow.newInstance(m_teamWidth, m_dsWidth, m_robotWidth, m_voltageWidth, m_bandwidthWidth, m_signalWidth, m_enableWidth, alliance);
    }

    private void setupFragments() {
        // Get the column widths
        m_teamWidth = m_fragView.findViewById(R.id.field_monitor_team_number_header).getWidth();
        m_dsWidth = m_fragView.findViewById(R.id.field_monitor_ds_header).getWidth();
        m_robotWidth = m_fragView.findViewById(R.id.field_monitor_robot_header).getWidth();
        m_voltageWidth = m_fragView.findViewById(R.id.field_monitor_battery_voltage_header).getWidth();
        m_bandwidthWidth = m_fragView.findViewById(R.id.field_monitor_bandwidth_header).getWidth();
        m_signalWidth = m_fragView.findViewById(R.id.field_monitor_signal_header).getWidth();
        m_enableWidth = m_fragView.findViewById(R.id.field_monitor_enable_status_header).getWidth();

        // Get the individual team rows
        blue1 = getRow(BLUE);
        blue2 = getRow(BLUE);
        blue3 = getRow(BLUE);
        red1 = getRow(RED);
        red2 = getRow(RED);
        red3 = getRow(RED);

        // Insert them into the fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.field_monitor_blue1, blue1)
                .replace(R.id.field_monitor_blue2, blue2)
                .replace(R.id.field_monitor_blue3, blue3)
                .replace(R.id.field_monitor_red1, red1)
                .replace(R.id.field_monitor_red2, red2)
                .replace(R.id.field_monitor_red3, red3)
                .commit();

        startMonitor();
    }

    private void startMonitor() {
        if (blue1 == null || blue2 == null || blue3 == null || red1 == null || red2 == null || red3 == null) {
            // The app is not setup yet, this will be called when the app is fully setup
            // TODO: Actually update status form monitor
            return;
        }
        FieldMonitorFactory.getInstance().getFieldStatus().registerObserver(this);
    }

    private void stopMonitor() {
        FieldMonitorFactory.getInstance().getFieldStatus().deregisterObserver(this);
    }

    @Override
    public void update(FieldUpdateType update) {
        Log.d(FieldMonitorFragment.class.getName(), "Update type of " + update);

        switch (update) {
            // We don't do anything with the time updates, they are fetched from shared preferences when
            // it is time start the timer
            case AUTO_TIME:
            case TELEOP_TIME:
                break;
            case MATCH_NUMBER:
                final String newMatch = FieldMonitorFactory.getInstance().getFieldStatus().getMatchNumber();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) getActivity().findViewById(R.id.field_monitor_match_number)).setText(newMatch);
                    }
                });
                break;
            case MATCH_STATUS:
                updateMatchStatus(FieldMonitorFactory.getInstance().getFieldStatus().getMatchStatus());
                break;
            default:
                Log.w(FieldMonitorFragment.class.getName(), "Unknown field update type " + update);
        }
    }

    @Override
    public void update(TeamUpdateType update, int teamNum, Alliance alliance) {

    }

    private void updateMatchStatus(final MatchStatus newStatus) {
        switch (newStatus) {
            case AUTO_PAUSED:
            case TELEOP_PAUSED:
                m_matchTimer.cancel();
                break;
            case AUTO:
                m_remainingSeconds = m_sharedPreferences.getInt(getString(R.string.auto_time_key), 0);
                startTimer(m_remainingSeconds);
                break;
            case TELEOP:
                m_remainingSeconds = m_sharedPreferences.getInt(getString(R.string.teleop_time_key), 0);
                startTimer(m_remainingSeconds);
                break;
            case AUTO_END:
                m_matchTimer.cancel();
                break;
            case OVER:
                m_matchTimer.cancel();
            case READY_TO_PRESTART:
            case NOT_READY:
            case MATCH_READY:
            case PRESTART_COMPLETED:
            case PRESTART_INITIATED:
                break;
        }

        // Update the match status text on the UI Thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(FieldMonitorFragment.class.getName(), "Setting the status to be " + newStatus.toString());
                ((TextView) getActivity().findViewById(R.id.field_monitor_status)).setText(newStatus.toString());
            }
        });
    }

    private void startTimer(int remainingSeconds) {
        m_matchTimer.cancel();
        m_remainingSeconds = remainingSeconds;
        m_matchTimer = new MatchTimer(m_remainingSeconds);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_matchTimer.start();
            }
        });
    }

    private final class SetupRunnable implements Runnable {

        @Override
        public void run() {
            try {
                setupFragments();
            } catch (IllegalStateException ex) {
                updateFragment = true;
            }
        }
    }

    private final class MatchTimer extends CountDownTimer {

        public MatchTimer(long millisInFuture) {
            // One thousand millisecond callbacks
            super(millisInFuture, 1000);
        }

        @Override
        public void onTick(long l) {
            m_remainingSeconds -= 1;
            ((TextView) getActivity().findViewById(R.id.field_monitor_time)).setText(m_remainingSeconds + " seconds");
        }

        @Override
        public void onFinish() {
            m_remainingSeconds = 0;
            ((TextView) getActivity().findViewById(R.id.field_monitor_time)).setText(m_remainingSeconds + " seconds");
        }
    }
}
