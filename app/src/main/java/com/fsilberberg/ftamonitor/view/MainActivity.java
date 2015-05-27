package com.fsilberberg.ftamonitor.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fsilberberg.ftamonitor.R;


public class MainActivity extends AppCompatActivity implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment m_navigationDrawerFragment;
    @InjectView(R.id.toolbar_actionbar)
    protected Toolbar m_toolbar;
    @InjectView(R.id.drawer)
    protected DrawerLayout m_drawerLayout;

    private boolean m_backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(m_toolbar);

        m_navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        m_navigationDrawerFragment.setup(R.id.fragment_drawer, m_drawerLayout, m_toolbar);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (m_navigationDrawerFragment.isDrawerOpen()) {
            m_navigationDrawerFragment.closeDrawer();
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
}
