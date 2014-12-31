package com.fsilberberg.ftamonitor.api.firstapi;

import android.util.Log;

import java.net.URLConnection;

/**
 * This contains the model and keys for obtaining information from the FIRST events API
 */
class EventsApi {

    private static final String LOCATION_FORMAT = "%d/events";
    private static final String EVENT_CODE_FORMAT = "?eventcode=%s";

    public class EventsListingModel {
        public EventModel[] Events;
        public int eventCount;

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Event count: ").append(eventCount);
            for (EventModel model : Events) {
                stringBuilder.append(System.lineSeparator()).append(model.toString());
            }
            return stringBuilder.toString();
        }
    }

    public class EventModel {
        public String code;
        public int id;
        public String name;
        public String type;
        public String districtCode;
        public String venue;
        public String location;
        public String dateStart;
        public String dateEnd;

        @Override
        public String toString() {
            return "EventModel{" +
                    "code='" + code + '\'' +
                    ", id=" + id +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", districtCode='" + districtCode + '\'' +
                    ", venue='" + venue + '\'' +
                    ", location='" + location + '\'' +
                    ", dateStart='" + dateStart + '\'' +
                    ", dateEnd='" + dateEnd + '\'' +
                    '}';
        }
    }

    /**
     * Creates a new connection with the appropriate location for getting an event listing. If you want a listing for
     * all events, pass null for the event code
     *
     * @param year      The year to search the api for. This must be greater than 2015
     * @param eventCode The event to query for. If the event is null, all events will be queried
     * @return A connection with the correct headers and location for connecting. It is <b>NOT</b> yet open
     */
    public static URLConnection getEventsConnection(int year, String eventCode) {
        // TODO: When the api is officially published, update the minimum year to the actual minimum year
        if (year < 2014) {
            Log.e(EventsApi.class.getName(), "Error: Cannot request year less than 2014. Year is " + year);
            return null;
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(String.format(LOCATION_FORMAT, year));
        if (eventCode != null) {
            urlBuilder.append(String.format(EVENT_CODE_FORMAT, eventCode));
        }

        return FIRSTApi.createConnection(urlBuilder.toString());
    }
}
