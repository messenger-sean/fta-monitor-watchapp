package com.fsilberberg.ftamonitor.view.testing;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.view.old.fieldmonitor.BlankFragment;

/**
 * The root of all testing fragments, manages the current active fragment
 */
public class TestingRootFragment extends Fragment {

    @InjectView(R.id.testing_pager)
    protected ViewPager m_pager;
    @InjectView(R.id.testing_tab_layout)
    protected TabLayout m_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_testing_root, container, false);
        ButterKnife.inject(this, v);

        m_pager.setAdapter(new PagerAdapter(getFragmentManager()));
        m_layout.setupWithViewPager(m_pager);
        m_layout.setTabMode(TabLayout.MODE_SCROLLABLE);

        return v;
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

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
            switch (position) {
                case 0:
                    return new TestingRandomization();
                default:
                    return new BlankFragment();
            }
        }

        @Override
        public int getCount() {
            return m_names.length;
        }
    }
}
