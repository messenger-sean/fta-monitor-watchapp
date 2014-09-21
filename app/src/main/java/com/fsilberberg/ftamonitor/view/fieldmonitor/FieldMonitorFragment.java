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
    // If we need to set up the fragments or if it's been done
    private boolean m_updateFragments = false;

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

        // Set up the fragments
        getFragmentManager().beginTransaction()
                .replace(R.id.field_status_fragment, new FieldStatusFragment())
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        FieldConnectionService.deregisterConnectionObserver(this);
    }

    /**
     * Updates the view based on the new status. This must be called from the UI thread, otherwise
     * an exception will be thrown
     *
     * @param updateType
     */
    private void updateView(final ConnectionState updateType) {
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
                // Waits for half a second for a nice transition
                // TODO: When L features can be used, make this an animation
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        m_connectionView.setVisibility(View.INVISIBLE);
                        m_fieldView.setVisibility(View.VISIBLE);
                    }
                }, 500);
                break;
        }
    }

    @Override
    public void update(final ConnectionState updateType) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                updateView(updateType);
            }
        });
    }
}
