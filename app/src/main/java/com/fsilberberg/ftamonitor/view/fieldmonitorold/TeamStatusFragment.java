package com.fsilberberg.ftamonitor.view.fieldmonitorold;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.databinding.FragmentTeamStatusBinding;
import com.fsilberberg.ftamonitor.databinding.FragmentTeamStatusBlueBinding;
import com.fsilberberg.ftamonitor.databinding.FragmentTeamStatusRedBinding;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.UpdateType;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeamStatusFragment extends Fragment {

    private static final String ALLIANCE_ARG = "alliance_arg";
    private static final String STATION_ARG = "station_arg";

    public static TeamStatusFragment makeInstance(Alliance alliance, Station station) {
        Bundle args = new Bundle();
        args.putInt(ALLIANCE_ARG, alliance.ordinal());
        args.putInt(STATION_ARG, station.ordinal());

        TeamStatusFragment status = new TeamStatusFragment();
        status.setArguments(args);
        return status;
    }

    private final TeamObserver m_observer = new TeamObserver();
    private TeamStatus m_team;
    private ViewGroup m_container;
    private TextView m_bandwidthText;
    private TextView m_mpText;
    private TextView m_ttText;
    private TextView m_errorText;
    private boolean m_isHeader = true;

    public TeamStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!m_isHeader) {
            m_team.registerObserver(m_observer);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!m_isHeader) {
            m_team.unregisterObserver(m_observer);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v;

        if (getArguments() != null) {
            m_isHeader = false;

            Bundle args = getArguments();
            FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
            Alliance alliance = Alliance.values()[args.getInt(ALLIANCE_ARG)];
            int station = args.getInt(STATION_ARG);
            TeamStatus[] red = new TeamStatus[]{
                    field.getRed1(), field.getRed2(), field.getRed3()
            };
            TeamStatus[] blue = new TeamStatus[]{
                    field.getBlue1(), field.getBlue2(), field.getBlue3()
            };

            switch (alliance) {
                case RED:
                    m_team = red[station];

                    final FragmentTeamStatusRedBinding redBinding = DataBindingUtil.inflate(
                            inflater,
                            R.layout.fragment_team_status_red,
                            container,
                            false);

                    v = redBinding.getRoot();

                    m_bandwidthText = redBinding.fieldMonitorBandwidth;
                    m_mpText = redBinding.fieldMonitorMissedPackets;
                    m_ttText = redBinding.fieldMonitorTripTime;
                    m_errorText = redBinding.fieldMonitorErrorText;
                    m_container = redBinding.fieldMonitorRow;

                    redBinding.setRowNumber(station);
                    redBinding.setTeam(m_team);
                    redBinding.setIsTeam(m_isHeader);
                    break;
                case BLUE:
                    m_team = blue[station];

                    final FragmentTeamStatusBlueBinding blueBinding = DataBindingUtil.inflate(
                            inflater,
                            R.layout.fragment_team_status_blue,
                            container,
                            false);

                    v = blueBinding.getRoot();

                    m_bandwidthText = blueBinding.fieldMonitorBandwidth;
                    m_mpText = blueBinding.fieldMonitorMissedPackets;
                    m_ttText = blueBinding.fieldMonitorTripTime;
                    m_errorText = blueBinding.fieldMonitorErrorText;
                    m_container = blueBinding.fieldMonitorRow;

                    blueBinding.setRowNumber(station);
                    blueBinding.setTeam(m_team);
                    blueBinding.setIsTeam(m_isHeader);
                    break;
                default:
                    throw new RuntimeException("Unknown Alliance type " + alliance);
            }
        } else {
            final FragmentTeamStatusBinding blueBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.fragment_team_status,
                    container,
                    false);

            v = blueBinding.getRoot();

            m_bandwidthText = blueBinding.fieldMonitorBandwidth;
            m_mpText = blueBinding.fieldMonitorMissedPackets;
            m_ttText = blueBinding.fieldMonitorTripTime;
            m_errorText = blueBinding.fieldMonitorErrorText;
            m_container = blueBinding.fieldMonitorRow;

            blueBinding.setIsTeam(m_isHeader);
        }


        return v;
    }

    private void setBackground(Drawable background, View v) {
        int pL = v.getPaddingLeft();
        int pR = v.getPaddingRight();
        int pT = v.getPaddingTop();
        int pB = v.getPaddingBottom();
        ViewGroup.LayoutParams params = v.getLayoutParams();
        v.setBackground(background);
        v.setPadding(pL, pT, pR, pB);
        v.setLayoutParams(params);
    }

    private class TeamObserver implements Observer<UpdateType> {
        @Override
        public void update(UpdateType updateType) {
            if (!m_team.isDsEth()) {
                animateError("Ethernet");
            } else if (!m_team.isDs()) {
                animateError("Driver Station");
            } else if (!m_team.isRadio()) {
                animateError("Radio");
            } else if (!m_team.isRio()) {
                animateError("RoboRIO");
            } else if (!m_team.isCode()) {
                animateError("Code");
            } else {
                removeError();
            }
        }

        private void animateError(final String text) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            TransitionManager.beginDelayedTransition(m_container, new Fade());
                        }
                        m_errorText.setVisibility(View.VISIBLE);
                        m_bandwidthText.setVisibility(View.INVISIBLE);
                        m_mpText.setVisibility(View.INVISIBLE);
                        m_ttText.setVisibility(View.INVISIBLE);
                        m_errorText.setText(text);
                    }
                });
            }
        }

        private void removeError() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            TransitionManager.beginDelayedTransition(m_container, new Fade());
                        }
                        m_errorText.setVisibility(View.INVISIBLE);
                        m_bandwidthText.setVisibility(View.VISIBLE);
                        m_mpText.setVisibility(View.VISIBLE);
                        m_ttText.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}