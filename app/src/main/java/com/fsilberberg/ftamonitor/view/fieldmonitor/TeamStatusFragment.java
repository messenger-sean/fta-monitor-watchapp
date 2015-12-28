package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.databinding.FragmentTeamStatusBinding;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
    private TextView m_errorText;
    private ViewGroup m_root;
    private Collection<View> m_errorVisibleElements = new ArrayList<>();
    private Collection<View> m_errorInvisibleElements = new ArrayList<>();

    public TeamStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        m_team.addOnPropertyChangedCallback(m_observer);
        m_observer.onPropertyChanged(m_team, BR.dsEth);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_team.removeOnPropertyChangedCallback(m_observer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentTeamStatusBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_team_status,
                container,
                false);

        if (getArguments() == null) {
            throw new RuntimeException("Null arguments passed to team status fragment! " +
                    "Use the makeInstance factory to make new instances of team status fragment.");
        }

        Bundle args = getArguments();
        FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
        Alliance alliance = Alliance.values()[args.getInt(ALLIANCE_ARG)];
        Station station = Station.values()[args.getInt(STATION_ARG)];
        TeamStatus[] red = new TeamStatus[]{
                field.getRed1(), field.getRed2(), field.getRed3()
        };
        TeamStatus[] blue = new TeamStatus[]{
                field.getBlue1(), field.getBlue2(), field.getBlue3()
        };

        switch (alliance) {
            case RED:
                m_team = red[station.getStationNumber() - 1];
                break;
            case BLUE:
                m_team = blue[station.getStationNumber() - 1];
                break;
            default:
                throw new RuntimeException("Unknown Alliance type " + alliance);
        }

        binding.setAlliance(alliance);
        binding.setRowText(String.format("%s %d", alliance.getPrettyName(), station.getStationNumber()));
        binding.setTeam(m_team);

        m_root = binding.rootLayout;
        m_errorText = binding.robotStatus;

        m_errorInvisibleElements.addAll(Arrays.asList(
                binding.bandwidth, binding.bandwidthLabel,
                binding.roundTrip, binding.roundTripLabel,
                binding.missedPackets, binding.missedPacketsLabel,
                binding.signalQuality, binding.signalQualityLabel,
                binding.signalStrength, binding.signalStrengthLabel
        ));

        m_errorVisibleElements.addAll(Arrays.asList(binding.robotStatus, binding.robotStatusLabel));

        return binding.getRoot();
    }

    private class TeamObserver extends Observable.OnPropertyChangedCallback {
        private final Collection<Integer> updateTypes = Arrays.asList(BR.dsEth, BR.ds, BR.radio,
                BR.rio, BR.code, BR.bypassed, BR.estop);

        private void animateError(final String text) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            TransitionManager.beginDelayedTransition(m_root, new Fade());
                        }
                        for (View v : m_errorVisibleElements) {
                            v.setVisibility(View.VISIBLE);
                        }
                        for (View v : m_errorInvisibleElements) {
                            v.setVisibility(View.INVISIBLE);
                        }
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
                            TransitionManager.beginDelayedTransition(m_root, new Fade());
                        }
                        for (View v : m_errorInvisibleElements) {
                            v.setVisibility(View.VISIBLE);
                        }
                        for (View v : m_errorVisibleElements) {
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }

        @Override
        public void onPropertyChanged(Observable observable, int propertyChanged) {
            if (!updateTypes.contains(propertyChanged)) return;
            if (!m_team.isDsEth()) {
                animateError("No DS Ethernet");
            } else if (!m_team.isDs()) {
                animateError("No Driver Station");
            } else if (!m_team.isRadio()) {
                animateError("No Robot Radio");
            } else if (!m_team.isRio()) {
                animateError("No RoboRIO");
            } else if (!m_team.isCode()) {
                animateError("No Code");
            } else if (m_team.isEstop()) {
                animateError("Team is E-Stopped");
            } else if (m_team.isBypassed()) {
                animateError("Team is Bypassed");
            } else {
                removeError();
            }
        }
    }
}