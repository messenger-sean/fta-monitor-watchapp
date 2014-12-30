package com.fsilberberg.ftamonitor.api.firstapi;

import android.util.Log;
import com.fsilberberg.ftamonitor.FTAMonitorApplication;
import com.fsilberberg.ftamonitor.api.Api;
import com.fsilberberg.ftamonitor.database.Database;
import com.fsilberberg.ftamonitor.database.DatabaseException;
import com.fsilberberg.ftamonitor.database.DatabaseFactory;
import com.fsilberberg.ftamonitor.ftaassistant.AssistantFactory;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of the {@link com.fsilberberg.ftamonitor.api.Api} interface that talks to the official FIRST Api
 */
public class FIRSTApi implements Api {

    // Official url of the FIRST server
    private static final String FIRST_SERVER = "http://private-1246e-frceventsprelimapitraffic.apiary-proxy.com/api/";
    private static final String AUTHORIZATION_KEY = "Authorization";
    private static final String AUTHORIZATION_VALUE = "Token communitysampletoken";
    private static final String ACCEPT_KEY = "Accept";
    private static final String ACCEPT_VALUE = "application/json";

    public static URLConnection createConnection(String location) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(FIRST_SERVER);
        urlBuilder.append(location);
        try {
            URL url = new URL(urlBuilder.toString());
            URLConnection connection = url.openConnection();
            connection.addRequestProperty(AUTHORIZATION_KEY, AUTHORIZATION_VALUE);
            connection.addRequestProperty(ACCEPT_KEY, ACCEPT_VALUE);
            return connection;
        } catch (java.io.IOException e) {
            Log.w(FIRSTApi.class.getName(), "Could not create url for " + urlBuilder.toString(), e);
            return null;
        }
    }

    @Override
    public void retrieveAllEvents(int year) {
        URLConnection connection = new EventsApi().getEventsConnection(year, null);
        if (connection == null) {
            Log.w(FIRSTApi.class.getName(), "Could not retrieve events: null connection");
            return;
        }

        BufferedReader jsonReader = null;
        try {
            connection.connect();
            // If there was an error while attempting to refresh the events, then return
            if (((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(FIRSTApi.class.getName(), "Received response code " + ((HttpURLConnection) connection).getResponseCode());
                return;
            }
            jsonReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Gson gson = new GsonBuilder().create();
            EventsApi.EventsListingModel model = gson.fromJson(jsonReader, EventsApi.EventsListingModel.class);
            Database db = DatabaseFactory.getInstance().getDatabase(FTAMonitorApplication.getContext());
            if (model.eventCount > 0) {
                Collection<Event> finalEvents = new ArrayList<>();
                for (EventsApi.EventModel em : model.Events) {
                    // Query the database and see if the event already exists
                    Optional<Event> query = db.getEvent(DateTime.now().withYear(year), em.code);
                    if (query.isPresent()) {
                        Event event = query.get();
                        event.setEventLoc(em.location);
                        event.setEventName(em.name);
                        event.setStartDate(new DateTime(em.dateStart));
                        event.setEndDate(new DateTime(em.dateEnd));
                        finalEvents.add(event);
                    } else {
                        Event event = AssistantFactory.getInstance().makeEvent(em.code,
                                em.name,
                                em.location,
                                new DateTime(em.dateStart),
                                new DateTime(em.dateEnd),
                                null, null, null);
                        finalEvents.add(event);
                    }
                }

                Event[] arr = new Event[0];
                db.saveEvent(finalEvents.toArray(arr));
            }
        } catch (IOException | DatabaseException e) {
            Log.e(FIRSTApi.class.getName(), "Error when trying to update the event list", e);
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException e) {
                    Log.w(FIRSTApi.class.getName(), "Exception thrown when trying to close the reader", e);
                }
            }
        }
    }

    @Override
    public void updateEvent(Event event) {

    }

    @Override
    public void updateTeam(Team team) {

    }
}
