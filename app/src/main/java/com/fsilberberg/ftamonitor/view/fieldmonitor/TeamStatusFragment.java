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
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;

import org.w3c.dom.Text;

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
    private TextView m_signalQualityView;
    private TextView m_signalStrengthView;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        m_teamStatus.registerObserver(this);
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
        m_signalQualityView = (TextView) fragView.findViewById(R.id.team_status_signal_quality);
        m_signalStrengthView = (TextView) fragView.findViewById(R.id.team_status_signal_strength);

        fragView.post(new Runnable() {
            @Override
            public void run() {
                // Set the error layout params
                m_bandwidthView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                // All of the widths of these cells are the same (stretched in the table) so we just
                // multiply by 4 to get the new width
                Log.d(TeamStatusFragment.class.getName(), "The measured width is " + m_bandwidthView.getMeasuredWidth() + " and the width is " + m_bandwidthView.getWidth());
                // Subtract 3 to account for padding
                int errorWidth = (m_bandwidthView.getWidth() - 3) * 4;
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

    }
}
