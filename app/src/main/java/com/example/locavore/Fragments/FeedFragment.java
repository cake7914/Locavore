package com.example.locavore.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.locavore.Adapters.FarmEventsAdapter;
import com.example.locavore.Adapters.FarmProfilesAdapter;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.Farm;
import com.example.locavore.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import permissions.dispatcher.NeedsPermission;

public class FeedFragment extends Fragment implements LocationListener {
    public static final String TAG = "FeedFragment";
    private static final double METERS_TO_MILE = 1609.34;


    private RecyclerView rvFarmProfiles;
    private RecyclerView rvFarmEvents;
    private FarmEventsAdapter eventsAdapter;
    private FarmProfilesAdapter profilesAdapter;
    private List<Event> events;
    private List<Farm> farms;
    private Location location;
    private LocationManager locationManager;
    private Button btnRefresh;

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
        events = new ArrayList<>();
        eventsAdapter = new FarmEventsAdapter(getContext(), events);
        rvFarmEvents.setAdapter(eventsAdapter);
        rvFarmEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        rvFarmProfiles = view.findViewById(R.id.rvFarmProfiles);
        farms = new ArrayList<>();
        profilesAdapter = new FarmProfilesAdapter(getContext(), farms);
        rvFarmProfiles.setAdapter(profilesAdapter);
        rvFarmProfiles.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_feed);

        locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);

        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        queryFarms();
        queryFarmersMarkets();
        queryEvents();
    }

    private void queryFarms() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(Farm.KEY_USER_TYPE, Farm.USER_TYPE);
        query.findInBackground((users, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting farms ", e);
            } else {
                List<Farm> newFarms = new ArrayList<>();
                for (ParseUser user : users) {
                    //show farms that are within the user's radius
                    Location userLocation = new Location(LocationManager.GPS_PROVIDER);
                    userLocation.setLatitude(user.getDouble("latitude"));
                    userLocation.setLongitude(user.getDouble("longitude"));
                    if(location != null) {
                        if(location.distanceTo(userLocation) < ParseUser.getCurrentUser().getDouble("radius"))
                        {
                            Farm farm = new Farm(user.getString(Farm.KEY_NAME), user.getParseFile("profilePhoto").getUrl());
                            newFarms.add(farm);
                            Log.i(TAG, "farm successfully added!");
                        } else {
                            Log.i(TAG, "this farm is not within the required radius!");
                        }
                    } else {
                        Log.i(TAG, "location is null");
                    }
                }
                profilesAdapter.addAll(newFarms);
            }
        });
    }

    private void queryFarmersMarkets() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(Farm.KEY_USER_TYPE, "farmersmarket");
        query.findInBackground((users, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting farms ", e);
            } else {
                List<Farm> newFarms = new ArrayList<>();
                for (ParseUser user : users) {
                    //show farms that are within the user's radius
                    Location userLocation = new Location(LocationManager.GPS_PROVIDER);
                    userLocation.setLatitude(user.getDouble("latitude"));
                    userLocation.setLongitude(user.getDouble("longitude"));
                    if(location != null) {
                        if(location.distanceTo(userLocation) < ParseUser.getCurrentUser().getDouble("radius"))
                        {
                            Farm farm = new Farm(user.getString(Farm.KEY_NAME), user.getParseFile("profilePhoto").getUrl());
                            newFarms.add(farm);
                            Log.i(TAG, "farm successfully added!");
                        } else {
                            Log.i(TAG, "this farm is not within the required radius!");
                        }
                    } else {
                        Log.i(TAG, "location is null");
                    }
                }
                profilesAdapter.addAll(newFarms);
            }
        });
    }



    private void queryEvents() {
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        /*query.findInBackground((newEvents, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting events ", e);
            } else {
                eventsAdapter.addAll(newEvents);
            }
        });*/
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> newEvents, ParseException e) {
                eventsAdapter.addAll(newEvents);
            }
        });

    }

    @Override
    public void onLocationChanged(@NonNull Location newLocation) {
        location = newLocation;
    }
}