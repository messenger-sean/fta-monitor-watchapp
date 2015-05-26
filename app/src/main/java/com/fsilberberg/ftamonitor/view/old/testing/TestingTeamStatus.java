package com.fsilberberg.ftamonitor.view.old.testing;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingTeamStatus extends Fragment {

    private static final String TEAM_NUMBER_ARG = "team_number_arg";

    public static TestingTeamStatus makeInstance(int team) {
        Bundle bundle = new Bundle();
        bundle.putInt(TEAM_NUMBER_ARG, team);
        TestingTeamStatus tts = new TestingTeamStatus();
        tts.setArguments(bundle);

        return tts;
    }

    private TeamStatus m_status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            throw new RuntimeException("Arguments to team testing status was null");
        }

        final int team = args.getInt(TEAM_NUMBER_ARG);
        FieldStatus field = FieldMonitorFactory.getInstance().getFieldStatus();
        switch (team) {
            case 1:
                m_status = field.getRed1();
                break;
            case 2:
                m_status = field.getRed2();
                break;
            case 3:
                m_status = field.getRed3();
                break;
            case 4:
                m_status = field.getBlue1();
                break;
            case 5:
                m_status = field.getBlue2();
                break;
            case 6:
                m_status = field.getBlue3();
                break;
            default:
                throw new RuntimeException("Given invalid team number " + team);
        }

        View rootView = inflater.inflate(R.layout.fragment_testing_team_status, container, false);

        RadioGroup robotStatus = (RadioGroup) rootView.findViewById(R.id.team_testing_status);
        robotStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.team_testing_dseth: // DS Eth
                        m_status.setDsEth(false);
                        m_status.setDs(false);
                        m_status.setRadio(false);
                        m_status.setRobot(false);
                        m_status.setCode(false);
                        m_status.setEstop(false);
                        break;
                    case R.id.team_testing_ds: // DS
                        m_status.setDsEth(true);
                        m_status.setDs(false);
                        m_status.setRadio(false);
                        m_status.setRobot(false);
                        m_status.setCode(false);
                        m_status.setEstop(false);
                        break;
                    case R.id.team_testing_radio: // Radio
                        m_status.setDsEth(true);
                        m_status.setDs(true);
                        m_status.setRadio(false);
                        m_status.setRobot(false);
                        m_status.setCode(false);
                        m_status.setEstop(false);
                        break;
                    case R.id.team_testing_rio: // Rio
                        m_status.setDsEth(true);
                        m_status.setDs(true);
                        m_status.setRadio(true);
                        m_status.setRobot(false);
                        m_status.setCode(false);
                        m_status.setEstop(false);
                        break;
                    case R.id.team_testing_code: // Code
                        m_status.setDsEth(true);
                        m_status.setDs(true);
                        m_status.setRadio(true);
                        m_status.setRobot(true);
                        m_status.setCode(false);
                        m_status.setEstop(false);
                        break;
                    case R.id.team_testing_estop: // Estop
                        m_status.setDsEth(true);
                        m_status.setDs(true);
                        m_status.setRadio(true);
                        m_status.setRobot(true);
                        m_status.setCode(true);
                        m_status.setEstop(true);
                        break;
                    case R.id.team_testing_good: // Good
                        m_status.setDsEth(true);
                        m_status.setDs(true);
                        m_status.setRadio(true);
                        m_status.setRobot(true);
                        m_status.setCode(true);
                        m_status.setEstop(false);
                        break;
                }
                m_status.updateObservers();
            }
        });

        EditText edit = (EditText) rootView.findViewById(R.id.team_testing_number);
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    int teamNum = Integer.parseInt(s.toString());
                    m_status.setTeamNumber(teamNum);
                    m_status.updateObservers();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.team_testing_enabled);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_status.setEnabled(isChecked);
            }
        });

        ((EditText) rootView.findViewById(R.id.team_testing_battery)).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float battery = Float.parseFloat(s.toString());
                            m_status.setBattery(battery);
                            m_status.updateObservers();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        ((EditText) rootView.findViewById(R.id.team_testing_bandwidth)).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float dataRate = Float.parseFloat(s.toString());
                            m_status.setDataRate(dataRate);
                            m_status.updateObservers();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        ((EditText) rootView.findViewById(R.id.team_testing_missed_packets)).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            int missedPackets = Integer.parseInt(s.toString());
                            m_status.setDroppedPackets(missedPackets);
                            m_status.updateObservers();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        ((EditText) rootView.findViewById(R.id.team_testing_round_trip)).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            int rtt = Integer.parseInt(s.toString());
                            m_status.setRoundTrip(rtt);
                            m_status.updateObservers();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        ((EditText) rootView.findViewById(R.id.team_testing_sq)).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float sq = Float.parseFloat(s.toString());
                            m_status.setSignalQuality(sq);
                            m_status.updateObservers();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        ((EditText) rootView.findViewById(R.id.team_testing_ss)).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float ss = Float.parseFloat(s.toString());
                            m_status.setSignalStrength(ss);
                            m_status.updateObservers();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        return rootView;
    }


}
