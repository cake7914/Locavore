package com.example.locavore.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class FeedFragment extends Fragment implements LocationListener {
    public static final String TAG = "FeedFragment";

    private RecyclerView rvFarmProfiles;
    private RecyclerView rvFarmEvents;
    private FarmEventsAdapter eventsAdapter;
    private FarmProfilesAdapter profilesAdapter;
    private List<Event> events;
    private List<Farm> farms;
    private Location location;
    private LocationManager locationManager;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
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

        queryFarms();
        queryFarmersMarkets();
        queryEvents();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
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
                    // only show farms that are within the user's radius
                    //location.distanceTo(new Location());
                    //if(user.getString("address"))
                    Farm farm = new Farm(user.getString(Farm.KEY_NAME), user.getParseFile("profilePhoto").getUrl());
                    newFarms.add(farm);
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
                    Farm farm = new Farm(user.getString(Farm.KEY_NAME), user.getParseFile("profilePhoto").getUrl());
                    newFarms.add(farm);
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
}