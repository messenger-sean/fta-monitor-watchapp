package com.fsilberberg.ftamonitor.view.eventdetail;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.database.Database;
import com.fsilberberg.ftamonitor.database.DatabaseFactory;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

/**
 * This fragment shows the details of an event, including the schedule and team list if available
 */
public class EventDetailFragment extends Fragment {

    // Intent extra to tell the EventDetailFragment what event to retrieve from the database
    public static final String EVENT_CODE_INTENT_EXTRA = "EVENT_CODE";
    public static final String EVENT_YEAR_INTENT_EXTRA = "EVENT_YEAR";

    /**
     * Package-protected helper method for retrieving a specific event from the database
     *
     * @param year    The year of the event
     * @param code    The code of the event
     * @param context The context for retrieving the database
     * @return The retrieved event from the database
     */
    static Event getEvent(int year, String code, Context context) {
        Database db = DatabaseFactory.getInstance().getDatabase(context);
        try {
            Optional<Event> maybeEvent = db.getEvent(DateTime.now().withYear(year), code);
            if (!maybeEvent.isPresent()) {
                throw new RuntimeException("Could not find event " + code + " in year " + year);
            }

            return maybeEvent.get();
        } finally {
            DatabaseFactory.getInstance().release(db);
        }
    }

    public static EventDetailFragment newInstance(int year, String code) {
        EventDetailFragment fragment = new EventDetailFragment();

        Bundle args = new Bundle();
        args.putInt(EVENT_YEAR_INTENT_EXTRA, year);
        args.putString(EVENT_CODE_INTENT_EXTRA, code);
        fragment.setArguments(args);

        return fragment;
    }

    // The elevation to set the tab bar to and to reset the action bar to on leaving
    private static final int ELEVATION = 20;

    private PagerTabStrip m_tabStrip;

    private String m_eventCode;
    private int m_eventYear;

    public EventDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        setElevation();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setElevation() {
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

        Bundle args = getArguments();
        m_eventCode = args.getString(EVENT_CODE_INTENT_EXTRA);
        m_eventYear = args.getInt(EVENT_YEAR_INTENT_EXTRA);
        Log.d(getClass().getName(), "Received code " + m_eventCode);

        // Set up the tab strip and adapters
        m_tabStrip = (PagerTabStrip) mainView.findViewById(R.id.event_detail_pager_strip);
        EventDetailPagerAdapter adapter = new EventDetailPagerAdapter(getChildFragmentManager());
        ViewPager pager = (ViewPager) mainView.findViewById(R.id.event_detail_pager);
        pager.setAdapter(adapter);
        m_tabStrip.setTabIndicatorColor(Color.WHITE);
        m_tabStrip.setTextColor(Color.WHITE);

        return mainView;
    }

    private class EventDetailPagerAdapter extends FragmentPagerAdapter {

        public EventDetailPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(getClass().getName(), "Calling new instance");
            return DetailFragment.newInstance(m_eventYear, m_eventCode);
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
