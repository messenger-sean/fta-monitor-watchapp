package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;

import microsoft.aspnet.signalr.client.ConnectionState;

/**
 * This fragment is responsible for managing the placement and organization of all of the relevant
 * field status information.
 *
 * @author Fredric
 */
public class FieldMonitorFragment extends Fragment implements IObserver<ConnectionState>, Button.OnClickListener {

    private static final String CONNECTION_STRING = "FMS Connection Status:\n";

    private LinearLayout m_conLayout;
    private Button m_conButton;
    private TextView m_connectionView;
    private LinearLayout m_mainFieldView;
    private FrameLayout m_fieldView;
    private FrameLayout m_teamView;
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
        View fragView = inflater.inflate(R.layout.fragment_field_monitor, container, false);
        m_connectionView = (TextView) fragView.findViewById(R.id.con_status_text);
        m_mainFieldView = (LinearLayout) fragView.findViewById(R.id.field_monitor_fragment);
        m_fieldView = (FrameLayout) fragView.findViewById(R.id.field_status_fragment);
        m_teamView = (FrameLayout) fragView.findViewById(R.id.team_status_fragment);
        m_conLayout = (LinearLayout) fragView.findViewById(R.id.con_status_layout);
        m_conButton = (Button) fragView.findViewById(R.id.con_status_button);
        m_conButton.setOnClickListener(this);
        // Inflate the layout for this fragment
        return fragView;
    }

    @Override
    public void onResume() {
        super.onResume();
        update(FieldConnectionService.getState());
        FieldConnectionService.registerConnectionObserver(this);
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
                removeFragments();
                m_connectionView.setText(CONNECTION_STRING + updateType.toString());
                setMatchVisibility(false);
                setConnectedVisibility(true);
                break;
            case Connected:
                m_connectionView.setText(CONNECTION_STRING + updateType.toString());
                // Waits for half a second for a nice transition
                // TODO: When L features can be used, make this an animation
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setConnectedVisibility(false);
                        setMatchVisibility(true);
                        addFragments();
                    }
                }, 500);
                break;
        }
    }

    private void addFragments() {
        // Set up the fragments
        getFragmentManager().beginTransaction()
                .replace(R.id.field_status_fragment, new FieldStatusFragment())
                .replace(R.id.team_status_fragment, new TeamStatusTableFragment())
                .commit();
    }

    private void removeFragments() {
        // Set up the fragments
        getFragmentManager().beginTransaction()
                .replace(R.id.field_status_fragment, new BlankFragment())
                        //.replace(R.id.team_status_fragment, new BlankFragment())
                .commit();
    }

    public void setConnectedVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        m_conLayout.setVisibility(visibility);
        m_conButton.setVisibility(visibility);
        m_connectionView.setVisibility(visibility);
    }

    public void setMatchVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        m_mainFieldView.setVisibility(visibility);
        m_fieldView.setVisibility(visibility);
        m_teamView.setVisibility(visibility);
    }

    @Override
    public void update(final ConnectionState updateType) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                updateView(updateType);
                if (updateType == ConnectionState.Connected) {
                    m_conButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String url = preferences.getString(getString(R.string.fms_ip_addr_key), "10.0.100.5");
        Intent serviceIntent = new Intent(getActivity(), FieldConnectionService.class);
        serviceIntent.putExtra(FieldConnectionService.URL_INTENT_EXTRA, url);
        getActivity().startService(serviceIntent);
    }
}
