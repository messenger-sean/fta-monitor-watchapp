package com.fsilberberg.ftamonitor.view.old.testing;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fsilberberg.ftamonitor.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingFragment extends Fragment {

    private ViewPager m_viewPager;
    private PagerTabStrip m_tabStrip;
    private TestingPagerAdapter m_testingPagerAdapter;

    public TestingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_testing, container, false);
        m_tabStrip = (PagerTabStrip) root.findViewById(R.id.testing_pager_tab_strip);
        m_viewPager = (ViewPager) root.findViewById(R.id.testing_pager);
        m_testingPagerAdapter = new TestingPagerAdapter(getFragmentManager());
        m_viewPager.setAdapter(m_testingPagerAdapter);
        m_tabStrip.setTextColor(getResources().getColor(R.color.FRC_DARK_GREY));
        m_tabStrip.setTabIndicatorColorResource(R.color.FRC_DARK_GREY);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_testing));
    }

    private class TestingPagerAdapter extends FragmentPagerAdapter {
        public TestingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TestingConnectionStatus();
                case 1:
                    return new TestingFieldStatus();
                default:
                    return TestingTeamStatus.makeInstance(position - 1);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Connection Control";
                case 1:
                    return "Field Control";
                case 2:
                case 3:
                case 4:
                    return "Red " + (position - 1) + " Control";
                case 5:
                case 6:
                case 7:
                    return "Blue " + (position - 4) + " Control";
                default:
                    return "Team " + (position - 1) + " Control";
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    }
}
