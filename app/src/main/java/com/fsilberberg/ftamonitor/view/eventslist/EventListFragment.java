package com.fsilberberg.ftamonitor.view.eventslist;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.*;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.api.Api;
import com.fsilberberg.ftamonitor.api.ApiFactory;
import com.fsilberberg.ftamonitor.database.Database;
import com.fsilberberg.ftamonitor.database.DatabaseFactory;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import org.joda.time.DateTime;

import java.nio.DoubleBuffer;
import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventListFragment extends Fragment {

    private ListView m_eventsList;
    private ProgressBar m_progressBar;
    private TextView m_bodyText;

    public EventListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_event_list, container, false);

        m_eventsList = (ListView) mainView.findViewById(R.id.eventsList);
        m_progressBar = (ProgressBar) mainView.findViewById(R.id.eventsSpinner);
        m_bodyText = (TextView) mainView.findViewById(R.id.eventsListBodyText);

        m_eventsList.setVisibility(View.INVISIBLE);
        m_bodyText.setVisibility(View.INVISIBLE);

        new LoadEventsTask(getActivity()).execute();

        return mainView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.events_list_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.events_list_refresh:
                new RefreshEventsTask(getActivity()).execute(2014);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadEventsTask extends AsyncTask<Void, Void, List<Event>> {

        private Context m_context;
        private Database db;

        public LoadEventsTask(Context context) {
            m_context = context;
            db = DatabaseFactory.getInstance().getDatabase(m_context);
        }

        @Override
        protected List<Event> doInBackground(Void... voids) {
            Collection<? extends Event> events = db.getEvents(DateTime.now());
            List<Event> eventsList = new ArrayList<>(events);
            Collections.sort(eventsList, new Comparator<Event>() {
                @Override
                public int compare(Event e1, Event e2) {
                    if (e1.getEventCode().equals(e2.getEventCode())) {
                        return 0;
                    } else if (e1.getStartDate().isBefore(e2.getStartDate())) {
                        return -1;
                    } else if (e2.getStartDate().isBefore(e1.getStartDate())) {
                        return 1;
                    } else {
                        return e1.getEventName().compareTo(e2.getEventName());
                    }
                }
            });
            return eventsList;
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null && events.size() > 0) {
                m_eventsList.setAdapter(new EventListAdapter(m_context, R.id.eventsList, events));
                m_eventsList.setVisibility(View.VISIBLE);
            } else {
                m_bodyText.setText("No Events were found for the current year. " +
                        "Please hit the refresh button at the top of the screen to retrieve the events list.");
                m_bodyText.setVisibility(View.VISIBLE);
            }

            m_progressBar.setVisibility(View.INVISIBLE);
            if (db != null) {
                DatabaseFactory.getInstance().release(db);
                db = null;
            }
        }
    }

    private class RefreshEventsTask extends AsyncTask<Integer, Void, Void> {

        private final Context m_context;

        public RefreshEventsTask(Context context) {
            m_context = context;
        }

        @Override
        protected void onPreExecute() {
            m_eventsList.setVisibility(View.INVISIBLE);
            m_bodyText.setVisibility(View.INVISIBLE);
            m_progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            if (integers == null || integers[0] == null) {
                Log.w(RefreshEventsTask.class.getName(), "Asked to refresh null year!");
                return null;
            }
            int year = integers[0];
            Log.i(LoadEventsTask.class.getName(), "Starting events refresh for year " + year);
            Api api = ApiFactory.getInstance().getApi();
            api.retrieveAllEvents(year);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadEventsTask(m_context).execute();
        }
    }

    private class EventListAdapter extends ArrayAdapter<Event> {

        private LayoutInflater m_inflater;
        private Context m_context;

        public EventListAdapter(Context context, int resource, List<Event> objects) {
            super(context, resource, objects);
            m_context = context;
            m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // If the old view isn't null, use that view. Otherwise, create a new view
            View eventView = convertView != null ? convertView : m_inflater.inflate(R.layout.event_list_element, parent, false);
            Event event = getItem(position);
            TextView eventName = (TextView) eventView.findViewById(R.id.eventListName);
            TextView eventLoc = (TextView) eventView.findViewById(R.id.eventListLoc);
            TextView eventDate = (TextView) eventView.findViewById(R.id.eventListDate);

            eventName.setText(event.getEventName());
            eventLoc.setText(event.getEventLoc());
            eventDate.setText(event.getStartDate().toString("d MMM, y"));

            return eventView;
        }
    }
}
