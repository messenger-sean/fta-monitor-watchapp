package com.fsilberberg.ftamonitor.view;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.view.fieldmonitor.FieldMonitorRootFragment;
import com.fsilberberg.ftamonitor.view.testing.TestingRootFragment;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    @Bind(R.id.drawer_layout)
    protected NavigationView m_navigationView;
    @Bind(R.id.toolbar_actionbar)
    protected Toolbar m_toolbar;
    @Bind(R.id.drawer)
    protected DrawerLayout m_drawer;
    private ActionBarDrawerToggle m_toggle;

    private boolean m_backPressed = false;
    private char m_curDrawerEl = 'f';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        m_toggle = new ActionBarDrawerToggle(
                this,
                m_drawer,
                m_toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        m_drawer.setDrawerListener(m_toggle);

        setSupportActionBar(m_toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If we've enabled testing mode, then set the nav bar to be the testing menu. Otherwise,
        // use the regular menu
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean testEnabled = sp.getBoolean(getString(R.string.testing_enabled_key), false);
        if (testEnabled) {
            m_navigationView.inflateMenu(R.menu.main_drawer_testing);
        } else {
            m_navigationView.inflateMenu(R.menu.main_drawer);
        }

        m_navigationView.getMenu().getItem(0).setChecked(true);

        m_navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getAlphabeticShortcut() != m_curDrawerEl) {
                    switch (menuItem.getAlphabeticShortcut()) {
                        case 'f':
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, new FieldMonitorRootFragment())
                                    .commit();
                            break;
                        case 's':
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, new SettingsFragment())
                                    .commit();
                            break;
                        case 't':
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, new TestingRootFragment())
                                    .commit();
                            break;
                    }
                    m_curDrawerEl = menuItem.getAlphabeticShortcut();
                }
                menuItem.setChecked(true);
                m_drawer.closeDrawers();
                return true;
            }
        });

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new FieldMonitorRootFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (m_drawer.isDrawerOpen(m_navigationView)) {
            m_drawer.closeDrawers();
            m_backPressed = false;
        } else if (m_backPressed) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            m_backPressed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_backPressed = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        m_toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        m_toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return m_toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

}
