package com.example.locavore;

import static android.location.LocationManager.NETWORK_PROVIDER;

import static com.example.locavore.BuildConfig.YELP_API_KEY;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Adapter;

import androidx.annotation.NonNull;

import com.example.locavore.Activities.LoginActivity;
import com.example.locavore.Activities.SplashScreenActivity;
import com.example.locavore.Fragments.MapFragment;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.FarmSearchResult;
import com.example.locavore.Models.User;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.Parse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataManager {
    public static final String TAG = "DataManager";
    public static final String BASE_URL = "https://api.yelp.com/v3/";
    private static final int MAX_YELP_RADIUS = 40000; // 40,000 meters or ~25 miles
    private static final double METERS_TO_MILE = 1609.34;
    private static DataManager sDataManager = null;

    public List<User> mFarms;
    public List<String> mFarmIds;
    public List<Event> mEvents;
    public int mRadius;

    private DataManager() {
        if(ParseUser.getCurrentUser() != null) {
            mRadius = ParseUser.getCurrentUser().getInt(User.KEY_RADIUS);
        }
        mFarms = new ArrayList<>();
        mFarmIds = new ArrayList<>();
        mEvents = new ArrayList<>();
    }

    public static DataManager getInstance()
    {
        if (sDataManager == null) { // initialize
            sDataManager = new DataManager();
        }
        return sDataManager;
    }

    public void queryFarms(String request, Location currentLocation) throws ParseException {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.KEY_USER_TYPE, request);
        query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), mRadius / METERS_TO_MILE);

        List<User> newFarms = new ArrayList<>();

        List<ParseUser> databaseFarms = query.find();
        for (int i = 0; i < databaseFarms.size(); i++) {
            if(!mFarmIds.contains(databaseFarms.get(i).getString(User.KEY_YELP_ID))) {
                User farm = new User(databaseFarms.get(i), currentLocation);
                mFarms.add(farm);
                mFarmIds.add(farm.getId());
                newFarms.add(farm);
                // also add any events that this farm has to the events list
                queryEvents(farm);
            }
        }
    }

    private void queryEvents(User farm) {
        JSONArray newEvents = farm.getUser().getJSONArray(User.KEY_EVENTS);
        if (newEvents != null) {
            for (int j = 0; j < newEvents.length(); j++) {
                try {
                    String eventId = newEvents.getJSONObject(j).getString("objectId");
                    ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
                    eventQuery.getInBackground(eventId, (event, err) -> {
                        if (err != null) {
                            Log.e(TAG, "Issue with getting event ", err);
                        } else {
                            // add this event to the events list
                            mEvents.add((Event) event);
                        }
                    });

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void yelpRequest(String request, Location currentLocation) throws ParseException, IOException {
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
                        if (!mFarmIds.contains(farm.getId())) {
                            ParseUser user = createUserFromYelpData(farm, request);
                            if(farm.getDistance() < mRadius) {
                                newFarms.add(farm);
                                mFarms.add(farm);
                                mFarmIds.add(farm.getId());
                                farm.setUser(user);
                                if (Objects.equals(farm.getImageUrl(), "")) {
                                    farm.setImageUrl(farm.getUser().getString(User.KEY_PROFILE_BACKDROP));
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FarmSearchResult> call, @NonNull Throwable t) {
                Log.i(TAG, "Failure " + t);
            }
        });
    }

    // populate the parse database with farm user
    public ParseUser createUserFromYelpData(User farm, String request) {
        ParseUser user = new ParseUser();
        user.setUsername(farm.getId());
        user.setPassword(farm.getId());
        user.put(User.KEY_USER_TYPE, request);
        user.put(User.KEY_NAME, farm.getName());
        user.put(User.KEY_ADDRESS, farm.getLocation().getAddress1() + " " + farm.getLocation().getCity() + " " + farm.getLocation().getState());
        user.put(User.KEY_LOCATION, new ParseGeoPoint(farm.getCoordinates().latitude, farm.getCoordinates().longitude));
        if(!Objects.equals(farm.getImageUrl(), "")) { // use the default image instead
            user.put(User.KEY_PROFILE_BACKDROP, farm.getImageUrl());
        }
        user.put(User.KEY_BIO, "this farm has not yet created a bio.");
        user.put(User.KEY_YELP_ID, farm.getId());
        user.add(User.KEY_TAGS, farm.getId());
        user.signUpInBackground();
        return user;
    }
}
