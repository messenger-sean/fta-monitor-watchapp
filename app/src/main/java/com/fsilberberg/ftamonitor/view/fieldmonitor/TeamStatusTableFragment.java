package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

import static com.fsilberberg.ftamonitor.common.Alliance.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeamStatusTableFragment extends Fragment {


    public TeamStatusTableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_team_status_table, container, false);
        getFragmentManager().beginTransaction()
                .replace(R.id.team_status_row_blue1, TeamStatusFragment.newInstance(1, BLUE))
                .replace(R.id.team_status_row_blue2, TeamStatusFragment.newInstance(2, BLUE))
                .replace(R.id.team_status_row_blue3, TeamStatusFragment.newInstance(3, BLUE))
                .replace(R.id.team_status_row_red1, TeamStatusFragment.newInstance(1, RED))
                .replace(R.id.team_status_row_red2, TeamStatusFragment.newInstance(2, RED))
                .replace(R.id.team_status_row_red3, TeamStatusFragment.newInstance(3, RED))
                .commit();
        return fragView;
    }


}
