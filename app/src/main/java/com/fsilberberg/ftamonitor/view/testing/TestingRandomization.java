package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.services.RandomizationService;

/**
 * Contains the settings for the randomization service
 */
public class TestingRandomization extends Fragment {

    @InjectView(R.id.randomize_enable)
    protected CheckBox m_enableRandom;
    @InjectView(R.id.randomize_field_con)
    protected CheckBox m_fieldCon;
    @InjectView(R.id.randomize_robot_con)
    protected CheckBox m_robotCon;
    @InjectView(R.id.randomize_robot_vals)
    protected CheckBox m_robotVals;

    private SharedPreferences m_prefs;
    private String m_enableKey;
    private String m_fieldKey;
    private String m_robotConKey;
    private String m_robotValsKey;

    private final ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RandomizationService.RandomizationBinder binder = (RandomizationService.RandomizationBinder) service;
            m_random = binder.getService();
            m_isBound = true;
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
        m_enableKey = getString(R.string.randomize_enable_key);
        m_fieldKey = getString(R.string.randomize_field_con_key);
        m_robotConKey = getString(R.string.randomize_robot_con_key);
        m_robotValsKey = getString(R.string.randomize_robot_vals_key);

        m_enableRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getActivity(), "Enable: " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });
        m_fieldCon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getActivity(), "Field: " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });
        m_robotCon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getActivity(), "Robot Con: " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });
        m_robotVals.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getActivity(), "Robot Val: " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        m_enableRandom.setChecked(m_prefs.getBoolean(m_enableKey, false));
        m_fieldCon.setChecked(m_prefs.getBoolean(m_fieldKey, false));
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
