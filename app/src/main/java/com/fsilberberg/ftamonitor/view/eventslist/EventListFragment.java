package com.fsilberberg.ftamonitor.view.eventslist;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.api.Api;
import com.fsilberberg.ftamonitor.api.ApiFactory;
import com.fsilberberg.ftamonitor.database.Database;
import com.fsilberberg.ftamonitor.database.DatabaseFactory;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
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
    private LinearLayout m_searchLayout;
    private EditText m_searchBox;
    private ImageButton m_cancelButton;

    private SortingOrder m_order = SortingOrder.DATE;
    private List<Event> m_allEvents = null;
    private List<Event> m_adapterEvents = null;
    private EventListAdapter m_adapter;
    private String m_searchString = "";

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
        m_searchLayout = (LinearLayout) mainView.findViewById(R.id.events_list_search_layout);
        m_searchBox = (EditText) mainView.findViewById(R.id.events_list_search_box);
        m_cancelButton = (ImageButton) mainView.findViewById(R.id.events_list_search_cancel);

        m_eventsList.setVisibility(View.INVISIBLE);
        m_bodyText.setVisibility(View.INVISIBLE);
        m_searchLayout.setVisibility(View.GONE);

        // Set the listener for updating the search string
        m_searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(EventListFragment.class.getName(), "Called textwatcher " + charSequence);
                m_searchString = String.valueOf(charSequence);
                updateListView();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Sets the listener for the cancel button, with calls clear search to remove all parameters
        m_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSearch();
            }
        });

        new LoadEventsTask(getActivity()).execute();

        return mainView;
    }

    /**
     * Clears the search box and removes it from being visible in the layout
     */
    private void clearSearch() {
        m_searchBox.setText("");
        m_searchString = "";
        updateListView();
        m_searchLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.events_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.events_list_refresh:
                // If we need to refresh the events, start a background task
                new RefreshEventsTask(getActivity()).execute(2014);
                return true;
            case R.id.events_list_sort:
                // If we're change the sort method, display a dialog for the user to select what sort type
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Sort Events By:");
                builder.setItems(SortingOrder.getStrings(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Update the sorting order. If the list is visible, update it
                        m_order = SortingOrder.values()[i];
                        if (m_eventsList.getVisibility() == View.VISIBLE) {
                            updateListView();
                        }
                    }
                });
                builder.create().show();
                return true;
            case R.id.events_list_search:
                // Toggle the visibility of the search pane
                if (m_searchLayout.getVisibility() != View.VISIBLE) {
                    m_searchLayout.setVisibility(View.VISIBLE);
                    updateListView();
                } else {
                    clearSearch();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates the list view by copying the elements from the master event list, filtering and sorting them in the
     * adapter list, and then letting the adapter know to update. If any of these are not initialized, they will
     * be here
     */
    private void updateListView() {
        if (!m_searchString.equals("")) {
            List<Event> temp = new ArrayList<>(Collections2.filter(m_allEvents, new Predicate<Event>() {
                @Override
                public boolean apply(Event input) {
                    // Search by contains, case does not matter
                    return input.getEventName().toLowerCase().contains(m_searchString.toLowerCase());
                }
            }));
            m_adapterEvents.clear();
            m_adapterEvents.addAll(temp);
        } else {
            if (m_adapterEvents == null) {
                m_adapterEvents = new ArrayList<>();
            } else {
                m_adapterEvents.clear();
            }
            m_adapterEvents.addAll(m_allEvents);
        }
        // Use java sort to sort according to the order's comparator
        Collections.sort(m_adapterEvents, m_order.getComparator());
        if (m_adapter == null) {
            m_adapter = new EventListAdapter(getActivity(), R.id.eventsList, m_adapterEvents);
            m_eventsList.setAdapter(m_adapter);
        } else {
            m_adapter.notifyDataSetChanged();
        }
    }

    /**
     * This is a task that loads the events from the database in the background, and then passed the complete events
     * list off to the adapter to handle loading.
     */
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
            // We need the elements to be in an arraylist for the adapter, so make a clone of
            // the collection to ensure this
            return new ArrayList<>(events);
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null && events.size() > 0) {
                // If we found events, set the proper visibility and update the events list
                EventListFragment.this.m_allEvents = events;
                updateListView();
                m_eventsList.setVisibility(View.VISIBLE);
            } else {
                // No events were in the database, so display a message for the user
                m_bodyText.setText("No Events were found for the current year. " +
                        "Please hit the refresh button at the top of the screen to retrieve the events list.");
                m_bodyText.setVisibility(View.VISIBLE);
            }

            // Either way, remove the progress bar and release the db
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
            // Before executing, move back to the progress bar view
            m_eventsList.setVisibility(View.INVISIBLE);
            m_bodyText.setVisibility(View.INVISIBLE);
            m_progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            // Ensure that we were given a valid year to search
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
            // The db has now been updated, reload the data
            new LoadEventsTask(m_context).execute();
        }
    }

    private class EventListAdapter extends ArrayAdapter<Event> {

        // Date format is "Mar 10, 2015"
        private static final String DATE_FORMAT = "MMM d, y";

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
            eventDate.setText(event.getStartDate().toString(DATE_FORMAT) + " to " + event.getEndDate().toString(DATE_FORMAT));

            return eventView;
        }
    }
}
