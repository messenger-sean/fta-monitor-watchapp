package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldUpdateType;

import java.lang.reflect.Field;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldStatusFragment extends Fragment implements IObserver<FieldUpdateType> {

    private TextView m_matchNumberView;
    private TextView m_fieldStatusView;
    private TextView m_timeView;

    public FieldStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_field_status, container, false);
        m_matchNumberView = (TextView) fragView.findViewById(R.id.field_status_match_number);
        m_fieldStatusView = (TextView) fragView.findViewById(R.id.field_status_text);
        m_timeView = (TextView) fragView.findViewById(R.id.field_status_time);
        return fragView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update all of the field elements on resume of the application
        for (FieldUpdateType type : FieldUpdateType.values()) {
            updateFieldElement(type);
        }

        FieldMonitorFactory.getInstance().getFieldStatus().registerObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        FieldMonitorFactory.getInstance().getFieldStatus().deregisterObserver(this);
    }

    @Override
    public void update(final FieldUpdateType updateType) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                updateFieldElement(updateType);
            }
        });
    }

    private void updateFieldElement(FieldUpdateType type) {
        FieldStatus fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        switch (type) {
            case MATCH_NUMBER:
                m_matchNumberView.setText(fieldStatus.getMatchNumber());
                break;
            case MATCH_STATUS:
                m_fieldStatusView.setText(fieldStatus.getMatchStatus().toString());
                break;
            case TELEOP_TIME:
            case AUTO_TIME:
                // These are ignored here. They are caught in the main Field Status, and the defaults
                // are updated. The defaults are loaded later when we want to start the timer
                break;
        }
    }
}
