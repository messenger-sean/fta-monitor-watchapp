package com.fsilberberg.ftamonitor.view.testing;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fsilberberg.ftamonitor.R;

/**
 * Contains the settings for the randomization service
 */
public class TestingRandomization extends Fragment {


    public TestingRandomization() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testing_randomization, container, false);
    }


}
