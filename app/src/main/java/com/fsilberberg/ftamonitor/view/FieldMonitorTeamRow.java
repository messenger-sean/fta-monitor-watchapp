package com.fsilberberg.ftamonitor.view;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;

import java.sql.SQLClientInfoException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldMonitorTeamRow extends Fragment {

    // Column width bundle extras
    private static final String TEAM_WIDTH = "TEAM_WIDTH";
    private static final String DS_WIDTH = "DS_WIDTH";
    private static final String ROBOT_WIDTH = "ROBOT_WIDTH";
    private static final String VOLTAGE_WIDTH = "VOLTAGE_WIDTH";
    private static final String BANDWIDTH_WIDTH = "BANDWIDTH_WIDTH";
    private static final String SIGNAL_WIDTH = "SIGNAL_WIDTH";
    private static final String ENABLE_WIDTH = "ENABLE_WIDTH";
    private static final String ALLIANCE = "ALLIANCE";

    private int m_teamWidth;
    private int m_dsWidth;
    private int m_robotWidth;
    private int m_voltageWidth;
    private int m_bandwidthWidth;
    private int m_signalWidth;
    private int m_enableWidth;
    private Alliance m_alliance;

    private boolean m_widthSet = false;

    public static final FieldMonitorTeamRow newInstance(int teamWidth, int dsWidth, int robotWidth, int voltageWidth, int bandwidthWidth, int signalWidth, int enableWidth, Alliance alliance) {
        FieldMonitorTeamRow row = new FieldMonitorTeamRow();
        Bundle args = new Bundle();
        args.putInt(TEAM_WIDTH, teamWidth);
        args.putInt(DS_WIDTH, dsWidth);
        args.putInt(ROBOT_WIDTH, robotWidth);
        args.putInt(VOLTAGE_WIDTH, voltageWidth);
        args.putInt(BANDWIDTH_WIDTH, bandwidthWidth);
        args.putInt(SIGNAL_WIDTH, signalWidth);
        args.putInt(ENABLE_WIDTH, enableWidth);
        args.putInt(ALLIANCE, alliance.ordinal());
        row.setArguments(args);
        return row;
    }

    public FieldMonitorTeamRow() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle args = getArguments();
            m_teamWidth = args.getInt(TEAM_WIDTH);
            m_dsWidth = args.getInt(DS_WIDTH);
            m_robotWidth = args.getInt(ROBOT_WIDTH);
            m_voltageWidth = args.getInt(VOLTAGE_WIDTH);
            m_bandwidthWidth = args.getInt(BANDWIDTH_WIDTH);
            m_signalWidth = args.getInt(SIGNAL_WIDTH);
            m_enableWidth = args.getInt(ENABLE_WIDTH);
            m_alliance = Alliance.values()[args.getInt(ALLIANCE)];
            m_widthSet = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWidths(getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_field_monitor_team_row, container, false);
        return fragView;
    }

    private void updateWidths(View fragView) {
        if (m_widthSet) {
            fragView.findViewById(R.id.field_monitor_team_number).setLayoutParams(getParams(m_teamWidth));
            fragView.findViewById(R.id.field_monitor_ds_bounding_box).setLayoutParams(getParams(m_dsWidth));
            fragView.findViewById(R.id.field_monitor_robot_bounding_box).setLayoutParams(getParams(m_robotWidth));
            fragView.findViewById(R.id.field_monitor_battery_voltage).setLayoutParams(getParams(m_voltageWidth));
            fragView.findViewById(R.id.field_monitor_bandwidth_bounding_box).setLayoutParams(getParams(m_bandwidthWidth));
            fragView.findViewById(R.id.field_monitor_signal_bounding_box).setLayoutParams(getParams(m_signalWidth));
            fragView.findViewById(R.id.field_monitor_enable_status).setLayoutParams(getParams(m_enableWidth));
            fragView.findViewById(R.id.field_monitor_team_number).setBackground(getResources().getDrawable(m_alliance.getBackgroundId()));
        }
    }

    private LinearLayout.LayoutParams getParams(int width) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        return params;
    }
}
