package com.fsilberberg.ftamonitor.view;

import android.app.Activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fsilberberg.ftamonitor.R;


public class DrawerActivity extends Activity {

    private String[] m_drawerItems;
    private ListView m_drawerList;
    private DrawerLayout m_drawerLayout;
    private ActionBarDrawerToggle m_drawerToggle;
    private CharSequence m_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Set up the drawer content
        m_drawerItems = getResources().getStringArray(R.array.drawer_tabs);
        m_drawerList = (ListView) findViewById(R.id.left_drawer);
        m_drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.list_white_text, R.id.list_content, m_drawerItems));

        // Set up the drawer toggle and listeners
        m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        m_drawerToggle = new ActionBarDrawerToggle(this, m_drawerLayout, R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                m_title = getActionBar().getTitle();
                getActionBar().setTitle("FTA Monitor");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setTitle(m_title);
            }
        };
        m_drawerLayout.setDrawerListener(m_drawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        m_drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Set up the main content
        getFragmentManager().beginTransaction().replace(R.id.container, new FieldMonitorFragment()).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (m_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectScreen(int position) {
        Fragment newFrag = null;
        switch (position) {
            case 0:
                m_title = "Field Monitor";
                newFrag = new FieldMonitorFragment();
                break;
            case 1:
                m_title = "Settings";
                newFrag = new SettingsFragment();
                break;
        }

        // Replace the current fragment
        if (newFrag != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, newFrag)
                    .addToBackStack(null)
                    .commit();

            // Highlight item, set title, close drawer
            m_drawerList.setItemChecked(position, true);
            getActionBar().setTitle(m_title);
            m_drawerLayout.closeDrawer(m_drawerList);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectScreen(i);
        }
    }
}
