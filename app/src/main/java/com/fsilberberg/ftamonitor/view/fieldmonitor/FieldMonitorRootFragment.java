package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;

import butterknife.ButterKnife;
import butterknife.InjectView;
import microsoft.aspnet.signalr.client.ConnectionState;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldMonitorRootFragment extends Fragment {

    private String m_conPrefix;

    private ServiceConnection m_signalrCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final FieldConnectionService fcs = ((FieldConnectionService.FCSBinder) service).getService();
            m_conState = fcs.getStatusObservable();
            m_observer = new FieldMonitorSignalrObserver();
            m_conState.registerObserver(m_observer);
            m_isBound = true;
            m_observer.update(fcs.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_isBound = false;
            m_conState.unregisterObserver(m_observer);
            m_conState = null;
            m_observer = null;
        }
    };
    private boolean m_isBound;
    private FieldConnectionService.ConnectionStateObservable m_conState;
    private FieldMonitorSignalrObserver m_observer;

    @InjectView(R.id.field_monitor_signalr_status)
    protected TextView m_fieldMonitorStatus;
    @InjectView(R.id.field_monitor_retry_signalr)
    protected Button m_retryButton;
    @InjectView(R.id.field_monitor_signalr_layout)
    protected LinearLayout m_signalrLayout;

    public FieldMonitorRootFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_field_monitor_root, container, false);
        ButterKnife.inject(this, view);
        m_conPrefix = getActivity().getString(R.string.field_monitor_connection_prefix);
        m_retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FieldConnectionService.start(true);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_isBound) {
            m_conState.registerObserver(m_observer);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (m_isBound) {
            m_conState.unregisterObserver(m_observer);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(
                new Intent(getActivity(), FieldConnectionService.class),
                m_signalrCon,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (m_isBound) {
            getActivity().unbindService(m_signalrCon);
        }
    }

    private class FieldMonitorSignalrObserver implements Observer<ConnectionState> {
        @Override
        public void update(final ConnectionState updateType) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_fieldMonitorStatus.setText(m_conPrefix + "\n" + updateType.toString());
                    }
                });
                switch (updateType) {
                    case Connecting:
                    case Disconnected:
                    case Reconnecting:
                        // TODO: Remove other fragments
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_signalrLayout.setVisibility(View.VISIBLE);
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.field_monitor_fragment, new BlankFragment())
                                        .commit();
                            }
                        });
                        break;
                    case Connected:
                        View v = getView();
                        if (v != null) {
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    m_signalrLayout.setVisibility(View.INVISIBLE);
                                    getFragmentManager().beginTransaction()
                                            .replace(R.id.field_monitor_fragment, new FieldMonitorStatusFragment())
                                            .commit();
                                }
                            }, 500);
                        }
                        break;
                }
            }
        }
    }
}
