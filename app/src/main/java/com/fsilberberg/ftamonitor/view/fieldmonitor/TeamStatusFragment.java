package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.databinding.FragmentTeamStatusBinding;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

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

    TeamStatus m_team;

    public TeamStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() == null) {
            throw new RuntimeException("No args given to TeamStatusFragment. Use the makeInstance method.");
        }

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

        FragmentTeamStatusBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_team_status,
                container,
                false);

        switch (alliance) {
            case RED:
                m_team = red[station];
                binding.fieldMonitorRow.setBackground(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.team_status_background_red, null)
                );
                break;
            case BLUE:
                m_team = blue[station];
                binding.fieldMonitorRow.setBackground(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.team_status_background_blue, null)
                );
                break;
        }

        binding.fieldMonitorRow.setPadding(0, 0, 0, 0);

        binding.setTeam(m_team);
        binding.fieldMonitorRowNumber.setText(Integer.toString(station + 1));

        return binding.getRoot();
    }
}