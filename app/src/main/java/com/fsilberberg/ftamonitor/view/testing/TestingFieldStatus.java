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
public class TestingFieldStatus extends Fragment {


    public TestingFieldStatus() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testing_field_status, container, false);
    }


}
