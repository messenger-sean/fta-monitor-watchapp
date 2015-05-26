package com.fsilberberg.ftamonitor.view.old.fieldmonitor;

import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;
import microsoft.aspnet.signalr.client.ConnectionState;

/**
 * This fragment is responsible for managing the placement and organization of all of the relevant
 * field status information.
 *
 * @author Fredric
 */
public class FieldMonitorFragment extends Fragment implements Observer<ConnectionState>, Button.OnClickListener {

    private static final String CONNECTION_STRING = "FMS Connection Status:\n";

    private LinearLayout m_conLayout;
    private Button m_conButton;
    private TextView m_connectionView;
    private LinearLayout m_mainFieldView;
    private FrameLayout m_fieldView;
    private FrameLayout m_teamView;
    private FieldConnectionService m_service;
    private boolean m_isBound = false;

    private ServiceConnection m_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FieldConnectionService.FCSBinder binder = (FieldConnectionService.FCSBinder) service;
            m_service = binder.getService();
            m_isBound = true;
            m_service.registerObserver(FieldMonitorFragment.this);
            update(m_service.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_isBound = false;
            m_service.deregisterObserver(FieldMonitorFragment.this);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Intent fcsIntent = new Intent(getActivity(), FieldConnectionService.class);
        getActivity().bindService(fcsIntent, m_connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(m_connection);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        if (m_service != null) {
            m_service.registerObserver(this);
            update(m_service.getState());
        }
        setupLockScreen();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_field_monitor));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (m_service != null) {
            m_service.deregisterObserver(this);
        }
    }

    /**
     * Updates the view based on the new status. This must be called from the UI thread, otherwise
     * an exception will be thrown
     *
     * @param updateType The new connection state to show
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
        Log.d(FieldMonitorFragment.class.getName(), "Updated ConnectionState with " + updateType);
        getView().post(new Runnable() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void run() {
                updateView(updateType);
                if (updateType == ConnectionState.Connected) {
                    setupLockScreen();
                    m_conButton.setVisibility(View.INVISIBLE);
                } else {
                    removeLockScreen();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        FieldConnectionService.start();
    }

    private void setupLockScreen() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lockKey = getString(R.string.lock_screen_display_key);
        if (prefs.getBoolean(lockKey, false)
                && m_isBound
                && m_service.getState().equals(ConnectionState.Connected)) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            // Make absolutely sure not to display in these cases
            removeLockScreen();
        }
    }

    private void removeLockScreen() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }
}
