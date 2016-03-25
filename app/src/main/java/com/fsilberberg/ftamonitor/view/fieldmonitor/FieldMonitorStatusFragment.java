package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Alliance;
import com.fsilberberg.ftamonitor.common.Station;
import com.fsilberberg.ftamonitor.databinding.FragmentFieldMonitorStatusBinding;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldMonitorStatusFragment extends Fragment {

    private final FieldStatus m_field = FieldMonitorFactory.getInstance().getFieldStatus();
    private final FieldStatusObserver m_observer = new FieldStatusObserver();
    protected TextView m_matchStatus;

    public FieldMonitorStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentFieldMonitorStatusBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_field_monitor_status,
                container,
                false
        );
        binding.setField(m_field);

        m_matchStatus = binding.fieldMonitorMatchStatus;

        // Insert all of the team status fragments
        getFragmentManager().beginTransaction()
                .replace(R.id.field_monitor_blue1, TeamStatusFragment.makeInstance(Alliance.BLUE, Station.STATION1))
                .replace(R.id.field_monitor_blue2, TeamStatusFragment.makeInstance(Alliance.BLUE, Station.STATION2))
                .replace(R.id.field_monitor_blue3, TeamStatusFragment.makeInstance(Alliance.BLUE, Station.STATION3))
                .replace(R.id.field_monitor_red1, TeamStatusFragment.makeInstance(Alliance.RED, Station.STATION1))
                .replace(R.id.field_monitor_red2, TeamStatusFragment.makeInstance(Alliance.RED, Station.STATION2))
                .replace(R.id.field_monitor_red3, TeamStatusFragment.makeInstance(Alliance.RED, Station.STATION3))
                .commit();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        m_field.addOnPropertyChangedCallback(m_observer);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_field.removeOnPropertyChangedCallback(m_observer);
    }

    private class FieldStatusObserver extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_matchStatus.setText(m_field.getMatchStatus().toString());
                    }
                });
            }
        }
    }
}
