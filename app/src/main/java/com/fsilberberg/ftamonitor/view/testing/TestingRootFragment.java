package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.astuetz.PagerSlidingTabStrip;
import com.fsilberberg.ftamonitor.R;

/**
 * The root of all testing fragments, manages the current active fragment
 */
public class TestingRootFragment extends Fragment {

    protected Toolbar m_toolbar;
    @InjectView(R.id.testing_tabs)
    protected PagerSlidingTabStrip m_tabs;
    @InjectView(R.id.testing_pager)
    protected ViewPager m_pager;
    private PagerAdapter m_adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_testing_root, container, false);
        ButterKnife.inject(this, v);
        m_toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);

        m_adapter = new PagingAdapter(getChildFragmentManager());
        m_pager.setAdapter(m_adapter);
        m_tabs.setViewPager(m_pager);

        return v;
    }

    private class PagingAdapter extends FragmentPagerAdapter {

        private final String[] m_categories = new String[]{
                "Randomizer", "Connection Status", "Field Status",
                "Red 1", "Red 2", "Red 3",
                "Blue 1", "Blue 2", "Blue 3"
        };
        private final FragmentManager m_fm;

        public PagingAdapter(FragmentManager fm) {
            super(fm);
            m_fm = fm;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return m_categories[position];
        }

        @Override
        public Fragment getItem(int position) {
            return new TestingRandomization();
        }

        @Override
        public int getCount() {
            return m_categories.length;
        }
    }
}
