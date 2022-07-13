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
import java.util.function.Predicate;

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
    DataManager dataManager = DataManager.getInstance(currentLocation);
    private List<User> mFarms = dataManager.mFarms;

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
            try {
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                bounds = new LatLngBounds(latLng, latLng);
                displayLocation();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            tvRadius.setText(String.format(getContext().getString(R.string.radius_string), dataManager.mRadius / METERS_TO_MILE));
        });

        tvRadius = view.findViewById(R.id.tvRadius);
        tvRadius.setText(String.format(getContext().getString(R.string.radius_string), dataManager.mRadius / METERS_TO_MILE));

        rvProfiles = view.findViewById(R.id.rvProfiles);
        profilesAdapter = new MapProfilesAdapter(getContext(), mFarms);
        rvProfiles.setAdapter(profilesAdapter);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvProfiles.setLayoutManager(linearLayoutManager);

        smoothScroller = new CenterSmoothScroller(getContext());
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
            for(int i = 0; i < mFarms.size(); i++) {
                if(mFarms.get(i) == marker.getTag()) {
                    smoothScroller.setTargetPosition(i);
                    linearLayoutManager.startSmoothScroll(smoothScroller);
                    mFarms.get(i).expanded = true;
                    profilesAdapter.notifyItemChanged(i);
                } else if (mFarms.get(i).expanded){
                    mFarms.get(i).expanded = false;
                    profilesAdapter.notifyItemChanged(i);
                }
            }
            return true;
        });

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        try {
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
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            bounds = new LatLngBounds(latLng, latLng);
            displayLocation();
        }
    }

    private void displayLocation() throws ParseException, IOException {
        if (currentLocation != null) {
            boolean firstLoad = markers.size() == 0;

            map.clear();
            markers = new ArrayList<>();

            dataManager.getFarms(currentLocation);
            compareInstances();
            dropMarkers(mFarms, firstLoad);
        }
    }

    private void compareInstances() {
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

    public void dropMarkers(List<User> newFarms, boolean firstLoad) {
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
        }
        // if this is the first load, then moveCamera rather than animateCamera
        if(firstLoad)
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        else
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
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