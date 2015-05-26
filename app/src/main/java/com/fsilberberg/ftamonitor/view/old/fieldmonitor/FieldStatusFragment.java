package com.fsilberberg.ftamonitor.view.old.fieldmonitor;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.UpdateType;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldStatusFragment extends Fragment implements Observer<UpdateType> {

    private TextView m_fieldStatusView;

    public FieldStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_field_status, container, false);
        m_fieldStatusView = (TextView) fragView.findViewById(R.id.field_status_text);
        return fragView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update all of the field elements on resume of the application
        updateField();
        FieldMonitorFactory.getInstance().getFieldStatus().registerObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        FieldMonitorFactory.getInstance().getFieldStatus().unregisterObserver(this);
    }

    @Override
    public void update(final UpdateType updateType) {
        if (updateType == UpdateType.TEAM)
            getView().post(new Runnable() {
                @Override
                public void run() {
                    updateField();
                }
            });
    }

    /**
     * Updates the display elements. It will fetch the current value from the field status
     */
    private void updateField() {
        FieldStatus fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        MatchStatus matchStatus = fieldStatus.getMatchStatus();
        m_fieldStatusView.setText(matchStatus.toString());
    }
}
