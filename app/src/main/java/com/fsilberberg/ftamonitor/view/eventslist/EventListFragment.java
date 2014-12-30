package com.fsilberberg.ftamonitor.view.eventslist;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.database.Database;
import com.fsilberberg.ftamonitor.database.DatabaseFactory;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventListFragment extends Fragment {

    private ListView m_eventsList;
    private ProgressBar m_progressBar;

    public EventListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_event_list, container, false);

        m_eventsList = (ListView) mainView.findViewById(R.id.eventsList);
        m_progressBar = (ProgressBar) mainView.findViewById(R.id.eventsSpinner);

        m_eventsList.setVisibility(View.INVISIBLE);

        new LoadEventsTask(getActivity()).execute();

        return mainView;
    }


    private class LoadEventsTask extends AsyncTask {

        private Context m_context;
        private Database db;
        private List<Event> m_eventList;

        public LoadEventsTask(Context context) {
            m_context = context;
            db = DatabaseFactory.getInstance().getDatabase(m_context);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Collection<? extends Event> events = db.getEvents(DateTime.now());
            m_eventList = new ArrayList<>(events);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (m_eventsList != null) {
                m_eventsList.setAdapter(new EventListAdapter(m_context, R.id.eventsList, m_eventList));
            }

            m_progressBar.setVisibility(View.INVISIBLE);
            m_eventsList.setVisibility(View.VISIBLE);
            if (db != null) {
                DatabaseFactory.getInstance().release(db);
                db = null;
            }
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

            return super.getView(position, convertView, parent);
        }
    }
}
