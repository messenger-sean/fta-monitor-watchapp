package com.fsilberberg.ftamonitor.view.eventdetail;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements OnMapReadyCallback {

    // Date format is "Mar 10, 2015"
    private static final String DATE_FORMAT = "MMM d, y";

    private Event m_event;

    private TextView m_name;
    private TextView m_code;
    private TextView m_date;
    private TextView m_loc;
    private MapFragment m_map;

    public static DetailFragment newInstance(int year, String eventCode) {
        DetailFragment fragment = new DetailFragment();

        Bundle args = new Bundle();
        args.putInt(EventDetailFragment.EVENT_YEAR_INTENT_EXTRA, year);
        args.putString(EventDetailFragment.EVENT_CODE_INTENT_EXTRA, eventCode);

        fragment.setArguments(args);

        return fragment;
    }

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle args = getArguments();
        int eventYear = args.getInt(EventDetailFragment.EVENT_YEAR_INTENT_EXTRA);
        String eventCode = args.getString(EventDetailFragment.EVENT_CODE_INTENT_EXTRA);
        m_event = EventDetailFragment.getEvent(eventYear, eventCode, getActivity());

        m_name = (TextView) view.findViewById(R.id.event_detail_event_name);
        m_code = (TextView) view.findViewById(R.id.event_detail_event_code);
        m_date = (TextView) view.findViewById(R.id.event_detail_event_dates);
        m_loc = (TextView) view.findViewById(R.id.event_detail_event_location);

        m_name.setText(m_event.getEventName());
        m_code.setText(m_event.getEventCode());
        m_loc.setText(m_event.getEventLoc());
        m_date.setText(m_event.getStartDate().toString(DATE_FORMAT) + " to " + m_event.getEndDate().toString(DATE_FORMAT));

        m_map = (MapFragment) getChildFragmentManager().findFragmentById(R.id.event_detail_map);
        m_map.getMapAsync(this);

        return view;
    }

    public void setEvent(Event event) {
        m_event = event;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocationName(m_event.getEventLoc(), 5);
            if (addresses != null) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                googleMap.addMarker(new MarkerOptions().position(latLng));
                Log.d(getClass().getName(), "Found address");
            } else {
                Log.w(getClass().getName(), "Could not find address");
            }
        } catch (IOException e) {
            Log.w(getClass().getName(), "Could not find address for " + m_event.getEventLoc(), e);
        }

    }
}
