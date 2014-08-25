package com.fsilberberg.ftamonitor;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fsilberberg.ftamonitor.fieldmonitor.FieldConnectionService;

/**
 * Created by Fredric on 8/23/14.
 */
public class FTAMonitorApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url = preferences.getString(getString(R.string.fms_ip_addr_key), "10.0.100.5");
        Intent serviceIntent = new Intent(getBaseContext(), FieldConnectionService.class);
        serviceIntent.putExtra(FieldConnectionService.URL_INTENT_EXTRA, url);
        startService(serviceIntent);
    }
}
