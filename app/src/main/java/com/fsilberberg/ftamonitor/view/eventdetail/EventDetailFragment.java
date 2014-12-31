package com.fsilberberg.ftamonitor.view.eventdetail;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fsilberberg.ftamonitor.R;

/**
 * This fragment shows the details of an event, including the schedule and team list if available
 */
public class EventDetailFragment extends Fragment {

    // The elevation to set the tab bar to and to reset the action bar to on leaving
    private static final int ELEVATION = 20;

    private PagerTabStrip m_tabStrip;

    public EventDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        // Done in onPause and Resume to ensure that the bar is reset every the view is left
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(0);
            m_tabStrip.setElevation(ELEVATION);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(ELEVATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_event_detail, container, false);
        m_tabStrip = (PagerTabStrip) mainView.findViewById(R.id.event_detail_pager_strip);
        EventDetailPagerAdapter adapter = new EventDetailPagerAdapter(getFragmentManager());
        ViewPager pager = (ViewPager) mainView.findViewById(R.id.event_detail_pager);
        pager.setAdapter(adapter);

        return mainView;
    }


    private class EventDetailPagerAdapter extends FragmentPagerAdapter {

        public EventDetailPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return new DetailFragment();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Event Details";
        }
    }
}
