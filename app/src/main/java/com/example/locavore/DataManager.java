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
import com.example.locavore.Models.YelpCategory;
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
    public Location mLocation;
    public UpdateResponse updateResponse;

    private DataManager(Location currentLocation) {
        if(ParseUser.getCurrentUser() != null) {
            mRadius = ParseUser.getCurrentUser().getInt(User.KEY_RADIUS);
        }
        mFarms = new ArrayList<>();
        mFarmIds = new ArrayList<>();
        mEvents = new ArrayList<>();
        mLocation = currentLocation;
    }

    public static DataManager getInstance(Location currentLocation)
    {
        if (sDataManager == null) { // initialize
            sDataManager = new DataManager(currentLocation);
        }
        return sDataManager;
    }

    public interface UpdateResponse {
        void onUpdate(List<User> farms, List<Event> events);
    }

    public void getFarms(Location currentLocation, int radius) throws ParseException, IOException {
        if(currentLocation != null) {
            mLocation = currentLocation;
        }

        if (mFarms.size() != 0) { // if we have saved farms, remove any & their events that are no longer relevant based on the user's location
            Log.i(TAG, "saved farms exist!");

            for (int i = mFarms.size()-1; i >=0; i--) {
                Location farmLocation = new Location(NETWORK_PROVIDER);
                farmLocation.setLatitude(mFarms.get(i).getCoordinates().latitude);
                farmLocation.setLongitude(mFarms.get(i).getCoordinates().longitude);
                if (mLocation.distanceTo(farmLocation) > radius) {
                    mFarms.remove(i);
                    mFarmIds.remove(i);
                }
            }
            mEvents.removeIf(event -> !mFarmIds.contains(event.getFarm()));
        }

        // if we have no farms
        if(mFarms.size() == 0)
        {
            queryFarms(User.FARM_USER_TYPE, mLocation);
            queryFarms(User.FARMERS_MARKET_USER_TYPE, mLocation);
        }

        // if the radius has increased, query the database.
        if(mRadius < radius) {
            mRadius = radius;
            queryFarms(User.FARM_USER_TYPE, mLocation);
            queryFarms(User.FARMERS_MARKET_USER_TYPE, mLocation);
        } else if(mRadius > radius){ //otherwise, just decrease mRadius
            mRadius = radius;
        }

        // only make the yelp request if necessary-- if we have very few saved farms in this radius
        if(mFarms.size() <= 5) {
            yelpRequest(User.FARM_USER_TYPE, mLocation);
            yelpRequest(User.FARMERS_MARKET_USER_TYPE, mLocation);
        }
    }

    public void queryFarms(String request, Location currentLocation) throws ParseException {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.KEY_USER_TYPE, request);
        query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), mRadius / METERS_TO_MILE);

        List<ParseUser> databaseFarms = query.find();
        for (int i = 0; i < databaseFarms.size(); i++) {
            if(!mFarmIds.contains(databaseFarms.get(i).getString(User.KEY_YELP_ID))) {
                User farm = new User(databaseFarms.get(i), currentLocation);
                mFarms.add(farm);
                mFarmIds.add(farm.getId());
                queryEvents(farm, currentLocation);
            }
        }

        mergeSort(mEvents, mEvents.size());
        for(int i = 0; i < mEvents.size(); i++)
            Log.i(TAG, i + " " + mEvents.get(i).getName() + ": " + mEvents.get(i).mWeight);

        if(updateResponse != null)
            updateResponse.onUpdate(mFarms, mEvents);
    }

    private void queryEvents(User farm, Location currentLocation) {
        JSONArray newEvents = farm.getUser().getJSONArray(User.KEY_EVENTS);
        if (newEvents != null) {
            for (int j = 0; j < newEvents.length(); j++) {
                try {
                    String eventId = newEvents.getString(j);
                    ParseQuery<Event> eventQuery = ParseQuery.getQuery("Event");
                    Event event = eventQuery.get(eventId);
                    event.mWeight = weightEvent(event, currentLocation, farm);
                    mEvents.add(event);
                } catch (JSONException | ParseException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void insertEvent(Event event) {
        if (mEvents.size() != 0) {
            for (int i = 0; i < mEvents.size(); i++) {
                if (event.mWeight > mEvents.get(i).mWeight) { // add in front
                    mEvents.add(i, event);
                    return;
                }
            }
        }
        // at the end
        mEvents.add(event);
    }

    public void addEvent(Event event) {
        insertEvent(event);
        if(updateResponse != null)
            updateResponse.onUpdate(mFarms, mEvents);
    }

    public static void mergeSort(List<Event> events, int size) {
        if(size < 2) {
            return;
        }

        int mid = size / 2;
        List<Event> left = new ArrayList<>(mid);
        List<Event> right = new ArrayList<>(size-mid);

        for (int i = 0; i < mid; i++) {
            left.add(i, events.get(i));
        }
        for (int i = mid; i < size; i++) {
            right.add(i - mid, events.get(i));
        }
        mergeSort(left, mid);
        mergeSort(right, size - mid);

        merge(events, left, right, mid, size - mid);
    }

    public static void merge(List<Event> events, List<Event> left, List<Event> right, int leftIndex, int rightIndex) {
        int i = 0, j = 0, k = 0;
        while (i < leftIndex && j < rightIndex) {
            if (left.get(i).mWeight >= right.get(j).mWeight)
                events.set(k++, left.get(i++));
            else
                events.set(k++, right.get(j++));
        }
        while (i < leftIndex)
            events.set(k++, left.get(i++));
        while (j < rightIndex)
            events.set(k++, right.get(j++));
    }

    public int weightEvent(Event event, Location currentLocation, User farm) throws JSONException, ParseException {
        int weight = 0;

        // calculate distance: subtract from total weight (want the highest weight to be the first shown)
        Location eventLocation = new Location(NETWORK_PROVIDER);
        eventLocation.setLatitude(event.getLocation().getLatitude());
        eventLocation.setLongitude(event.getLocation().getLongitude());
        weight -= (currentLocation.distanceTo(eventLocation) / METERS_TO_MILE);

        // if the user follows the farm, add a lot of weight
        if(checkUserFollowingFarm(event.getFarm(), ParseUser.getCurrentUser().getJSONArray(User.KEY_FARMS_FOLLOWING)) != -1) {
            weight += 500;
        }

        // if the user has attended an event at the farm before, add 100 for each liked event, and subtract 100 for each disliked
        weight = checkAttendedEvents(event, weight);

        // factor in farm's yelp rating--> multiply by factor of 50
        weight += farm.getRating() * 50;

        // factor in farm's number of followers--> 10 for each follower
        JSONArray followers = farm.getUser().getJSONArray(User.KEY_FOLLOWERS);
        if(followers != null) {
            weight += (followers.length() * 10);
        }

        // factor in how many users have liked/disliked this event--> +-25 for each user.
        weight = quantityUsersLiked(event, weight);

        return weight;
    }

    protected int quantityUsersLiked(Event event, int weight) throws ParseException {
        ParseQuery<UserEvent> query = ParseQuery.getQuery("UserEvent");
        query.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());

        List<UserEvent> userEvents;

        userEvents = query.find(); //find in background?
        for (int i = 0; i < userEvents.size(); i++) {
            if(userEvents.get(i).getLiked() == UserEvent.LIKED) {
                weight += 25;
            } else if(userEvents.get(i).getLiked() == UserEvent.DISLIKED) {
                weight -= 25;
            }
        }
        return weight;
    }

    protected int checkAttendedEvents(Event event, int weight) throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();

        ParseQuery<UserEvent> query = ParseQuery.getQuery("UserEvent");
        query.whereEqualTo(UserEvent.KEY_USER_ID, user.getObjectId());
        query.whereEqualTo(UserEvent.KEY_FARM_ID, event.getFarm());

        List<UserEvent> userEvents = query.find(); //findinbackground?
        for (int i = 0; i < userEvents.size(); i++) {
            if(((UserEvent)userEvents.get(i)).getLiked() == UserEvent.LIKED) {
                weight += 100;
            } else if(((UserEvent)userEvents.get(i)).getLiked() == UserEvent.DISLIKED) {
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

    public void yelpRequest(String request, Location currentLocation) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        YelpService yelpService = retrofit.create(YelpService.class);

        Call<FarmSearchResult> call = yelpService.searchFarms("Bearer " + YELP_API_KEY, currentLocation.getLatitude(), currentLocation.getLongitude(), request, 50, MAX_YELP_RADIUS);

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
        String sessionToken = ParseUser.getCurrentSessionToken();

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
        user.put(User.KEY_PHONE, farm.getPhone());
        user.put(User.KEY_REVIEW_COUNT, farm.getReviewCount());
        for(YelpCategory category : farm.getCategories())
            user.add(User.KEY_TAGS, category.getTitle());
        user.signUpInBackground(e -> {
            try {
                ParseUser.become(sessionToken);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });
        return user;
    }
}
