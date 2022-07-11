package com.example.locavore.Fragments;

import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.example.locavore.BuildConfig.YELP_API_KEY;
import static com.example.locavore.DataManager.BASE_URL;
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
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.Models.FarmSearchResult;
import com.example.locavore.DataManager;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
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
    private static final int MAX_YELP_RADIUS = 40000; // 40,000 meters or ~25 miles
    private static final int YELP_RADIUS_INCREMENT = 8000; // ~ 5 miles
    private static final double METERS_TO_MILE = 1609.34;
    private static final long UPDATE_INTERVAL = 3000;
    private static final long FASTEST_INTERVAL = 3000;
    private static final double MIN_DISTANCE_CHANGE = 19312.1;
    private boolean firstLoad = true;

    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private Location currentLocation;
    private Location prevLocation;
    private LocationRequest locationRequest;
    private Button btnIncreaseRadius;
    private Button btnDecreaseRadius;
    private TextView tvRadius;
    private List<Marker> markers = new ArrayList<>();
    private LatLngBounds bounds;
    private RecyclerView rvProfiles;
    private MapProfilesAdapter profilesAdapter;
    private RecyclerView.SmoothScroller smoothScroller;
    private LinearLayoutManager linearLayoutManager;
    DataManager dataManager = DataManager.getInstance();

    public MapFragment() {
        // Required empty public constructor
    }

    public MapProfilesAdapter getProfilesAdapter() {
        return profilesAdapter;
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

        btnIncreaseRadius = view.findViewById(R.id.btnIncreaseRadius);
        btnIncreaseRadius.setOnClickListener(v -> {
            // increase radius by 5 miles
            if (dataManager.mRadius < MAX_YELP_RADIUS) //maximum radius allowed by yelp
            {
                dataManager.mRadius += YELP_RADIUS_INCREMENT;
                ParseUser.getCurrentUser().put(User.KEY_RADIUS, dataManager.mRadius);
                ParseUser.getCurrentUser().saveInBackground();
            }
            try {
                displayLocation();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            tvRadius.setText(String.format(getContext().getString(R.string.radius_string), dataManager.mRadius / METERS_TO_MILE));
        });

        btnDecreaseRadius = view.findViewById(R.id.btnDecreaseRadius);
        btnDecreaseRadius.setOnClickListener(v -> {
            // decrease radius by 5 miles
            if (dataManager.mRadius > YELP_RADIUS_INCREMENT) //min radius ~= 5 miles
            {
                dataManager.mRadius -= YELP_RADIUS_INCREMENT;
                ParseUser.getCurrentUser().put(User.KEY_RADIUS, dataManager.mRadius);
                ParseUser.getCurrentUser().saveInBackground();
            }
            //adjust the map accordingly-- have to remove out of range markers
            adjustRange();
            tvRadius.setText(String.format(getContext().getString(R.string.radius_string), dataManager.mRadius / METERS_TO_MILE));
        });

        tvRadius = view.findViewById(R.id.tvRadius);
        tvRadius.setText(String.format(getContext().getString(R.string.radius_string), dataManager.mRadius / METERS_TO_MILE));

        rvProfiles = view.findViewById(R.id.rvProfiles);
        profilesAdapter = new MapProfilesAdapter(getContext(), dataManager.mFarms);
        rvProfiles.setAdapter(profilesAdapter);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvProfiles.setLayoutManager(linearLayoutManager);

        smoothScroller = new CenterSmoothScroller(getContext());
    }

    protected void adjustRange() {
        List<User> nFarms = new ArrayList<>();
        List<Marker> nMarkers = new ArrayList<>();
        List<String> nFarmIds = new ArrayList<>();

        for (int i = 0; i < markers.size(); i++) // farms and their markers always be at the same index? as farms get removed / added so do their markers?
        {
            if(((User) markers.get(i).getTag()).getDistance() > dataManager.mRadius) { // remove these markers from the map
                markers.get(i).remove();
            } else // keep these ones.
            {
                nFarms.add((User) markers.get(i).getTag());
                nMarkers.add(markers.get(i));
                nFarmIds.add(((User) markers.get(i).getTag()).getId());
            }
        }

        dataManager.mFarms.removeIf(farm -> !nFarms.contains(farm)); // maintain adapter list
        markers = nMarkers;
        dataManager.mFarmIds = nFarmIds;
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
            for(int i = 0; i < dataManager.mFarms.size(); i++) {
                if(dataManager.mFarms.get(i) == marker.getTag()) {
                    smoothScroller.setTargetPosition(i);
                    linearLayoutManager.startSmoothScroll(smoothScroller);
                    dataManager.mFarms.get(i).expanded = true;
                } else {
                    dataManager.mFarms.get(i).expanded = false;
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
                            CameraUpdate point = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
                            map.moveCamera(point);
                            onLocationChanged(location);
                        } catch (ParseException | IOException e) {
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
                        } catch (ParseException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) throws ParseException, IOException {
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
        } else if (prevLocation.distanceTo(location) > MIN_DISTANCE_CHANGE) { // location has changed by significant amount: set prevLocation to currentLocation and currentLocation to the new location and make request.
            prevLocation = currentLocation;
            currentLocation = location;
            displayLocation();
        }
    }

    private void displayLocation() throws ParseException, IOException {
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            map.animateCamera(cameraUpdate);
            if(firstLoad && !dataManager.mFarms.isEmpty()) {
                dropMarkers(dataManager.mFarms);
            } else {
                getRequest(User.FARM_USER_TYPE);
                getRequest(User.FARMERS_MARKET_USER_TYPE);
            }
            firstLoad = false;
        }
    }

    public Bitmap getMarker(String request) {
        IconGenerator iconGen = new IconGenerator(getContext());

        // Define the size from the dimensions file
        int shapeSize = this.getResources().getDimensionPixelSize(R.dimen.custom_marker_value);

        Drawable shapeDrawable = request.equals(User.FARM_USER_TYPE) ? ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_farmhouse, null) : ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_baseline_storefront_24, null);
        iconGen.setBackground(shapeDrawable);

        // Create a view container to set the size
        View view = new View(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(shapeSize, shapeSize));
        iconGen.setContentView(view);

        // Create the bitmap
        return iconGen.makeIcon();
    }

    public void dropMarkers(List<User> newFarms) {
        for (User farm : newFarms) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(farm.getCoordinates())
                    .title(farm.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarker(farm.getUser().getString(User.KEY_USER_TYPE))))
            );
            assert marker != null;
            marker.setTag(farm); // associate farm --> marker
            markers.add(marker);
            bounds = bounds.including(farm.getCoordinates());
            map.setLatLngBoundsForCameraTarget(bounds);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
    }

    protected void getRequest(String request) throws ParseException, IOException {
        // first make request to Parse database to check for farms nearby
        queryFarms(request, currentLocation);

        // then, only make the yelp request if we have no farms in this area
        if(!(dataManager.mFarms.size() > 0)) {
            // only make the request to Yelp if we don't have farms yet in the radius of the user's location.
            yelpRequest(request, currentLocation);
        }
    }

    /* requests have to be made from within the class in order to maintain adapters... */

    public void queryFarms(String request, Location currentLocation) throws ParseException {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.KEY_USER_TYPE, request);

        if(ParseUser.getCurrentUser() != null && currentLocation != null) {
            query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), dataManager.mRadius / METERS_TO_MILE);
        } else { // situation where the user has not logged in yet. TODO: should show go straight to login, show the loading splash screen after logging in.
            query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), MAX_YELP_RADIUS / METERS_TO_MILE);
        }

        List<User> newFarms = new ArrayList<>();

        List<ParseUser> databaseFarms = query.find();
        for (int i = 0; i < databaseFarms.size(); i++) {
            if(!dataManager.mFarmIds.contains(databaseFarms.get(i).getString(User.KEY_YELP_ID))) {
                User farm = new User(databaseFarms.get(i), currentLocation);
                dataManager.mFarms.add(farm);
                dataManager.mFarmIds.add(farm.getId());
                newFarms.add(farm);
                // also add any events that this farm has to the events list
                //queryEvents(farm);
            }
        }
        profilesAdapter.notifyItemRangeInserted(dataManager.mFarms.size() - newFarms.size(), newFarms.size());
        dropMarkers(newFarms);
    }

    public void yelpRequest(String request, Location currentLocation) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        YelpService yelpService = retrofit.create(YelpService.class);
        Call<FarmSearchResult> call = yelpService.searchFarms("Bearer " + YELP_API_KEY, currentLocation.getLatitude(), currentLocation.getLongitude(), request, 50, MAX_YELP_RADIUS);
        List<User> newFarms = new ArrayList<>();

        call.enqueue(new Callback<FarmSearchResult>() {
            @Override
            public void onResponse(@NonNull Call<FarmSearchResult> call, @NonNull Response<FarmSearchResult> response) {
                Log.i(TAG, "Success! " + response);
                if (response.body() == null) {
                    Log.e(TAG, "Error retrieving response body");
                } else {
                    for (User farm : response.body().getFarms()) {
                        if (!dataManager.mFarmIds.contains(farm.getId())) {
                            ParseUser user = dataManager.createUserFromYelpData(farm, request);
                            if(farm.getDistance() < ParseUser.getCurrentUser().getInt(User.KEY_RADIUS)) {
                                newFarms.add(farm);
                                dataManager.mFarms.add(farm);
                                dataManager.mFarmIds.add(farm.getId());
                                farm.setUser(user);
                                if (Objects.equals(farm.getImageUrl(), "")) {
                                    farm.setImageUrl(farm.getUser().getString(User.KEY_PROFILE_BACKDROP));
                                }
                            }
                        }
                    }
                    profilesAdapter.notifyItemRangeInserted(dataManager.mFarms.size() - newFarms.size(), newFarms.size());
                    dropMarkers(newFarms);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FarmSearchResult> call, @NonNull Throwable t) {
                Log.i(TAG, "Failure " + t);
            }
        });
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