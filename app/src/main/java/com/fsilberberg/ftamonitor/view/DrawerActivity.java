package com.fsilberberg.ftamonitor.view;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class DrawerActivity extends ActionBarActivity {

    private Drawer.Result m_drawer = null;
    private boolean m_backButtonPressed = false;
    private int m_curPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        String[] drawerItems = getResources().getStringArray(R.array.drawer_tabs);

        // Handle Toolbar
        m_drawer = new Drawer()
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(drawerItems[0]),
                        new PrimaryDrawerItem().withName(drawerItems[1]),
                        new PrimaryDrawerItem().withName(drawerItems[2])
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            if (m_curPos != position) {
                                FragmentTransaction trans = getFragmentManager().beginTransaction();
                                switch (position) {
                                    case 0:
                                        trans.replace(
                                                R.id.frame_container,
                                                new FieldMonitorFragment(),
                                                FieldMonitorFragment.class.getName()
                                        );
                                        break;
                                    case 1:
                                        trans.replace(
                                                R.id.frame_container,
                                                new TestingFragment(),
                                                TestingFragment.class.getName()
                                        );
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

        getFragmentManager().beginTransaction().replace(R.id.frame_container, new FieldMonitorFragment(), FieldMonitorFragment.class.getName()).commit();
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

