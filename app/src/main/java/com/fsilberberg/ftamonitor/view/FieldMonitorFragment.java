package com.fsilberberg.ftamonitor.view;



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
public class FieldMonitorFragment extends Fragment {

    public FieldMonitorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_field_monitor, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(getString(R.string.action_field_monitor));
    }
}
