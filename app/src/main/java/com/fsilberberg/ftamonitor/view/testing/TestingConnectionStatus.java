package com.fsilberberg.ftamonitor.view.testing;


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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;

import butterknife.Bind;
import butterknife.ButterKnife;
import microsoft.aspnet.signalr.client.ConnectionState;

/**
 * Manages the testing framework for setting the connection status
 */
public class TestingConnectionStatus extends Fragment {

    private FieldConnectionService m_service;
    private boolean m_isBound = false;
    private final ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            m_isBound = true;
            m_service = ((FieldConnectionService.FCSBinder) service).getService();
            switch (m_service.getState()) {
                case Disconnected:
                    m_disconnected.setChecked(true);
                    break;
                case Connecting:
                    m_connecting.setChecked(true);
                    break;
                case Reconnecting:
                    m_reconnecting.setChecked(true);
                    break;
                case Connected:
                    m_connected.setChecked(true);
                    break;
            }
            m_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.disconnected_button:
                            m_service.getStatusObservable().setConnectionState(ConnectionState.Disconnected);
                            break;
                        case R.id.connecting_button:
                            m_service.getStatusObservable().setConnectionState(ConnectionState.Connecting);
                            break;
                        case R.id.reconnecting_button:
                            m_service.getStatusObservable().setConnectionState(ConnectionState.Reconnecting);
                            break;
                        case R.id.connected_button:
                            m_service.getStatusObservable().setConnectionState(ConnectionState.Connected);
                            break;
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                }
            });
            m_isBound = false;
            m_service = null;
        }
    };

    @Bind(R.id.connection_radio_group)
    protected RadioGroup m_radioGroup;
    @Bind(R.id.disconnected_button)
    protected RadioButton m_disconnected;
    @Bind(R.id.connecting_button)
    protected RadioButton m_connecting;
    @Bind(R.id.reconnecting_button)
    protected RadioButton m_reconnecting;
    @Bind(R.id.connected_button)
    protected RadioButton m_connected;

    public TestingConnectionStatus() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_testing_connection_status, container, false);
        ButterKnife.bind(this, mainView);


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(new Intent(getActivity(), FieldConnectionService.class),
                m_connection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (m_isBound) {
            getActivity().unbindService(m_connection);
            m_isBound = false;
            m_service = null;
        }
    }
}
