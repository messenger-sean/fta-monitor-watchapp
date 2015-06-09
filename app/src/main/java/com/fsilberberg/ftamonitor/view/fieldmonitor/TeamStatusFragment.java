package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
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

        final FragmentTeamStatusBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_team_status,
                container,
                false);

        m_bandwidthText = binding.fieldMonitorBandwidth;
        m_mpText = binding.fieldMonitorMissedPackets;
        m_ttText = binding.fieldMonitorTripTime;
        m_errorText = binding.fieldMonitorErrorText;
        m_container = binding.fieldMonitorRow;

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

            Drawable background;

            switch (alliance) {
                case RED:
                    m_team = red[station];
                    background = ResourcesCompat.getDrawable(getResources(), R.drawable.team_status_background_red, null);
                    break;
                case BLUE:
                    m_team = blue[station];
                    background = ResourcesCompat.getDrawable(getResources(), R.drawable.team_status_background_blue, null);
                    break;
                default:
                    throw new RuntimeException("Unknown Alliance type " + alliance);
            }

            binding.setTeamBackground(background);
            binding.setRowNumber(station);
            binding.setTeam(m_team);
        }

        binding.setIsTeam(m_isHeader);

        return binding.getRoot();
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