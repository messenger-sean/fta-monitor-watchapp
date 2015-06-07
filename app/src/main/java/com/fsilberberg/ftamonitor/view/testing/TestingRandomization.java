package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.services.RandomizationService;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Contains the settings for the randomization service
 */
public class TestingRandomization extends Fragment {

    @InjectView(R.id.randomize_enable)
    protected Switch m_enableRandom;
    @InjectView(R.id.randomize_field_con)
    protected Switch m_fieldCon;
    @InjectView(R.id.randomize_match_status)
    protected Switch m_matchStatus;
    @InjectView(R.id.randomize_robot_con)
    protected Switch m_robotCon;
    @InjectView(R.id.randomize_robot_vals)
    protected Switch m_robotVals;

    private SharedPreferences m_prefs;
    private String m_fieldKey;
    private String m_matchStatusKey;
    private String m_robotConKey;
    private String m_robotValsKey;

    private final ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RandomizationService.RandomizationBinder binder = (RandomizationService.RandomizationBinder) service;
            m_random = binder.getService();
            m_isBound = true;
            if (m_enableRandom.isChecked()) {
                m_random.setEnabled(true, getActivity());
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_enableRandom.setChecked(m_random.isStarted());
                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_isBound = false;
            m_random = null;
        }
    };

    private boolean m_isBound = false;
    private RandomizationService m_random;

    public TestingRandomization() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_testing_randomization, container, false);
        ButterKnife.inject(this, v);

        m_prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        m_fieldKey = getString(R.string.randomize_field_con_key);
        m_matchStatusKey = getString(R.string.randomize_match_status_key);
        m_robotConKey = getString(R.string.randomize_robot_con_key);
        m_robotValsKey = getString(R.string.randomize_robot_vals_key);

        m_enableRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (m_isBound) {
                    m_random.setEnabled(isChecked, getActivity());
                }
            }
        });
        m_fieldCon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (m_isBound) {
                    m_random.update();
                }
                m_prefs.edit().putBoolean(m_fieldKey, isChecked).apply();
            }
        });
        m_matchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (m_isBound) {
                    m_random.update();
                }
                m_prefs.edit().putBoolean(m_matchStatusKey, isChecked).apply();
            }
        });
        m_robotCon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (m_isBound) {
                    m_random.update();
                }
                m_prefs.edit().putBoolean(m_robotConKey, isChecked).apply();
            }
        });
        m_robotVals.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (m_isBound) {
                    m_random.update();
                }
                m_prefs.edit().putBoolean(m_robotValsKey, isChecked).apply();
            }
        });

        m_fieldCon.setChecked(m_prefs.getBoolean(m_fieldKey, false));
        m_matchStatus.setChecked(m_prefs.getBoolean(m_matchStatusKey, false));
        m_robotCon.setChecked(m_prefs.getBoolean(m_robotConKey, false));
        m_robotVals.setChecked(m_prefs.getBoolean(m_robotValsKey, false));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(new Intent(getActivity(), RandomizationService.class),
                m_connection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (m_isBound) {
            getActivity().unbindService(m_connection);
        }
    }
}
