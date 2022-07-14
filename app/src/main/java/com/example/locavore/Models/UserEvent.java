package com.example.locavore.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("UserEvent")
public class UserEvent extends ParseObject {

    public static final String KEY_USER_ID = "userId";
    public static final String KEY_FARM_ID = "farmId";
    public static final String KEY_EVENT_ID = "eventId";
    public static final String KEY_LIKED = "liked";
    public static final int LIKED = 1;
    public static final int DISLIKED = -1;
    public static final int NEUTRAL = 0;

    public String getUserId() {
        return getString(KEY_USER_ID);
    }
    public void setUserId(String userId) {
        put(KEY_USER_ID, userId);
    }

    public String getFarmId() {
        return getString(KEY_FARM_ID);
    }
    public void setFarmId(String farmId) {
        put(KEY_FARM_ID, farmId);
    }

    public String getEventId() {
        return getString(KEY_EVENT_ID);
    }
    public void setEventId(String eventId) {
        put(KEY_EVENT_ID, eventId);
    }

    public int getLiked() {
        return getInt(KEY_LIKED);
    }
    public void setLiked(int liked) {
        put(KEY_LIKED, liked);
    }

}
