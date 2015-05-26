package com.fsilberberg.ftamonitor.view.old.fieldmonitor;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.UpdateType;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeamStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeamStatusFragment extends Fragment implements Observer<UpdateType> {
    // Bundle parameters
    private static final String STATION_NUMBER = "station_number";
    private static final String ALLIANCE_COLOR = "alliance_color";

    // Team information
    private int m_stationNumber;
    private Alliance m_allianceColor;
    private TeamStatus m_teamStatus;
    private FieldStatus m_fieldStatus;

    private TextView m_teamNumberView;
    private TextView m_voltageView;
    private TextView m_enabledView;
    // Error related views
    private TextView m_errorTextView;
    // Data views when teams are OK
    private TextView m_bandwidthView;
    private TextView m_missedPacketsView;
    private TextView m_roundTripView;
    private TextView m_signalQualityView;
    private TextView m_signalStrengthView;

    private final int whiteText = R.color.white_text;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param color      The alliance color of this row.
     * @param teamNumber The team number of this row.
     * @return A new instance of fragment TeamStatusFragment.
     */
    public static TeamStatusFragment newInstance(int teamNumber, Alliance color) {
        TeamStatusFragment fragment = new TeamStatusFragment();
        Bundle args = new Bundle();
        args.putInt(STATION_NUMBER, teamNumber);
        args.putInt(ALLIANCE_COLOR, color.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    public TeamStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            m_stationNumber = getArguments().getInt(STATION_NUMBER);
            m_allianceColor = Alliance.values()[getArguments().getInt(ALLIANCE_COLOR)];
        } else {
            throw new RuntimeException("Error, team row was created without using the factory. Team row must be created with makeInstance");
        }

        switch (m_allianceColor) {
            case RED:
                if (m_stationNumber == 1) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getRed1();
                } else if (m_stationNumber == 2) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getRed2();
                } else if (m_stationNumber == 3) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getRed3();
                } else {
                    throw new RuntimeException("Error, station numbers must be between 1 and 3. You gave" + m_stationNumber);
                }
                break;
            case BLUE:
                if (m_stationNumber == 1) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getBlue1();
                } else if (m_stationNumber == 2) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getBlue2();
                } else if (m_stationNumber == 3) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getBlue3();
                } else {
                    throw new RuntimeException("Error, station numbers must be between 1 and 3. You gave" + m_stationNumber);
                }
                break;
        }

        m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        m_teamStatus.registerObserver(this);

        // Update all fields
        update(UpdateType.TEAM);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_teamStatus.unregisterObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_team_status, container, false);
        // Find and set all of the view elements
        TextView m_stationNumberView = (TextView) fragView.findViewById(R.id.team_status_table_station_number);
        m_teamNumberView = (TextView) fragView.findViewById(R.id.team_status_number);
        m_voltageView = (TextView) fragView.findViewById(R.id.team_status_voltage);
        m_enabledView = (TextView) fragView.findViewById(R.id.team_status_enable_status);
        m_errorTextView = (TextView) fragView.findViewById(R.id.team_status_error_text);
        m_bandwidthView = (TextView) fragView.findViewById(R.id.team_status_bandwidth);
        m_missedPacketsView = (TextView) fragView.findViewById(R.id.team_status_missed_packets);
        m_roundTripView = (TextView) fragView.findViewById(R.id.team_status_round_trip);
        m_signalQualityView = (TextView) fragView.findViewById(R.id.team_status_signal_quality);
        m_signalStrengthView = (TextView) fragView.findViewById(R.id.team_status_signal_strength);

        // Set the station number
        setText(m_stationNumberView, m_stationNumber);
        // Set the backgrounds for the team and station numbers
        int blueRobotBox = R.drawable.blue_robot_with_border;
        int redRobotBox = R.drawable.red_robot_with_border;
        int backRes = m_allianceColor == Alliance.RED ? redRobotBox : blueRobotBox;
        int textColor = R.color.white_text;
        setBackground(m_stationNumberView, backRes);
        setBackground(m_teamNumberView, backRes);
        setText(m_stationNumberView, String.valueOf(m_stationNumber), textColor);
        setText(m_teamNumberView, String.valueOf(m_stationNumber), textColor);

        fragView.post(new Runnable() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void run() {
                // Set the error layout params. There are 4 columns with equal width, so we can
                // just multiply the width by 4
                int bandwidthWidth = m_bandwidthView.getWidth();
                int errorWidth = bandwidthWidth * 4;
                int errorHeight = m_bandwidthView.getHeight();
                FrameLayout.LayoutParams errorParams = new FrameLayout.LayoutParams(errorWidth, errorHeight);
                errorParams.gravity = Gravity.RIGHT;
                m_errorTextView.setLayoutParams(errorParams);
            }
        });

        return fragView;
    }


    @Override
    public void update(UpdateType updateType) {
        if (updateType == UpdateType.TEAM) {
            updateTeamEnableStatus();
            float batteryVoltage = m_teamStatus.getBattery();
            setText(m_voltageView, batteryVoltage);
            int teamNumber = m_teamStatus.getTeamNumber();
            setText(m_teamNumberView, teamNumber);
            float bandwidth = m_teamStatus.getDataRate();
            setText(m_bandwidthView, bandwidth);
            int droppedPackets = m_teamStatus.getDroppedPackets();
            setText(m_missedPacketsView, droppedPackets);
            float sq = m_teamStatus.getSignalQuality();
            setText(m_signalQualityView, sq);
            float ss = m_teamStatus.getSignalStrength();
            setText(m_signalStrengthView, ss);
            int rtt = m_teamStatus.getRoundTrip();
            setText(m_roundTripView, rtt);
            updateTeamStatus();
        }
    }

    /**
     * Reads the current team enable status and sets the enable box based on it
     */
    private void updateTeamEnableStatus() {
        int redBox = R.drawable.red_with_border;
        if (m_teamStatus.isEstop()) {
            int blackBox = R.drawable.black_with_border;
            setBackground(m_enabledView, blackBox);
            setText(m_enabledView, "E", whiteText);
        } else if (m_teamStatus.isBypassed()) {
            setBackground(m_enabledView, redBox);
            setText(m_enabledView, "B", whiteText);
        } else if (m_teamStatus.isEnabled()) {
            int greenBox = R.drawable.green_with_border;
            setBackground(m_enabledView, greenBox);
            setEnabledText();
        } else {
            setBackground(m_enabledView, redBox);
            setEnabledText();
        }
    }

    /**
     * Helper method to take care of setting up the correct team status views and what they should
     * read
     */
    private void updateTeamStatus() {
        if (!m_teamStatus.isDsEth()) {
            setDataInfoVisible(false);
            setTeamStatusVisible(true);
            setText(m_errorTextView, getString(R.string.ds_ethernet));
        } else if (!m_teamStatus.isDs()) {
            setDataInfoVisible(false);
            setTeamStatusVisible(true);
            setText(m_errorTextView, getString(R.string.ds));
        } else if (!m_teamStatus.isRadio()) {
            setDataInfoVisible(false);
            setTeamStatusVisible(true);
            setText(m_errorTextView, getString(R.string.radio));
        } else if (!m_teamStatus.isRobot()) {
            setDataInfoVisible(false);
            setTeamStatusVisible(true);
            setText(m_errorTextView, getString(R.string.rio));
        } else if (!m_teamStatus.isCode()) {
            setDataInfoVisible(false);
            setTeamStatusVisible(true);
            setText(m_errorTextView, getString(R.string.code));
        } else {
            setTeamStatusVisible(false);
            setDataInfoVisible(true);
        }
    }

    /**
     * Sets all of the data related views visible or invisible
     *
     * @param visible Which state to set
     */
    private void setDataInfoVisible(boolean visible) {
        final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_bandwidthView.setVisibility(visibility);
                m_missedPacketsView.setVisibility(visibility);
                m_roundTripView.setVisibility(visibility);
                m_signalQualityView.setVisibility(visibility);
                m_signalStrengthView.setVisibility(visibility);
            }
        });

    }

    /**
     * Sets the team status field to visible or invisible
     *
     * @param visible Which state to set
     */
    private void setTeamStatusVisible(boolean visible) {
        final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_errorTextView.setVisibility(visibility);
            }
        });
    }

    /**
     * Sets the enable box text based on the current gameplay mode. It does not take into account
     * whether or not the team is bypassed or estopped
     */
    private void setEnabledText() {
        MatchStatus status = m_fieldStatus.getMatchStatus();
        switch (status) {
            case TELEOP:
                setText(m_enabledView, "T", whiteText);
                break;
            case AUTO:
                setText(m_enabledView, "A", whiteText);
                break;
            default:
                setText(m_enabledView, "", whiteText);
                break;
        }
    }

    /**
     * Helper method to run a background update for a view element on the ui thread
     *
     * @param target The element to update
     * @param resId  The id of the background to update to
     */
    @SuppressLint({"Deprecation", "NewApi"})
    private void setBackground(final View target, final int resId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //noinspection deprecation
                    target.setBackground(getResources().getDrawable(resId));
                } else {
                    target.setBackground(getResources().getDrawable(resId, null));
                }
            }
        });
    }

    /**
     * Helper method to update the text of a TextView element on the ui thread.
     *
     * @param target  The textview to update
     * @param newText The new text for the view
     */
    private void setText(final TextView target, final String newText) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                target.setText(newText);
            }
        });
    }

    /**
     * Helper method to update the text and text color of a view on the ui thread.
     *
     * @param target  The textview to update
     * @param newText The new text for the view
     * @param colorId The new color of the text
     */
    private void setText(final TextView target, final String newText, final int colorId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                target.setText(newText);
                target.setTextColor(getResources().getColor(colorId));
            }
        });
    }

    /**
     * Helper method to update the text of a textview to a given decimal, rounded to 2 places
     *
     * @param target The textview to update
     * @param newVal The decimal to display, 2 decimal places
     */
    private void setText(final TextView target, final float newVal) {
        // Formatter for the decimal places. Formats to 2 decimals
        final DecimalFormat df = new DecimalFormat("#.##");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                target.setText(df.format(newVal));
            }
        });
    }

    /**
     * Helper method to update the text of a textview to a given decimal, rounded to 2 places
     *
     * @param target The textview to update
     * @param newVal The decimal to display, 2 decimal places
     */
    private void setText(final TextView target, final int newVal) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                target.setText(String.valueOf(newVal));
            }
        });
    }
}
