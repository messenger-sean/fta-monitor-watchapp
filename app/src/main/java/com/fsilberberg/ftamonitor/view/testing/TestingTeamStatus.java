package com.fsilberberg.ftamonitor.view.testing;


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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamStatus;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
    @InjectView(R.id.team_testing_status)
    protected RadioGroup m_robotStatus;
    @InjectView(R.id.team_testing_number)
    protected EditText m_teamNum;
    @InjectView(R.id.team_testing_enabled)
    protected CheckBox m_enabled;
    @InjectView(R.id.team_testing_battery)
    protected EditText m_battery;
    @InjectView(R.id.team_testing_bandwidth)
    protected EditText m_bandwidth;
    @InjectView(R.id.team_testing_missed_packets)
    protected EditText m_mp;
    @InjectView(R.id.team_testing_round_trip)
    protected EditText m_rt;
    @InjectView(R.id.team_testing_sq)
    protected EditText m_sq;
    @InjectView(R.id.team_testing_ss)
    protected EditText m_ss;
    @InjectView(R.id.team_testing_dseth)
    protected RadioButton m_dsEth;
    @InjectView(R.id.team_testing_ds)
    protected RadioButton m_ds;
    @InjectView(R.id.team_testing_radio)
    protected RadioButton m_radio;
    @InjectView(R.id.team_testing_rio)
    protected RadioButton m_rio;
    @InjectView(R.id.team_testing_code)
    protected RadioButton m_code;
    @InjectView(R.id.team_testing_byp)
    protected RadioButton m_byp;
    @InjectView(R.id.team_testing_estop)
    protected RadioButton m_estop;
    @InjectView(R.id.team_testing_good)
    protected RadioButton m_good;

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
                m_status = field.getBlue1();
                break;
            case 2:
                m_status = field.getBlue2();
                break;
            case 3:
                m_status = field.getBlue3();
                break;
            case 4:
                m_status = field.getRed1();
                break;
            case 5:
                m_status = field.getRed2();
                break;
            case 6:
                m_status = field.getRed3();
                break;
            default:
                throw new RuntimeException("Given invalid team number " + team);
        }

        View rootView = inflater.inflate(R.layout.fragment_testing_team_status, container, false);
        ButterKnife.inject(this, rootView);

        if (!m_status.isDsEth()) {
            m_dsEth.setChecked(true);
        } else if (!m_status.isDs()) {
            m_ds.setChecked(true);
        } else if (!m_status.isRadio()) {
            m_radio.setChecked(true);
        } else if (!m_status.isRio()) {
            m_rio.setChecked(true);
        } else if (!m_status.isCode()) {
            m_code.setChecked(true);
        } else if (m_status.isEstop()) {
            m_estop.setChecked(true);
        } else if (m_status.isBypassed()) {
            m_byp.setChecked(true);
        } else {
            m_good.setChecked(true);
        }

        m_robotStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private void setStatus(boolean eth, boolean ds, boolean radio, boolean robot,
                                   boolean code, boolean bypassed, boolean estop) {
                m_status.setDsEth(eth);
                m_status.setDs(ds);
                m_status.setRadio(radio);
                m_status.setRobot(robot);
                m_status.setCode(code);
                m_status.setBypassed(bypassed);
                m_status.setEstop(estop);
            }

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.team_testing_dseth: // DS Eth
                        setStatus(false, false, false, false, false, false, false);
                        break;
                    case R.id.team_testing_ds: // DS
                        setStatus(true, false, false, false, false, false, false);
                        break;
                    case R.id.team_testing_radio: // Radio
                        setStatus(true, true, false, false, false, false, false);
                        break;
                    case R.id.team_testing_rio: // Rio
                        setStatus(true, true, true, false, false, false, false);
                        break;
                    case R.id.team_testing_code: // Code
                        setStatus(true, true, true, true, false, false, false);
                        break;
                    case R.id.team_testing_byp: // Bypassed
                        setStatus(true, true, true, true, true, true, false);
                        break;
                    case R.id.team_testing_estop: // Estop
                        setStatus(true, true, true, true, true, true, true);
                        break;
                    case R.id.team_testing_good: // Good
                        setStatus(true, true, true, true, true, false, false);
                        break;
                }
                m_status.updateObservers();
            }
        });


        m_teamNum.setText(Integer.toString(m_status.getTeamNumber()));
        m_teamNum.addTextChangedListener(new Watcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    int teamNum = Integer.parseInt(s.toString());
                    m_status.setTeamNumber(teamNum);
                    m_status.updateObservers();
                }
            }
        });

        m_enabled.setChecked(m_status.isEnabled());
        m_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_status.setEnabled(isChecked);
            }
        });

        m_battery.setText(Float.toString(m_status.getBattery()));
        m_battery.addTextChangedListener(
                new Watcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float battery = Float.parseFloat(s.toString());
                            m_status.setBattery(battery);
                            m_status.updateObservers();
                        }
                    }
                }
        );

        m_bandwidth.setText(Float.toString(m_status.getDataRate()));
        m_bandwidth.addTextChangedListener(
                new Watcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float dataRate = Float.parseFloat(s.toString());
                            m_status.setDataRate(dataRate);
                            m_status.updateObservers();
                        }
                    }
                }
        );

        m_mp.setText(Integer.toString(m_status.getDroppedPackets()));
        m_mp.addTextChangedListener(
                new Watcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            int missedPackets = Integer.parseInt(s.toString());
                            m_status.setDroppedPackets(missedPackets);
                            m_status.updateObservers();
                        }
                    }
                }
        );

        m_rt.setText(Integer.toString(m_status.getRoundTrip()));
        m_rt.addTextChangedListener(
                new Watcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            int rtt = Integer.parseInt(s.toString());
                            m_status.setRoundTrip(rtt);
                            m_status.updateObservers();
                        }
                    }
                }
        );

        m_sq.setText(Float.toString(m_status.getSignalQuality()));
        m_sq.addTextChangedListener(
                new Watcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float sq = Float.parseFloat(s.toString());
                            m_status.setSignalQuality(sq);
                            m_status.updateObservers();
                        }
                    }
                }
        );

        m_ss.setText(Float.toString(m_status.getSignalStrength()));
        m_ss.addTextChangedListener(
                new Watcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0 && count != 0) {
                            float ss = Float.parseFloat(s.toString());
                            m_status.setSignalStrength(ss);
                            m_status.updateObservers();
                        }
                    }
                }
        );

        return rootView;
    }

    /**
     * Simple abstract class to no-op the {before/after}TextChanged methods, to reduce code size
     */
    private abstract class Watcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
