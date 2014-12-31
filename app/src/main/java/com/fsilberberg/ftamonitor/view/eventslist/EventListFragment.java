package com.fsilberberg.ftamonitor.view.eventslist;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventListFragment extends Fragment {

    private ListView m_eventsList;
    private ProgressBar m_progressBar;
    private TextView m_bodyText;

    private SortingOrder order = SortingOrder.DATE;
    private List<Event> events = null;

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
            case R.id.events_list_sort:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Sort Events By:");
                builder.setItems(SortingOrder.getStrings(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        order = SortingOrder.values()[i];
                        if (m_eventsList.getVisibility() == View.VISIBLE) {
                            Collections.sort(events, order.getComparator());
                            m_eventsList.setAdapter(new EventListAdapter(getActivity(), R.id.eventsList, events));
                        }
                    }
                });
                builder.create().show();
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
            Collections.sort(eventsList, order.getComparator());
            return eventsList;
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null && events.size() > 0) {
                m_eventsList.setAdapter(new EventListAdapter(m_context, R.id.eventsList, events));
                m_eventsList.setVisibility(View.VISIBLE);
                EventListFragment.this.events = events;
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

        public EventListAdapter(Context context, int resource, List<Event> objects) {
            super(context, resource, objects);
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
