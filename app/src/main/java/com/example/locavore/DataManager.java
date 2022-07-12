package com.example.locavore;

import static android.location.LocationManager.NETWORK_PROVIDER;

import static com.example.locavore.BuildConfig.YELP_API_KEY;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.locavore.Models.Event;
import com.example.locavore.Models.FarmSearchResult;
import com.example.locavore.Models.User;
import com.example.locavore.Models.UserEvent;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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

    public void getFarms(Location currentLocation) throws ParseException, IOException {
        // loop through all of the farms & check if they are within the user's radius still. if not, remove them.
        if(mFarms.size() != 0) {
            List<User> nFarms = new ArrayList<>();
            List<String> nFarmIds = new ArrayList<>();

            for (int i = 0; i < mFarms.size(); i++) {
                Location farmLocation = new Location(NETWORK_PROVIDER);
                farmLocation.setLatitude(mFarms.get(i).getCoordinates().latitude);
                farmLocation.setLongitude(mFarms.get(i).getCoordinates().longitude);
                if (currentLocation.distanceTo(farmLocation)<= mRadius) {
                    nFarms.add(mFarms.get(i));
                    nFarmIds.add(mFarms.get(i).getId());
                }
            }

            mFarms.removeIf(farm -> !nFarms.contains(farm));
            mFarmIds = nFarmIds;
        }
        queryFarms(User.FARM_USER_TYPE, currentLocation);
        queryFarms(User.FARMERS_MARKET_USER_TYPE, currentLocation);

        if(mFarms.size() != 0)
            return;

        yelpRequest(User.FARM_USER_TYPE, currentLocation);
        yelpRequest(User.FARMERS_MARKET_USER_TYPE, currentLocation);
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
                queryEvents(farm, currentLocation);
            }
        }
    }

    private void queryEvents(User farm, Location currentLocation) {
        JSONArray newEvents = farm.getUser().getJSONArray(User.KEY_EVENTS);
        if (newEvents != null) {
            for (int j = 0; j < newEvents.length(); j++) {
                try {
                    String eventId = newEvents.getString(j);
                    ParseQuery<Event> eventQuery = ParseQuery.getQuery("Event");
                    eventQuery.getInBackground(eventId, (event, err) -> {
                        if (err != null) {
                            Log.e(TAG, "Issue with getting event ", err);
                        } else {
                            // weight this event, then insert it into the events list based on its weight
                            try {
                                event.mWeight = weightEvent(event, currentLocation, farm);
                                insertEvent(event);
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void insertEvent(Event event) {
        if(mEvents.size() == 0) {
            mEvents.add(event);
        } else {
            for(int i = 0; i < mEvents.size(); i++) {
                if(event.mWeight > mEvents.get(i).mWeight) { // add in front
                    mEvents.add(i, event);
                    return;
                }
            } // case where event has the least weight & must be added last
            mEvents.add(event);
        }
    }

    public int weightEvent(Event event, Location currentLocation, User farm) throws JSONException, ParseException {
        int weight = 0;

        // calculate distance: subtract from total weight (want the highest weight to be the first shown)
        Location eventLocation = new Location(NETWORK_PROVIDER);
        eventLocation.setLatitude(event.getParseGeoPoint(Event.KEY_LOCATION).getLatitude());
        eventLocation.setLongitude(event.getParseGeoPoint(Event.KEY_LOCATION).getLongitude());
        weight -= currentLocation.distanceTo(eventLocation);

        // if the user follows the farm
        if(checkUserFollowingFarm(event.getFarm(), ParseUser.getCurrentUser().getJSONArray(User.KEY_FARMS_FOLLOWING)) != -1) {
            weight += 500;
        }

        // if the user has attended an event at the farm before & liked it
        weight = checkAttendedEvents(event, currentLocation, weight);

        // factor in farm's yelp rating
        weight += farm.getRating() * 100;

        // factor in farm's number of followers
        JSONArray followers = farm.getUser().getJSONArray(User.KEY_FOLLOWERS);
        if(followers != null)
        {
            weight += (followers.length() * 100);
        }

        // factor in how many users have liked this event
        weight = quantityUsersLiked(event, currentLocation, weight);

        return weight;
    }

    protected int quantityUsersLiked(Event event, Location currentLocation, int weight) throws ParseException {
        ParseQuery<UserEvent> query = ParseQuery.getQuery("UserEvent");
        query.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
        query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), mRadius / METERS_TO_MILE);

        List<UserEvent> userEvents;

        userEvents = query.find();
        for (int i = 0; i < userEvents.size(); i++) {
            if(userEvents.get(i).getLiked()) {
                weight += 25;
            } else if(!userEvents.get(i).getLiked()) {
                weight -= 25;
            }
        }
        return weight;
    }

    protected int checkAttendedEvents(Event event, Location currentLocation, int weight) throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();

        ParseQuery<UserEvent> query = ParseQuery.getQuery("UserEvent");
        query.whereEqualTo(UserEvent.KEY_USER_ID, user.getObjectId());
        query.whereEqualTo(UserEvent.KEY_FARM_ID, event.getFarm());
        query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), mRadius / METERS_TO_MILE);

        List<UserEvent> userEvents;

        userEvents = query.find();
        for (int i = 0; i < userEvents.size(); i++) {
            if(userEvents.get(i).getLiked()) {
                weight += 100;
            } else if(!userEvents.get(i).getLiked()) {
                weight -= 100;
            }
        }
        return weight;
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
        user.put(User.KEY_BIO, farm.getName() + " is located at " + farm.getLocation().getAddress1() + " " + farm.getLocation().getCity() + " " + farm.getLocation().getState());
        user.put(User.KEY_YELP_ID, farm.getId());
        user.put(User.KEY_RATING, farm.getRating());
        user.add(User.KEY_TAGS, farm.getId());
        user.signUpInBackground();
        return user;
    }
}
