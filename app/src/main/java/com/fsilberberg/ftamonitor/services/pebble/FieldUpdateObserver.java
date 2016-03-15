package com.fsilberberg.ftamonitor.services.pebble;

import android.databinding.Observable;

import com.fsilberberg.ftamonitor.BR;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;

public final class FieldUpdateObserver extends Observable.OnPropertyChangedCallback implements DeletableObserver {
    private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
    private final PebbleSender m_sender;

    public FieldUpdateObserver(PebbleSender sender) {
        m_sender = sender;
    }

    public FieldUpdateObserver init() {
        m_fieldStatus.addOnPropertyChangedCallback(this);
        return this;
    }

    public void unregister() {
        m_fieldStatus.removeOnPropertyChangedCallback(this);
    }

    @Override
    public void checkUpdate() {
        checkMatchStatus();
    }

    @Override
    public void onPropertyChanged(Observable observable, int property) {
        if (property != BR.matchStatus) {
            return;
        }

        checkMatchStatus();
    }

    private void checkMatchStatus() {
        PebbleSender.PebbleMessage message =
                new PebbleSender.PebbleMessage(PebbleSender.MATCH_STATE,
                        m_fieldStatus.getMatchStatus().ordinal(), false);
        m_sender.addMessage(PebbleSender.MATCH_STATE, message);
    }
}
