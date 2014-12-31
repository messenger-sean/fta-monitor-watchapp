package com.fsilberberg.ftamonitor.api.firstapi;

import java.net.URLConnection;

/**
 * This contains the models and keys for retrieving teams from the FIRST Teams API
 */
class TeamsApi {

    private static final String LOCATION_FORMAT = "%d/teams";
    private static final String PARAMETER = "?";
    private static final String TEAM_FORMAT = "teamNumber=%d";

    public class TeamsListingModel {
        TeamModel[] teams;
        int teamCountTotal;
        int teamCountPage;
        int pageCurrent;
        int pageTotal;
    }

    public class TeamModel {
        int number;
        String nameFull;
        String nameShort;
        String nameNick;
        String location;
        int rookieYear;
        String robotName;
        String districtCode;
    }

    public static URLConnection getTeamsConnection(int teamNumber, int year) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(String.format(LOCATION_FORMAT, year));
        urlBuilder.append(PARAMETER).append(String.format(TEAM_FORMAT, teamNumber));
        return FIRSTApi.createConnection(urlBuilder.toString());
    }
}
