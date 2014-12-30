package com.fsilberberg.ftamonitor.api.firstapi;

import android.util.Log;
import com.fsilberberg.ftamonitor.api.Api;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.fsilberberg.ftamonitor.ftaassistant.Team;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.BufferOverflowException;

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
            if (((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(FIRSTApi.class.getName(), "Received response code " + ((HttpURLConnection) connection).getResponseCode());
            }
            jsonReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Gson gson = new GsonBuilder().create();
            EventsApi.EventsListingModel model = gson.fromJson(jsonReader, EventsApi.EventsListingModel.class);
            Log.d(FIRSTApi.class.getName(), model.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
