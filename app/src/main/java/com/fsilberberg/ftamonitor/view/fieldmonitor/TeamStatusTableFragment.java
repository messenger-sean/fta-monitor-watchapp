package com.fsilberberg.ftamonitor.view.fieldmonitor;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fsilberberg.ftamonitor.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class TeamStatusTableFragment extends Fragment {


    public TeamStatusTableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_team_status_table_fragment, container, false);
        getFragmentManager().beginTransaction().replace(R.id.team_status_table_red1, new TeamStatusFragment()).commit();
        return fragView;
    }


}
