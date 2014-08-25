package com.fsilberberg.ftamonitor.view;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;

import static com.fsilberberg.ftamonitor.common.Alliance.*;

/**
 * A simple {@link Fragment} subclass.
 * TODO: Support Landscape
 */
public class FieldMonitorFragment extends Fragment {

    private int m_teamWidth;
    private int m_dsWidth;
    private int m_robotWidth;
    private int m_voltageWidth;
    private int m_bandwidthWidth;
    private int m_signalWidth;
    private int m_enableWidth;
    private View m_fragView;

    private boolean updateFragment = false;

    public FieldMonitorFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_fragView = inflater.inflate(R.layout.fragment_field_monitor, container, false);
        m_fragView.post(new SetupRunnable());
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
        FieldMonitorTeamRow blue1 = getRow(BLUE);
        FieldMonitorTeamRow blue2 = getRow(BLUE);
        FieldMonitorTeamRow blue3 = getRow(BLUE);
        FieldMonitorTeamRow red1 = getRow(RED);
        FieldMonitorTeamRow red2 = getRow(RED);
        FieldMonitorTeamRow red3 = getRow(RED);

        // Insert them into the fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.field_monitor_blue1, blue1)
                .replace(R.id.field_monitor_blue2, blue2)
                .replace(R.id.field_monitor_blue3, blue3)
                .replace(R.id.field_monitor_red1, red1)
                .replace(R.id.field_monitor_red2, red2)
                .replace(R.id.field_monitor_red3, red3)
                .commit();
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
