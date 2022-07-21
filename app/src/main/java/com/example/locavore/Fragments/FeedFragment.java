package com.example.locavore.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.locavore.Adapters.FarmEventsAdapter;
import com.example.locavore.Adapters.FarmProfilesAdapter;
import com.example.locavore.DataManager;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements DataManager.UpdateResponse {
    public static final String TAG = "FeedFragment";
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
        setHasOptionsMenu(true);
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

        Menu menu = toolbar.getMenu();
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                eventsAdapter.getFilter().filter("");
                profilesAdapter.getFilter().filter("");
                return true;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                eventsAdapter.getFilter().filter(query);
                profilesAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, false);

        location = locationManager.getLastKnownLocation(bestProvider);

        dataManager.updateResponse = this;
    }

    @Override
    public void onUpdate(List<User> farms, List<Event> events) {
        eventsAdapter.updateList(events, false);
        profilesAdapter.updateList(farms, false);
    }
}