package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldConnectionService;

import microsoft.aspnet.signalr.client.ConnectionState;

/**
 * This fragment is responsible for managing the placement and organization of all of the relevant
 * field status information.
 *
 * @author Fredric
 */
public class FieldMonitorFragment extends Fragment implements IObserver<ConnectionState> {

    private static final String CONNECTION_STRING = "FMS Connection Status:\n";

    private TextView m_connectionView;
    private LinearLayout m_fieldView;

    public FieldMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_field_monitor, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        m_connectionView = (TextView) getActivity().findViewById(R.id.con_status_text);
        m_fieldView = (LinearLayout) getActivity().findViewById(R.id.field_monitor_fragment);
        update(FieldConnectionService.getState());
        FieldConnectionService.registerConnectionObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        FieldConnectionService.deregisterConnectionObserver(this);
    }

    public void updateView(final ConnectionState updateType) {
        switch (updateType) {
            case Disconnected:
            case Connecting:
            case Reconnecting:
                m_connectionView.setText(CONNECTION_STRING + updateType.toString());
                m_connectionView.setVisibility(View.INVISIBLE);
                m_connectionView.setVisibility(View.VISIBLE);
                break;
            case Connected:
                m_connectionView.setText(CONNECTION_STRING + updateType.toString());
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        m_connectionView.setVisibility(View.INVISIBLE);
                        m_fieldView.setVisibility(View.VISIBLE);
                    }
                }, 500);
                break;
        }
        Log.d(FieldMonitorFragment.class.getName(), "View updated");
    }

    @Override
    public void update(final ConnectionState updateType) {
        Log.d(FieldMonitorFragment.class.getName(), "Scheduling update");
        getView().post(new Runnable() {
            @Override
            public void run() {
                Log.d(FieldMonitorFragment.class.getName(), "Updating the view");
                updateView(updateType);
            }
        });
    }
}
