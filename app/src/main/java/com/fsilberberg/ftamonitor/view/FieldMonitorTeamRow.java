package com.fsilberberg.ftamonitor.view;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;

import java.sql.SQLClientInfoException;
import java.text.DecimalFormat;

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

    // Width settings
    private int m_teamWidth;
    private int m_dsWidth;
    private int m_robotWidth;
    private int m_voltageWidth;
    private int m_bandwidthWidth;
    private int m_signalWidth;
    private int m_enableWidth;
    private Alliance m_alliance;
    private boolean m_widthSet = false;

    // Important IDs
    private int redBox = R.drawable.red_with_border;
    private int greenBox = R.drawable.green_with_border;
    private int blackBox = R.drawable.black_with_border;
    private int yellowBox = R.drawable.yellow_with_border;
    private int whiteBox = R.drawable.white_with_border;

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

    public void updateTeam(TeamUpdateType update, TeamStatus team) {
        switch (update) {
            case TEAM_NUMBER:
                setText(R.id.field_monitor_team_number, getIntegerString(team.getTeamNumber()));
                break;
            case BATTERY:
                setText(R.id.field_monitor_battery_voltage, getFloatString(team.getBattery()));
                break;
            case DATA_RATE:
                setText(R.id.field_monitor_bandwidth_usage, getFloatString(team.getDataRate()));
                break;
            case DROPPED_PACKETS:
                setText(R.id.field_monitor_missed_packets, getIntegerString(team.getDroppedPackets()));
                break;
            case SIGNAL_QUALITY:
                String sqText = getFloatString(team.getSignalQuality()) + "%";
                setText(R.id.field_monitor_signal_quality, sqText);
                break;
            case SIGNAL_STRENGTH:
                String ssText = getFloatString(team.getSignalStrength()) + "%";
                setText(R.id.field_monitor_signal_strength, ssText);
                break;
            case DS_ETH:
                setBackground(R.id.field_monitor_ds_eth, team.isDsEth() ? greenBox : redBox);
                break;
            case DS:
                setBackground(R.id.field_monitor_ds, team.isDs() ? greenBox : redBox);
                break;
            case RADIO:
                setBackground(R.id.field_monitor_radio, team.isRadio() ? greenBox : redBox);
                break;
            case ROBOT:
                setBackground(R.id.field_monitor_rio, team.isRobot() ? greenBox : redBox);
                break;
            case CODE:
                int codeBackground = team.isCode() ? whiteBox : yellowBox;
                setBackground(R.id.field_monitor_battery_voltage, codeBackground);
                setBackground(R.id.field_monitor_bandwidth_usage, codeBackground);
                setBackground(R.id.field_monitor_missed_packets, codeBackground);
                setBackground(R.id.field_monitor_signal_quality, codeBackground);
                setBackground(R.id.field_monitor_signal_strength, codeBackground);
                break;
            case ESTOP:
            case ENABLED:
            case BYPASSED:
                // If either of these changes, do the whole calculation
                if (team.isEstop()) {
                    // Team is estopped, overrides all other statuses
                    setBackground(R.id.field_monitor_enable_status, blackBox);
                    setText(R.id.field_monitor_enable_status, "E");
                } else if (team.isEnabled() && !team.isBypassed()) {
                    // If the team is enabled and not bypassed, they are playing
                    setBackground(R.id.field_monitor_enable_status, greenBox);
                    setText(R.id.field_monitor_enable_status, "");
                } else if (team.isEnabled() && team.isBypassed()) {
                    // The team is bypassed, the robot would be running if they weren't bypassed
                    setBackground(R.id.field_monitor_enable_status, greenBox);
                    setText(R.id.field_monitor_enable_status, "B");
                } else if (!team.isEnabled() && !team.isBypassed()) {
                    // Match is not running, team is disabled and not bypassed
                    setBackground(R.id.field_monitor_enable_status, redBox);
                    setText(R.id.field_monitor_enable_status, "");
                } else {
                    // Final option is disabled and bypassed
                    setBackground(R.id.field_monitor_enable_status, redBox);
                    setText(R.id.field_monitor_enable_status, "B");
                }
                break;
            // TODO Update the team layout to show cards
            // TODO Add round trip
        }
    }

    private void setBackground(final int targetId, final int resId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(targetId).setBackground(getResources().getDrawable(resId));
            }
        });
    }

    private void setText(final int targetId, final String newText) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) getActivity().findViewById(targetId)).setText(newText);
            }
        });
    }

    private String getIntegerString(int i) {
        return Integer.valueOf(i).toString();
    }

    private String getFloatString(float f) {
        return new DecimalFormat("#.##").format(f).toString();
    }
}
