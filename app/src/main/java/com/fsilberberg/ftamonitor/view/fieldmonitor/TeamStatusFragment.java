package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;
import com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes.MatchPlayInfo;

import org.w3c.dom.Text;

import java.text.DecimalFormat;

import static android.view.View.MeasureSpec;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeamStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeamStatusFragment extends Fragment implements IObserver<TeamUpdateType> {
    // Bundle parameters
    private static final String TEAM_NUMBER = "team_number";
    private static final String ALLIANCE_COLOR = "alliance_color";

    // Team information
    private int m_teamNumber;
    private Alliance m_allianceColor;
    private TeamStatus m_teamStatus;
    private FieldStatus m_fieldStatus;

    // Views
    // Always visible views
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

    // Important IDs
    private final int redBox = R.drawable.red_with_border;
    private final int greenBox = R.drawable.green_with_border;
    private final int blackBox = R.drawable.black_with_border;
    private final int yellowBox = R.drawable.yellow_with_border;
    private final int whiteBox = R.drawable.white_with_border;
    private final int whiteText = R.color.white_text;
    private final int blackText = R.color.black_text;

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
        args.putInt(TEAM_NUMBER, teamNumber);
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
            m_teamNumber = getArguments().getInt(TEAM_NUMBER);
            m_allianceColor = Alliance.values()[getArguments().getInt(ALLIANCE_COLOR)];
        } else {
            throw new RuntimeException("Error, team row was created without using the factory. Team row must be created with makeInstance");
        }

        switch (m_allianceColor) {
            case RED:
                if (m_teamNumber == 1) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getRed1();
                } else if (m_teamNumber == 2) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getRed2();
                } else if (m_teamNumber == 3) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getRed3();
                } else {
                    throw new RuntimeException("Error, station numbers must be between 1 and 3. You gave" + m_teamNumber);
                }
                break;
            case BLUE:
                if (m_teamNumber == 1) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getBlue1();
                } else if (m_teamNumber == 2) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getBlue2();
                } else if (m_teamNumber == 3) {
                    m_teamStatus = FieldMonitorFactory.getInstance().getFieldStatus().getBlue3();
                } else {
                    throw new RuntimeException("Error, station numbers must be between 1 and 3. You gave" + m_teamNumber);
                }
                break;
        }

        m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        m_teamStatus.registerObserver(this);
        Log.d(TeamStatusFragment.class.getName(), "Registered observer on " + m_allianceColor + " station " + m_teamNumber);

        // Update all fields
        for (TeamUpdateType type : TeamUpdateType.values()) {
            update(type);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_teamStatus.deregisterObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_team_status_fragment, container, false);
        // Find and set all of the view elements
        m_teamNumberView = (TextView) fragView.findViewById(R.id.team_status_number);
        m_voltageView = (TextView) fragView.findViewById(R.id.team_status_voltage);
        m_enabledView = (TextView) fragView.findViewById(R.id.team_status_enable_status);
        m_errorTextView = (TextView) fragView.findViewById(R.id.team_status_error_text);
        m_bandwidthView = (TextView) fragView.findViewById(R.id.team_status_bandwidth);
        m_missedPacketsView = (TextView) fragView.findViewById(R.id.team_status_missed_packets);
        m_roundTripView = (TextView) fragView.findViewById(R.id.team_status_round_trip);
        m_signalQualityView = (TextView) fragView.findViewById(R.id.team_status_signal_quality);
        m_signalStrengthView = (TextView) fragView.findViewById(R.id.team_status_signal_strength);

        fragView.post(new Runnable() {
            @Override
            public void run() {
                // Set the error layout params
                m_bandwidthView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int bandwidthWidth = m_bandwidthView.getWidth();
                int missedPacketsWidth = m_missedPacketsView.getWidth();
                int rttWidth = m_roundTripView.getWidth();
                int signalQWidth = m_signalQualityView.getWidth();
                int signalSWidth = m_signalStrengthView.getWidth();
                int errorWidth = bandwidthWidth + missedPacketsWidth + rttWidth + signalQWidth + signalSWidth;
                int errorHeight = m_bandwidthView.getHeight();
                FrameLayout.LayoutParams errorParams = new FrameLayout.LayoutParams(errorWidth, errorHeight);
                errorParams.gravity = Gravity.RIGHT;
                m_errorTextView.setLayoutParams(errorParams);
            }
        });

        return fragView;
    }


    @Override
    public void update(TeamUpdateType updateType) {
        switch (updateType) {
            case BYPASSED:
            case ESTOP:
            case ENABLED:
                updateTeamEnableStatus();
                break;
            case BATTERY:
                float batteryVoltage = m_teamStatus.getBattery();
                setText(m_voltageView, batteryVoltage);
                break;
            case TEAM_NUMBER:
                updateTeamNumber();
                break;
            case DATA_RATE:
                float bandwidth = m_teamStatus.getDataRate();
                setText(m_bandwidthView, bandwidth);
                break;
            case DROPPED_PACKETS:
                int droppedPackets = m_teamStatus.getDroppedPackets();
                setText(m_missedPacketsView, droppedPackets);
                break;
            case SIGNAL_QUALITY:
                float sq = m_teamStatus.getSignalQuality();
                setText(m_signalQualityView, sq);
                break;
            case SIGNAL_STRENGTH:
                float ss = m_teamStatus.getSignalStrength();
                setText(m_signalStrengthView, ss);
                break;
            case ROUND_TRIP:
                int rtt = m_teamStatus.getRoundTrip();
                setText(m_roundTripView, rtt);
                break;
            case DS_ETH:
            case DS:
            case RADIO:
            case ROBOT:
            case CODE:
                updateTeamEnableStatus();
                break;
        }
    }

    /**
     * Reads the current team enable status and sets the enable box based on it
     */
    private void updateTeamEnableStatus() {
        if (m_teamStatus.isEstop()) {
            setBackground(m_enabledView, blackBox);
            setText(m_enabledView, "E", whiteText);
        } else if (m_teamStatus.isBypassed()) {
            setBackground(m_enabledView, redBox);
            setText(m_enabledView, "B", blackText);
        } else if (m_teamStatus.isEnabled()) {
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
     * The team number column isn't fixed width, so we need to make it stay fixed width by saving
     * the old width, updating the text, and setting the old width again
     */
    private void updateTeamNumber() {
        final int teamNumber = m_teamStatus.getTeamNumber();
        getView().post(new Runnable() {
            @Override
            public void run() {
                int oldTeamWidth = m_teamNumberView.getWidth();
                TableRow.LayoutParams teamParams = (TableRow.LayoutParams) m_teamNumberView.getLayoutParams();
                m_teamNumberView.setText(String.valueOf(teamNumber));
                teamParams.width = oldTeamWidth;
                m_teamNumberView.setLayoutParams(teamParams);
            }
        });
    }

    /**
     * Sets all of the data related views visible or invisible
     *
     * @param visible Which state to set
     */
    private void setDataInfoVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        m_bandwidthView.setVisibility(visibility);
        m_missedPacketsView.setVisibility(visibility);
        m_roundTripView.setVisibility(visibility);
        m_signalQualityView.setVisibility(visibility);
        m_signalStrengthView.setVisibility(visibility);
    }

    /**
     * Sets the team status field to visible or invisible
     *
     * @param visible Which state to set
     */
    private void setTeamStatusVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        m_errorTextView.setVisibility(visibility);
    }

    /**
     * Sets the enable box text based on the current gameplay mode. It does not take into account
     * whether or not the team is bypassed or estopped
     */
    private void setEnabledText() {
        MatchStatus status = m_fieldStatus.getMatchStatus();
        switch (status) {
            case TELEOP_PAUSED:
            case TELEOP:
                setText(m_enabledView, "T", blackText);
                break;
            case AUTO:
            case AUTO_END:
            case AUTO_PAUSED:
                setText(m_enabledView, "A", blackText);
                break;
            case OVER:
            case READY_TO_PRESTART:
            case PRESTART_INITIATED:
            case PRESTART_COMPLETED:
            case MATCH_READY:
                setText(m_enabledView, "", blackText);
        }
    }

    /**
     * Helper method to run a background update for a view element on the ui thread
     *
     * @param target The element to update
     * @param resId  The id of the background to update to
     */
    private void setBackground(final View target, final int resId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                target.setBackground(getResources().getDrawable(resId));
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
                target.setTextColor(colorId);
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

    private void setTextFixedColumns(final TextView target, final String newText) {
        // Save the old widths
        int teamWidth = m_teamNumberView.getWidth();
        int voltageWidth = m_voltageView.getWidth();
        int enabledWidth = m_enabledView.getWidth();
        int bwuWidth = m_bandwidthView.getWidth();
        int mpWidth = m_missedPacketsView.getWidth();
        int rttWidth = m_roundTripView.getWidth();
        int sqWidth = m_signalQualityView.getWidth();
        int ssWidth = m_signalStrengthView.getWidth();

        // Save the layout params
        TableRow.LayoutParams teamParams = (TableRow.LayoutParams) m_teamNumberView.getLayoutParams();
        TableRow.LayoutParams voltageParams = (TableRow.LayoutParams) m_voltageView.getLayoutParams();
        TableRow.LayoutParams enabledParams = (TableRow.LayoutParams) m_enabledView.getLayoutParams();
        TableRow.LayoutParams bwuParams = (TableRow.LayoutParams) m_bandwidthView.getLayoutParams();
        TableRow.LayoutParams mpParams = (TableRow.LayoutParams) m_missedPacketsView.getLayoutParams();
        TableRow.LayoutParams rttParams = (TableRow.LayoutParams) m_roundTripView.getLayoutParams();
        TableRow.LayoutParams sqParams = (TableRow.LayoutParams) m_signalQualityView.getLayoutParams();
        TableRow.LayoutParams ssParams = (TableRow.LayoutParams) m_signalStrengthView.getLayoutParams();

        // Update the target
        target.setText(newText);

        // Reset the layout params
        teamParams.width = teamWidth;
        voltageParams.width = voltageWidth;
        enabledParams.width = enabledWidth;
        bwuParams.width = bwuWidth;
        mpParams.width = mpWidth;
        rttParams.width = rttWidth;
        sqParams.width = sqWidth;
        ssParams.width = ssWidth;

        // Set the layout parameters
        m_teamNumberView.setLayoutParams(teamParams);
        m_voltageView.setLayoutParams(voltageParams);
        m_enabledView.setLayoutParams(enabledParams);
        m_bandwidthView.setLayoutParams(bwuParams);
        m_missedPacketsView.setLayoutParams(mpParams);
        m_roundTripView.setLayoutParams(rttParams);
        m_signalQualityView.setLayoutParams(sqParams);
        m_signalStrengthView.setLayoutParams(ssParams);
    }
}
