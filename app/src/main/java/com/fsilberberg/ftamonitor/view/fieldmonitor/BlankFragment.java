package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fsilberberg.ftamonitor.R;

/**
 * This is just a blank fragment for replacing the Field Monitor sections when we are disconnected
 *
 */
public class BlankFragment extends Fragment {


    public BlankFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }


}
