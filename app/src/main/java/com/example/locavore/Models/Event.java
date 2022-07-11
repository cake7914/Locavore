package com.example.locavore.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Event")
public class Event extends ParseObject implements Comparable<Event> {

    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_FARM = "farm";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_PHOTO = "photo";
    public int mWeight;


    @Override
    public int compareTo(Event event) {
        return Integer.compare(mWeight, event.mWeight);
    }

    public String getName() {
        return getString(KEY_NAME);
    }
    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }
    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getFarm() {
        return getString(KEY_FARM);
    }
    public void setFarm(String farm) {
        put(KEY_FARM, farm);
    }

    public String getLocation() {
        return getString(KEY_LOCATION);
    }
    public void setLocation(String location) {
        put(KEY_LOCATION, location);
    }

    public ParseFile getPhoto() { return getParseFile(KEY_PHOTO); }
    public void setPhoto(ParseFile photo) { put(KEY_PHOTO, photo); }
}
