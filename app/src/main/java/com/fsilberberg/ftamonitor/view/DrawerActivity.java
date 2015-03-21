package com.fsilberberg.ftamonitor.view;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;
import com.fsilberberg.ftamonitor.view.fieldmonitor.FieldMonitorFragment;
import com.fsilberberg.ftamonitor.view.testing.TestingFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.ArrayList;

public class DrawerActivity extends ActionBarActivity {

    public static final String START_FRAGMENT = "start_fragment";
    public static final int FIELD_MONITOR = 0;
    public static final int TESTING = 1;
    public static final int SETTINGS = 2;

    private Drawer.Result m_drawer = null;
    private boolean m_backButtonPressed = false;
    private int m_curPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Check to see which screens are enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean fieldMonitor = prefs.getBoolean(getResources().getString(R.string.field_monitor_enabled_key), true);
        final boolean testing = prefs.getBoolean(getResources().getString(R.string.testing_enabled_key), false);

        // Handle Toolbar
        ArrayList<IDrawerItem> items = new ArrayList<>();
        if (fieldMonitor) {
            items.add(new PrimaryDrawerItem().withName(R.string.field_monitor_drawer));
            // Only add testing if the field monitor is enabled in the first place
            if (testing) {
                items.add(new PrimaryDrawerItem().withName(R.string.testing_drawer));
            }
        }
        items.add(new PrimaryDrawerItem().withName(R.string.settings_drawer));

        m_drawer = new Drawer()
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .withDrawerItems(items)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            if (m_curPos != position) {
                                FragmentTransaction trans = getFragmentManager().beginTransaction();
                                switch (position) {
                                    case 0:
                                        if (!fieldMonitor) {
                                            trans.replace(
                                                    R.id.frame_container,
                                                    new SettingsFragment(),
                                                    SettingsFragment.class.getName());
                                        } else {
                                            trans.replace(
                                                    R.id.frame_container,
                                                    new FieldMonitorFragment(),
                                                    FieldMonitorFragment.class.getName()
                                            );
                                        }
                                        break;
                                    case 1:
                                        if (!testing) {
                                            trans.replace(
                                                    R.id.frame_container,
                                                    new SettingsFragment(),
                                                    SettingsFragment.class.getName());
                                        } else {
                                            trans.replace(
                                                    R.id.frame_container,
                                                    new TestingFragment(),
                                                    TestingFragment.class.getName()
                                            );
                                        }
                                        break;
                                    case 2:
                                        trans.replace(
                                                R.id.frame_container,
                                                new SettingsFragment(),
                                                SettingsFragment.class.getName());
                                        break;
                                    default:
                                        return;
                                }
                                trans.commit();
                                m_curPos = position;
                            }
                        }
                    }
                }).build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Check to see if we have a default screen, and if we do, switch to it
        int startScreen = fieldMonitor ? FIELD_MONITOR : SETTINGS;
        if (getIntent() != null && getIntent().hasExtra(START_FRAGMENT)) {
            int extra = getIntent().getIntExtra(START_FRAGMENT, startScreen);
            switch (extra) {
                case FIELD_MONITOR:
                    if (fieldMonitor) {
                        startScreen = FIELD_MONITOR;
                    }
                    break;
                case TESTING:
                    if (fieldMonitor && testing) {
                        startScreen = TESTING;
                    }
                    break;
                case SETTINGS:
                    startScreen = SETTINGS;
                    break;
                default:
                    break;
            }
        }

        FragmentTransaction trans = getFragmentManager().beginTransaction();
        switch (startScreen) {
            case FIELD_MONITOR:
                trans.replace(R.id.frame_container, new FieldMonitorFragment(), FieldMonitorFragment.class.getName());
                m_drawer.setSelection(0);
                break;
            case TESTING:
                trans.replace(R.id.frame_container, new TestingFragment(), TestingFragment.class.getName());
                m_drawer.setSelection(1);
                break;
            case SETTINGS:
                trans.replace(R.id.frame_container, new SettingsFragment(), SettingsFragment.class.getName());
                m_drawer.setSelection(items.size() - 1);
                break;
        }
        trans.commit();
    }

    @Override
    public void onBackPressed() {
        if (m_drawer.isDrawerOpen()) {
            m_drawer.closeDrawer();
            m_backButtonPressed = false;
        } else {
            if (m_backButtonPressed) {
                super.onBackPressed();
                stopService(new Intent(this, FieldConnectionService.class));
                return;
            }

            m_backButtonPressed = true;
            Toast.makeText(this, "Pres Back Again to Exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_backButtonPressed = true;
                }
            }, 2000);
        }
    }
}

