package com.fsilberberg.ftamonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldConnectionService;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;

import java.util.concurrent.atomic.AtomicInteger;


public class FieldMonitor extends Activity {

    private static final int UPDATE_UI = 1;

    private final AtomicInteger m_updateHandler = new AtomicInteger(0);
    private Handler m_uiHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_monitor);
        Intent i = new Intent(this, FieldConnectionService.class);
        i.putExtra(FieldConnectionService.URL_INTENT_EXTRA, "http://10.0.0.164:8085/signalr");
        startService(i);

        m_uiHandler = new Handler(getMainLooper(), new FieldMonitorUiHandler(this));
        FieldMonitorFactory.getInstance().registerForUpdates(new FieldMonitorObserver(m_updateHandler, m_uiHandler));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.field_monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUi() {
        // TODO: Actually update the ui

        m_updateHandler.set(0);
    }

    private class FieldMonitorObserver implements IObserver {

        private final AtomicInteger m_updateHandler;
        private final Handler m_uiHandler;

        public FieldMonitorObserver(AtomicInteger updateHandler, Handler uiHandler) {
            m_updateHandler = updateHandler;
            m_uiHandler = uiHandler;
        }

        @Override
        public void update() {
            // If the handler is 0 and was set to 1, then send a message to the ui handler to update
            // the ui. If not, then don't do anything. This ensures that if multiple updates are
            // called simultaneously, only one ui update happens.
            if (m_updateHandler.compareAndSet(0, 1)) {
                m_uiHandler.sendEmptyMessage(UPDATE_UI);
            }
        }
    }

    private class FieldMonitorUiHandler implements Handler.Callback {

        private final FieldMonitor m_activity;

        public FieldMonitorUiHandler(FieldMonitor activity) {
            m_activity = activity;
        }

        @Override
        public boolean handleMessage(Message message) {
            m_activity.updateUi();
            return false;
        }
    }
}
