package com.fsilberberg.ftamonitor.view.testing;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;

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
        View rootView = inflater.inflate(R.layout.fragment_testing_field_status, container, false);

        final RadioGroup group = (RadioGroup) rootView.findViewById(R.id.field_status_radio_group);
        final EditText matchTextBox = (EditText) rootView.findViewById(R.id.field_match_number);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MatchStatus status;
                switch (checkedId) {
                    case R.id.field_testing_not_started:
                        status = MatchStatus.NOT_READY;
                        break;
                    case R.id.field_testing_timeout:
                        status = MatchStatus.TIMEOUT;
                        break;
                    case R.id.field_testing_ready_prestart:
                        status = MatchStatus.READY_TO_PRESTART;
                        break;
                    case R.id.field_testing_prestart_init:
                        status = MatchStatus.PRESTART_INITIATED;
                        break;
                    case R.id.field_testing_prestart_comp:
                        status = MatchStatus.PRESTART_COMPLETED;
                        break;
                    case R.id.field_testing_match_ready:
                        status = MatchStatus.MATCH_READY;
                        break;
                    case R.id.field_testing_auto:
                        status = MatchStatus.AUTO;
                        break;
                    case R.id.field_testing_teleop:
                        status = MatchStatus.TELEOP;
                        break;
                    case R.id.field_testing_over:
                        status = MatchStatus.OVER;
                        break;
                    case R.id.field_testing_aborted:
                        status = MatchStatus.ABORTED;
                        break;
                    default:
                        throw new RuntimeException("Encountered unknown field status radio button");
                }

                FieldMonitorFactory.getInstance().getFieldStatus().setMatchStatus(status);
                FieldMonitorFactory.getInstance().getFieldStatus().updateObservers();
            }
        });

        matchTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && count != 0) {
                    FieldMonitorFactory.getInstance().getFieldStatus().setMatchNumber(s.toString());
                    FieldMonitorFactory.getInstance().getFieldStatus().updateObservers();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return rootView;
    }


}
