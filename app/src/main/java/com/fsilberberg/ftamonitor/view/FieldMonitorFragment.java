package com.fsilberberg.ftamonitor.view;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

import static com.fsilberberg.ftamonitor.common.Alliance.*;

/**
 * A simple {@link Fragment} subclass.
 * TODO: Support Landscape
 */
public class FieldMonitorFragment extends Fragment {

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

    public FieldMonitorFragment() {
        // Required empty public constructor
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
            return;
        }

        // TODO: Actually setup the monitor to update the UI
    }

    private void stopMonitor() {
        // TODO: Actually stop  the monitor
    }

    private void updateValues() {
        FieldStatus status = FieldMonitorFactory.getInstance().getFieldStatus();
        m_matchNumber.setText(status.getMatchNumber());
        m_fieldStatus.setText(status.getMatchStatus().toString());
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
}
