package com.example.locavore.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.locavore.Adapters.FarmEventsAdapter;
import com.example.locavore.Adapters.FarmProfilesAdapter;
import com.example.locavore.DataManager;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements LocationListener {
    public static final String TAG = "FeedFragment";
    private static final double MIN_DISTANCE_CHANGE = 19312.1;

    private RecyclerView rvFarmProfiles;
    private RecyclerView rvFarmEvents;
    private FarmEventsAdapter eventsAdapter;
    private FarmProfilesAdapter profilesAdapter;
    private Location location;
    private LocationManager locationManager;
    private String bestProvider;
    DataManager dataManager = DataManager.getInstance();


    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    //@NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFarmEvents = view.findViewById(R.id.rvFarmEvents);
        eventsAdapter = new FarmEventsAdapter(getContext(), dataManager.mEvents);
        rvFarmEvents.setAdapter(eventsAdapter);
        rvFarmEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        rvFarmProfiles = view.findViewById(R.id.rvFarmProfiles);
        profilesAdapter = new FarmProfilesAdapter(getContext(), dataManager.mFarms);
        rvFarmProfiles.setAdapter(profilesAdapter);
        rvFarmProfiles.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_feed);

        locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(bestProvider, 0, 0, this);

        location = locationManager.getLastKnownLocation(bestProvider);
    }

    @Override
    public void onLocationChanged(@NonNull Location newLocation) {
        if(location.distanceTo(newLocation) > MIN_DISTANCE_CHANGE) {
            location = newLocation;
            try {
                dataManager.queryFarms(User.FARM_USER_TYPE, location);
                dataManager.queryFarms(User.FARMERS_MARKET_USER_TYPE, location);
                profilesAdapter.notifyDataSetChanged();
                eventsAdapter.notifyDataSetChanged();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // the way to do this is by using each qualifier to add to a total weight, then ordering them by the weights??

    // right now I just take into consideration the distance.

    // first thing to add: if the farm is followed by the user, add weight

    public void insertEvent(Event newEvent) {
        for(int i = 0; i < dataManager.mEvents.size(); i++) {
            Location eventLocation = new Location(bestProvider);
            eventLocation.setLatitude(dataManager.mEvents.get(i).getDouble("latitude"));
            eventLocation.setLongitude(dataManager.mEvents.get(i).getDouble("longitude"));

            Location newEventLocation = new Location(bestProvider);
            newEventLocation.setLatitude(newEvent.getLatitude());
            newEventLocation.setLongitude(newEvent.getLongitude());

            if(location.distanceTo(newEventLocation) < location.distanceTo(eventLocation)) {
                // insert this event earlier than previous event
                dataManager.mEvents.add(i, newEvent);
                return;
            }
        }
        // if we haven't hit the return, that means to just add the event at the end of the list
        dataManager.mEvents.add(newEvent);
    }

    public void weightEvent(Event event) throws JSONException {
        double weight = 0;

        // calculate distance: subtract from total weight (want the highest weight to be the first shown)
        Location eventLocation = new Location(bestProvider);
        eventLocation.setLatitude(event.getDouble("latitude")); //!!TODO change this to be a parse geo location in the database!!
        eventLocation.setLongitude(event.getDouble("longitude"));
        weight -= location.distanceTo(eventLocation);

        // if the user does follow the farm
        if(checkUserFollowingFarm(event.getFarm(), ParseUser.getCurrentUser().getJSONArray(User.KEY_FARMS_FOLLOWING)) != -1) {
            weight += 5000;
        }

        // if the user has attended an event at the farm before

    }

    protected int checkUserFollowingFarm(String farmID, JSONArray farmsFollowing) throws JSONException {
        if(farmsFollowing != null) {
            for (int i = 0; i < farmsFollowing.length(); i++) {
                if(farmsFollowing.getString(i).equals(farmID)) {
                    return i;
                }
            }
        }
        return -1;
    }
}