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
        URLConnection connection = EventsApi.getEventsConnection(year, null);
        if (connection == null) {
            Log.w(FIRSTApi.class.getName(), "Could not retrieve events: null connection");
            return;
        }

        Optional<EventsApi.EventsListingModel> modelOptional = getModel(connection, EventsApi.EventsListingModel.class);
        if (modelOptional.isPresent()) {
            EventsApi.EventsListingModel model = modelOptional.get();
            Database db = null;
            try {
                db = DatabaseFactory.getInstance().getDatabase(FTAMonitorApplication.getContext());
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
            } catch (DatabaseException e) {
                Log.e(FIRSTApi.class.getName(), "Error when trying to update the event list", e);
            } finally {
                if (db != null) {
                    DatabaseFactory.getInstance().release(db);
                }
            }
        }
    }

    @Override
    public void updateEvent(Event event) {

    }

    @Override
    public void updateTeam(int teamNumber) {
        URLConnection connection = TeamsApi.getTeamsConnection(teamNumber, DateTime.now().getYear());
        if (connection == null) {
            Log.e(FIRSTApi.class.getName(), "Could not retrieve team: connection is null");
            return;
        }

        Optional<TeamsApi.TeamsListingModel> modelOptional = getModel(connection, TeamsApi.TeamsListingModel.class);
        if (modelOptional.isPresent()) {
            TeamsApi.TeamsListingModel model = modelOptional.get();

            if (model.teamCountTotal < 0) {
                Log.w(FIRSTApi.class.getName(), "Could not retrieve information on team " + teamNumber);
                return;
            } else if (model.teamCountTotal > 1) {
                Log.w(FIRSTApi.class.getName(), "Multiple records found for team " + teamNumber +
                        ". Using first one.");
            }

            TeamsApi.TeamModel teamModel = model.teams[0];
            Database db = null;
            try {
                db = DatabaseFactory.getInstance().getDatabase(FTAMonitorApplication.getContext());
                Optional<Team> teamDbOptional = db.getTeam(teamNumber);
                Team team = null;
                if (teamDbOptional.isPresent()) {
                    team = teamDbOptional.get();
                    team.setTeamName(teamModel.nameFull);
                    team.setTeamNick(teamModel.nameNick);
                } else {
                    team = AssistantFactory.getInstance().makeTeam(teamModel.number,
                            teamModel.nameFull,
                            teamModel.nameNick,
                            null, null, null);
                }

                db.saveTeam(team);
            } catch (DatabaseException e) {
                Log.e(FIRSTApi.class.getName(), "Database error when saving data for team " + teamNumber, e);
            } finally {
                if (db != null) {
                    DatabaseFactory.getInstance().release(db);
                }
            }

        }
    }

    private <T> Optional<T> getModel(URLConnection connection, Class<T> tClass) {
        BufferedReader reader = null;
        try {
            connection.connect();
            int status = ((HttpURLConnection) connection).getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                Log.e(FIRSTApi.class.getName(), "Received error code " + status + " when retrieving from " + connection.getURL());
                return Optional.absent();
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Gson gson = new GsonBuilder().create();
            T model = gson.fromJson(reader, tClass);
            return Optional.of(model);
        } catch (IOException e) {
            Log.e(FIRSTApi.class.getName(), "Error while trying to retrieve for class " + tClass.getName(), e);
            return Optional.absent();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(FIRSTApi.class.getName(), "Error while trying to retrieve for class " + tClass.getName(), e);
                }
            }
        }
    }
}
