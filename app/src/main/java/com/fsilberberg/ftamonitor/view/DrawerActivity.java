package com.fsilberberg.ftamonitor.view;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.view.fieldmonitor.FieldMonitorFragment;


public class DrawerActivity extends ActionBarActivity {

    // If defined with a value of DisplayView, the specified view will be shown
    public static final String VIEW_INTENT_EXTRA = "VIEW";

    private ListView m_drawerList;
    private DrawerLayout m_drawerLayout;
    private ActionBarDrawerToggle m_drawerToggle;
    private CharSequence m_title;

    private boolean backButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(DrawerActivity.class.getName(), "Called Drawer Activity");

        setContentView(R.layout.activity_drawer);

        // Set up the drawer content
        String[] drawerItems = getResources().getStringArray(R.array.drawer_tabs);
        m_drawerList = (ListView) findViewById(R.id.left_drawer);
        m_drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.list_white_text, R.id.list_content, drawerItems));

        // Set up the drawer toggle and listeners
        m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        m_drawerToggle = new ActionBarDrawerToggle(this, m_drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                m_title = getSupportActionBar().getTitle();
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(m_title);
            }
        };
        m_drawerLayout.setDrawerListener(m_drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        m_drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Set up the main content
        getFragmentManager().beginTransaction().replace(R.id.container, new FieldMonitorFragment(), FieldMonitorFragment.class.getName()).commit();

        // Display the activity when the screen is locked!
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (backButtonPressed) {
            super.onBackPressed();
            return;
        }

        backButtonPressed = true;
        Toast.makeText(this, "Pres Back Again to Exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backButtonPressed = true;
            }
        }, 2000);
    }

    private void selectScreen(int position) {
        if (position == getCurrentTab()) {
            m_drawerLayout.closeDrawer(m_drawerList);
            return;
        }

        Fragment newFrag = null;
        switch (position) {
            case 0:
                m_title = getString(R.string.action_field_monitor);
                newFrag = new FieldMonitorFragment();
                break;
            case 1:
                m_title = getString(R.string.action_settings);
                newFrag = new SettingsFragment();
                break;
        }

        // Replace the current fragment
        if (newFrag != null) {
            // Highlight item, set title, close drawer
            m_drawerList.setItemChecked(position, true);
            getSupportActionBar().setTitle(m_title);
            m_drawerLayout.closeDrawer(m_drawerList);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, newFrag, newFrag.getClass().getName())
                    .commit();
        }
    }

    private int getCurrentTab() {
        Fragment fieldFrag = getFragmentManager().findFragmentByTag(FieldMonitorFragment.class.getName());
        Fragment settingsFrag = getFragmentManager().findFragmentByTag(SettingsFragment.class.getName());
        if (fieldFrag != null && fieldFrag.isVisible()) {
            return 0;
        } else if (settingsFrag != null && settingsFrag.isVisible()) {
            return 1;
        } else {
            throw new RuntimeException("Error: Unknown fragment active");
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectScreen(i);
        }
    }

    public enum DisplayView {
        FIELD_MONITOR, SETTINGS
    }
}
