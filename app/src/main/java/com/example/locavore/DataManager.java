package com.example.locavore;

import static android.location.LocationManager.NETWORK_PROVIDER;

import android.location.Location;
import android.util.Log;

import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public static final String TAG = "DataManager";
    private static final double METERS_TO_MILE = 1609.34;
    private static DataManager sDataManager = null;

    public List<User> mFarms;
    public List<String> mFarmIds;
    public List<Event> mEvents;

    private DataManager() {
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

        if(ParseUser.getCurrentUser() != null) {
            query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), ParseUser.getCurrentUser().getDouble(User.KEY_RADIUS) / METERS_TO_MILE);
        } else {
            query.whereWithinMiles(User.KEY_LOCATION, new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), 40000 / METERS_TO_MILE);
        }

        List<ParseUser> databaseFarms = query.find();

        for (int i = 0; i < databaseFarms.size(); i++) {
            User farm = new User(databaseFarms.get(i), currentLocation);
            mFarms.add(farm);
            mFarmIds.add(farm.getId());

            // also add any events that this farm has to the events list
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
    }
}
