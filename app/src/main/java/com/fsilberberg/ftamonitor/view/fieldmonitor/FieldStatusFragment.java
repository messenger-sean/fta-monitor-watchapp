package com.fsilberberg.ftamonitor.view.fieldmonitor;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldUpdateType;
import com.fsilberberg.ftamonitor.services.FieldTimeService;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldStatusFragment extends Fragment implements IObserver<FieldUpdateType> {

    private TextView m_matchNumberView;
    private TextView m_fieldStatusView;
    private TextView m_timeView;

    // Timer fields
    private MatchTimer m_matchTimer;

    public FieldStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_field_status, container, false);
        m_matchNumberView = (TextView) fragView.findViewById(R.id.field_status_match_number);
        m_fieldStatusView = (TextView) fragView.findViewById(R.id.field_status_text);
        m_timeView = (TextView) fragView.findViewById(R.id.field_status_time);
        return fragView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update all of the field elements on resume of the application
        updateFieldElement(FieldUpdateType.values());
        FieldMonitorFactory.getInstance().getFieldStatus().registerObserver(this);
        if (FieldTimeService.isTimerRunning()) {
            startTimer(FieldTimeService.getTimeRemaining());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FieldMonitorFactory.getInstance().getFieldStatus().deregisterObserver(this);
        if (m_matchTimer != null) {
            m_matchTimer.cancel();
        }
    }

    @Override
    public void update(final FieldUpdateType updateType) {
        getView().post(new Runnable() {
            @Override
            public void run() {
                updateFieldElement(updateType);
            }
        });
    }

    /**
     * Updates one of the display elements based on the given type. It will fetch the current value
     * from the field status
     *
     * @param types The field elements to update
     */
    private void updateFieldElement(FieldUpdateType... types) {
        FieldStatus fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
        for (FieldUpdateType type : types) {
            switch (type) {
                case MATCH_NUMBER:
                    m_matchNumberView.setText(fieldStatus.getMatchNumber());
                    break;
                case MATCH_STATUS:
                    MatchStatus matchStatus = fieldStatus.getMatchStatus();
                    m_fieldStatusView.setText(matchStatus.toString());
                    updateTimer(matchStatus);
                    break;
                case TELEOP_TIME:
                case AUTO_TIME:
                    // These are ignored here. They are caught in the main Field Status, and the defaults
                    // are updated. The defaults are loaded later when we want to start the timer
                    break;
            }
        }
    }

    /**
     * Takes a match status and determines if a timer should be started, what value it should have,
     * and starts it if necessary.
     *
     * @param matchStatus the new status for timing
     */
    private void updateTimer(MatchStatus matchStatus) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (matchStatus) {
            case AUTO:
                int autoTime = Integer.valueOf(prefs.getString(getString(R.string.auto_time_key), "0"));
                startTimer(autoTime);
                break;
            case TELEOP:
                int teleopTime = Integer.valueOf(prefs.getString(getString(R.string.teleop_time_key), "0"));
                startTimer(teleopTime);
                break;
            // In all of these cases, we just want to stop the timer
            case AUTO_PAUSED:
            case TELEOP_PAUSED:
            case AUTO_END:
            case OVER:
                if (m_matchTimer != null) {
                    m_matchTimer.cancel();
                }
                m_matchTimer = null;
                break;
        }
    }

    /**
     * Starts a timer that either resumes the paused timer, if one exists, or starts a new timer.
     */
    private void startTimer(int startTime) {
        // Create a new timer for a sufficiently long time, which will be cancelled before it runs out.
        // The timer updates the text with the new values from the bound service one a second
        setTimeText(startTime);
        m_matchTimer = new MatchTimer(1000);
        m_matchTimer.start();
    }

    private final class MatchTimer extends CountDownTimer {

        public MatchTimer(int time) {
            // One thousand millisecond callbacks. The cast to a long is just in case a very long
            // time is specified and the number of millis won't fit in an integer. This explicitly
            // converts the seconds to a long before to ensure no ambiguity
            super(time * 1000, 1000);
        }

        @Override
        public void onTick(long l) {
            if (FieldTimeService.isTimerRunning()) {
                int curTime = FieldTimeService.getTimeRemaining();
                setTimeText(curTime);
            }
        }

        @Override
        public void onFinish() {
        }
    }

    private void setTimeText(int curTime) {
        String postfix = curTime == 1 ? " second" : " seconds";
        m_timeView.setText(curTime + postfix);
    }
}
