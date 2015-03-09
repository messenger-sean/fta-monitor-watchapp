package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingFieldStatus extends Fragment {

    private MatchStatus m_curStatus = MatchStatus.NOT_READY;
    private String m_curNumber = "P1";

    public TestingFieldStatus() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_testing_field_status, container, false);

        final RadioGroup group = (RadioGroup) rootView.findViewById(R.id.field_status_radio_group);
        final TextView matchTextBox = (TextView) rootView.findViewById(R.id.field_match_number_text);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                m_curStatus = MatchStatus.values()[(checkedId - 1) % 10];
                FieldMonitorFactory.getInstance().getFieldStatus().setMatchStatus(m_curStatus);
            }
        });

        matchTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                m_curNumber = s.toString();
                FieldMonitorFactory.getInstance().getFieldStatus().setMatchNumber(m_curNumber);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return rootView;
    }


}
