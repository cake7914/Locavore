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

import java.io.IOException;
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
    DataManager dataManager = DataManager.getInstance(location);
    private List<User> mFarms = new ArrayList<>(dataManager.mFarms);
    private List<Event> mEvents = new ArrayList<>(dataManager.mEvents);


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
        eventsAdapter = new FarmEventsAdapter(getContext(), mEvents);
        rvFarmEvents.setAdapter(eventsAdapter);
        rvFarmEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        rvFarmProfiles = view.findViewById(R.id.rvFarmProfiles);
        profilesAdapter = new FarmProfilesAdapter(getContext(), mFarms);
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
                dataManager.getFarms(location, ParseUser.getCurrentUser().getInt(User.KEY_RADIUS));
                compareFarmInstances();
                compareEventsInstances();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void compareFarmInstances() {
        for(int i = 0; i < dataManager.mFarms.size(); i++) {
            if(!mFarms.contains(dataManager.mFarms.get(i))) {
                mFarms.add(dataManager.mFarms.get(i));
                profilesAdapter.notifyItemInserted(mFarms.size()-1);
            }
        }
        // remove if needed
        mFarms.removeIf(farm -> !dataManager.mFarms.contains(farm));
        profilesAdapter.notifyDataSetChanged();
    }

    private void compareEventsInstances() {
        for(int i = 0; i < dataManager.mEvents.size(); i++) {
            if(!mEvents.contains(dataManager.mEvents.get(i))) {
                mEvents.add(dataManager.mEvents.get(i));
                eventsAdapter.notifyItemInserted(mEvents.size()-1);
            }
        }

        // remove if needed
        mEvents.removeIf(event -> !dataManager.mEvents.contains(event));
        eventsAdapter.notifyDataSetChanged();
    }
}