package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fsilberberg.ftamonitor.R;

/**
 * The root of all testing fragments, manages the current active fragment
 */
public class TestingRootFragment extends Fragment {

    @InjectView(R.id.testing_pager)
    protected ViewPager m_pager;
    @InjectView(R.id.testing_pager_tab_strip)
    protected PagerTabStrip m_tabStrip;
    protected PagerAdapter m_adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_testing_root, container, false);
        ButterKnife.inject(this, v);
        m_adapter = new PagerAdapter(getFragmentManager());

        m_pager.setAdapter(m_adapter);
        m_tabStrip.setTextColor(getResources().getColor(R.color.FRC_DARK_GREY));
        m_tabStrip.setTabIndicatorColorResource(R.color.FRC_DARK_GREY);

        return v;
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        String[] m_names = new String[]{
                "Randomization", "Field Connection", "Field Status",
                "Red 1", "Red 2", "Red 3",
                "Blue 1", "Blue 2", "Blue 3"
        };

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return m_names[position];
        }

        @Override
        public Fragment getItem(int position) {
            return new TestingRandomization();
        }

        @Override
        public int getCount() {
            return m_names.length;
        }
    }
}
