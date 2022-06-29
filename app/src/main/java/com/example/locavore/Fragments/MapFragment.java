package com.example.locavore.Fragments;

import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.example.locavore.BuildConfig.YELP_API_KEY;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.locavore.Adapters.CustomWindowAdapter;
import com.example.locavore.Adapters.MapProfilesAdapter;
import com.example.locavore.Models.Farm;
import com.example.locavore.R;
import com.example.locavore.Models.FarmSearchResult;
import com.example.locavore.YelpService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.maps.android.ui.IconGenerator;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RuntimePermissions
public class MapFragment extends Fragment {

    public static final String TAG = "MapFragment";
    private static final String CATEGORY_FARM = "farms";
    private static final String CATEGORY_FARMERS_MARKET = "farmersmarket";
    public static final String BASE_URL = "https://api.yelp.com/v3/";
    private static final int MAX_YELP_RADIUS = 40000; // 40,000 meters or ~25 miles
    private static final int YELP_RADIUS_INCREMENT = 8000; // ~ 5 miles
    private static final double METERS_TO_MILE = 1609.34;
    private static final long UPDATE_INTERVAL = 100000;
    private static final long FASTEST_INTERVAL = 100000;
    private static final double MIN_DISTANCE_CHANGE = 19312.1;

    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private Location currentLocation;
    private Location prevLocation;
    private LocationRequest locationRequest;
    private Retrofit retrofit;
    private Button btnIncreaseRadius;
    private Button btnDecreaseRadius;
    private TextView tvRadius;

    private Integer radius;
    private List<Farm> farms = new ArrayList<>();
    private List<String> farmIds = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<String> farmsInDatabase = new ArrayList<>();
    private LatLngBounds bounds;
    private RecyclerView rvProfiles;
    private MapProfilesAdapter profilesAdapter;
    private RecyclerView.SmoothScroller smoothScroller;
    private LinearLayoutManager linearLayoutManager;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(map -> {
            loadMap(map);
            map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        radius = ParseUser.getCurrentUser().getInt("radius");

        if (radius == 0) {
            ParseUser.getCurrentUser().put("radius", MAX_YELP_RADIUS);
            ParseUser.getCurrentUser().saveInBackground();
            radius = MAX_YELP_RADIUS;
        }

        btnIncreaseRadius = view.findViewById(R.id.btnIncreaseRadius);
        btnIncreaseRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // increase radius by 5 miles
                if (radius < MAX_YELP_RADIUS) //maximum radius allowed by yelp
                {
                    radius += YELP_RADIUS_INCREMENT;
                    ParseUser.getCurrentUser().put("radius", radius);
                    ParseUser.getCurrentUser().saveInBackground();
                }
                try {
                    displayLocation();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                tvRadius.setText(String.format(getContext().getString(R.string.radius_string), radius / METERS_TO_MILE));
            }
        });

