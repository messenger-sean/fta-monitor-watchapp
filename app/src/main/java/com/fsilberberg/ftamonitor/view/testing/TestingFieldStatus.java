package com.fsilberberg.ftamonitor.view.testing;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;

import butterknife.ButterKnife;
import butterknife.Bind;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingFieldStatus extends Fragment {

    public TestingFieldStatus() {
        // Required empty public constructor
    }

    @Bind(R.id.field_status_radio_group)
    protected RadioGroup m_group;
    @Bind(R.id.field_match_number)
    protected EditText m_matchTextBox;
    @Bind(R.id.field_match_play)
    protected EditText m_replayTextBox;
    @Bind(R.id.field_testing_not_started)
    protected RadioButton m_notReady;
    @Bind(R.id.field_testing_timeout)
    protected RadioButton m_timeout;
    @Bind(R.id.field_testing_ready_prestart)
    protected RadioButton m_readyPrestart;
    @Bind(R.id.field_testing_prestart_init)
    protected RadioButton m_prestartInit;
    @Bind(R.id.field_testing_prestart_comp)
    protected RadioButton m_prestartComp;
    @Bind(R.id.field_testing_match_ready)
    protected RadioButton m_ready;
    @Bind(R.id.field_testing_auto)
    protected RadioButton m_auto;
    @Bind(R.id.field_testing_teleop)
    protected RadioButton m_teleop;
    @Bind(R.id.field_testing_over)
    protected RadioButton m_over;
    @Bind(R.id.field_testing_aborted)
    protected RadioButton m_aborted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_testing_field_status, container, false);
        ButterKnife.bind(this, rootView);

        FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
        m_matchTextBox.setText(field.getMatchNumber());
        switch (field.getMatchStatus()) {
            case NOT_READY:
                m_notReady.setChecked(true);
                break;
            case TIMEOUT:
                m_timeout.setChecked(true);
                break;
            case READY_TO_PRESTART:
                m_readyPrestart.setChecked(true);
                break;
            case PRESTART_INITIATED:
                m_prestartInit.setChecked(true);
                break;
            case PRESTART_COMPLETED:
                m_prestartComp.setChecked(true);
                break;
            case MATCH_READY:
                m_ready.setChecked(true);
                break;
            case AUTO:
                m_auto.setChecked(true);
                break;
            case TELEOP:
                m_teleop.setChecked(true);
                break;
            case OVER:
                m_over.setChecked(true);
                break;
            case ABORTED:
                m_aborted.setChecked(true);
                break;
        }

        m_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
            }
        });

        m_matchTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && count != 0) {
                    FieldMonitorFactory.getInstance().getFieldStatus().setMatchNumber(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        m_replayTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && count != 0) {
                    FieldMonitorFactory.getInstance().getFieldStatus().setPlayNumber(Integer.parseInt(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return rootView;
    }


}
