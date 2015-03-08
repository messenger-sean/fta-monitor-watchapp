package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fsilberberg.ftamonitor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingTeamStatus extends Fragment {

    private static final String TEAM_NUMBER_ARG = "team_number_arg";

    public static TestingTeamStatus makeInstance(int team) {
        Bundle bundle = new Bundle();
        bundle.putInt(TEAM_NUMBER_ARG, team);
        TestingTeamStatus tts = new TestingTeamStatus();
        tts.setArguments(bundle);

        return tts;
    }

    public TestingTeamStatus() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testing_team_status, container, false);
    }


}