        btnDecreaseRadius = view.findViewById(R.id.btnDecreaseRadius);
        btnDecreaseRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // decrease radius by 5 miles
                if (radius > YELP_RADIUS_INCREMENT) //min radius ~= 5 miles
                {
                    radius -= YELP_RADIUS_INCREMENT;
                    ParseUser.getCurrentUser().put("radius", radius);
                    ParseUser.getCurrentUser().saveInBackground();
                }
                //adjust the map accordingly-- have to remove out of range markers
                adjustRange();
                tvRadius.setText(String.format(getContext().getString(R.string.radius_string), radius / METERS_TO_MILE));
            }
        });

        tvRadius = view.findViewById(R.id.tvRadius);
        tvRadius.setText(String.format(getContext().getString(R.string.radius_string), radius / METERS_TO_MILE));

        rvProfiles = view.findViewById(R.id.rvProfiles);
        profilesAdapter = new MapProfilesAdapter(getContext(), farms);
        rvProfiles.setAdapter(profilesAdapter);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvProfiles.setLayoutManager(linearLayoutManager);

        smoothScroller = new CenterSmoothScroller(getContext());
    }

    protected void adjustRange() {
        List<Farm> nFarms = new ArrayList<>();
        List<Marker> nMarkers = new ArrayList<>();
        List<String> nFarmIds = new ArrayList<>();

        for (int i = 0; i < markers.size(); i++) // farms and their markers always be at the same index? as farms get removed / added so do their markers?
        {
            if (farms.get(i).getDistance() > radius) { // remove these markers from the map
                markers.get(i).remove();
            } else // keep these ones.
            {
                nFarms.add(farms.get(i));
                nMarkers.add(markers.get(i));
                nFarmIds.add(farmIds.get(i));
            }
        }

        farms.removeIf(farm -> !nFarms.contains(farm)); // maintain adapter list
        markers = nMarkers;
        farmIds = nFarmIds;
        profilesAdapter.notifyDataSetChanged(); // how to change this to be more specific?
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            getMyLocation();
            MapFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            MapFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressLint("PotentialBehaviorOverride")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        /*map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        getContext(), R.raw.style_json));*/

        map.setOnMarkerClickListener(marker -> {
            // scroll the adapter to the farm that has been clicked on
            for(int i = 0; i < farms.size(); i++) {
                if(farms.get(i) == marker.getTag()) {
                    smoothScroller.setTargetPosition(i);
                    linearLayoutManager.startSmoothScroll(smoothScroller);
                    farms.get(i).expanded = true;
                } else {
                    farms.get(i).expanded = false;
                }
            }
            profilesAdapter.notifyDataSetChanged();

            return true;
        });

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        try {
                            CameraUpdate point = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                            map.moveCamera(point);
                            onLocationChanged(location);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("MapFragment", "Error trying to get last GPS location");
                    e.printStackTrace();
                });
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        try {
                            onLocationChanged(locationResult.getLastLocation());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) throws ParseException {
        // GPS may be turned off
        if (location == null) {
            return;
        }
        if (prevLocation == null) { //prevlocation starts off as null--> initialize prevLocation and currentLocation to be the same
            prevLocation = location;
            currentLocation = location;
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            bounds = new LatLngBounds(latLng, latLng);

            displayLocation();
        } else if (prevLocation.distanceTo(location) > MIN_DISTANCE_CHANGE) { // location has changed by > 12 miles: set prevLocation to currentLocation and currentLocation to the new location and make request.
            prevLocation = currentLocation;
            currentLocation = location;
            displayLocation();
        }
    }

    private void displayLocation() throws ParseException {
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            map.animateCamera(cameraUpdate);
            getRequest(CATEGORY_FARM);
            getRequest(CATEGORY_FARMERS_MARKET);
        }
    }

    public Bitmap getMarker(String request) {
        IconGenerator iconGen = new IconGenerator(getContext());

        // Define the size from the dimensions file
        int shapeSize = this.getResources().getDimensionPixelSize(R.dimen.custom_marker_value);

        Drawable shapeDrawable = request.equals(CATEGORY_FARM) ? ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_farmhouse, null) : ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_baseline_storefront_24, null);
        iconGen.setBackground(shapeDrawable);

        // Create a view container to set the size
        View view = new View(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(shapeSize, shapeSize));
        iconGen.setContentView(view);

        // Create the bitmap
        return iconGen.makeIcon();
    }

    protected void dropMarkers(List<Farm> newFarms, String request) {
        for (Farm farm : newFarms) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(farm.getCoordinates())
                    .title(farm.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarker(request)))
            );
            assert marker != null;
            marker.setTag(farm); // associate farm --> marker
            markers.add(marker);
            bounds = bounds.including(farm.getCoordinates());
            map.setLatLngBoundsForCameraTarget(bounds);
        }
    }

    protected void getRequest(String request) throws ParseException {
        // first make request to Parse database to check for farms nearby
        populateDatabaseFarms(request);

        if(!(farms.size() > 0)) {
            // only make the request to Yelp if we don't have farms yet in the radius of the user's location.
            YelpService yelpService = retrofit.create(YelpService.class);
            Call<FarmSearchResult> call = yelpService.searchFarms("Bearer " + YELP_API_KEY, currentLocation.getLatitude(), currentLocation.getLongitude(), request, 50, radius);
            call.enqueue(new Callback<FarmSearchResult>() {
                @Override
                public void onResponse(@NonNull Call<FarmSearchResult> call, @NonNull Response<FarmSearchResult> response) {
                    Log.i(TAG, "Success! " + response);
                    if (response.body() == null) {
                        Log.e(TAG, "Error retrieving response body");
                    } else {
                        List<Farm> newFarms = new ArrayList<>();

                        for (Farm farm : response.body().getFarms()) {
                            if (!farmIds.contains(farm.getId()) && farm.getDistance() < radius) {
                                newFarms.add(farm);
                                farms.add(farm);
                                farmIds.add(farm.getId());
                                if (!farmsInDatabase.contains(farm.getId())) {
                                    farm.setUser(createUserFromYelpData(farm, request));
                                    farmsInDatabase.add(farm.getId());
                                    if(Objects.equals(farm.getImageUrl(), "")) {
                                        farm.setImageUrl(farm.getUser().getString(Farm.KEY_PROFILE_BACKDROP));
                                    }
                                } else {
                                    farm.setUser(findFarm(farm.getId()));
                                }
                            }
                        }
                        profilesAdapter.notifyItemRangeInserted(farms.size() - newFarms.size(), newFarms.size());
                        dropMarkers(newFarms, request);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FarmSearchResult> call, @NonNull Throwable t) {
                    Log.i(TAG, "Failure " + t);
                }
            });
        }
    }

    // populate the parse database with farm user
    protected ParseUser createUserFromYelpData(Farm farm, String request) {
        ParseUser user = new ParseUser();
        user.setUsername(farm.getId());
        user.setPassword(farm.getId());
        user.put(Farm.KEY_USER_TYPE, request);
        user.put(Farm.KEY_NAME, farm.getName());
        user.put(Farm.KEY_ADDRESS, farm.getLocation().getAddress1() + " " + farm.getLocation().getCity() + " " + farm.getLocation().getState());
        user.put(Farm.KEY_LOCATION, new ParseGeoPoint(farm.getCoordinates().latitude, farm.getCoordinates().longitude));
        if(!Objects.equals(farm.getImageUrl(), "")) { // use the default image instead
            user.put(Farm.KEY_PROFILE_BACKDROP, farm.getImageUrl());
        }
        user.put(Farm.KEY_BIO, "this farm has not yet created a bio.");
        user.put(Farm.KEY_YELP_ID, farm.getId());
        user.add(Farm.KEY_TAGS, farm.getId());
        user.signUpInBackground();
        return user;
    }

    protected ParseUser findFarm(String yelpID) {
        final ParseUser[] farm = new ParseUser[1];
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(Farm.KEY_YELP_ID, yelpID);
        query.include(Farm.KEY_BIO);
        query.findInBackground((objects, e) -> farm[0] = objects.get(0));
        return farm[0];
    }

    protected void populateDatabaseFarms(String request) throws ParseException {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(Farm.KEY_USER_TYPE, request);
        query.whereWithinMiles(Farm.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), radius/METERS_TO_MILE);

        List<ParseUser> databaseFarms = query.find(); // this cannot be in the background, or it makes the yelp request too quickly
        List<Farm> newFarms = new ArrayList<>();
        for(int i = 0; i < databaseFarms.size(); i++) {
            Farm farm = new Farm(databaseFarms.get(i));
            if(!farmIds.contains(farm.getUser().getString(Farm.KEY_YELP_ID))) {
                LatLng latLng = new LatLng(farm.getUser().getParseGeoPoint(Farm.KEY_LOCATION).getLatitude(), farm.getUser().getParseGeoPoint(Farm.KEY_LOCATION).getLongitude());
                farm.setCoordinates(latLng);
                Location location = new Location(NETWORK_PROVIDER);
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                farm.setDistance((double) currentLocation.distanceTo(location));
                farm.setName(farm.getUser().getString(Farm.KEY_NAME));
                farm.setImageUrl(farm.getUser().getString(Farm.KEY_PROFILE_BACKDROP));

                newFarms.add(farm);
                farms.add(farm);
                farmIds.add(farm.getUser().getString(Farm.KEY_YELP_ID));
            }
        }
        profilesAdapter.notifyItemRangeInserted(farms.size() - newFarms.size(), newFarms.size());
        dropMarkers(newFarms, request);
    }

    public class CenterSmoothScroller extends LinearSmoothScroller {

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }
    }
}