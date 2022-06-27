package com.example.locavore.Models;

import com.parse.ParseUser;

public class MyUser extends ParseUser {

    public static final String KEY_USER_TYPE = "userType";
    public static final String KEY_NAME = "name";
    public static final String KEY_PUSH_NOTIFS_ENABLED = "pushNotifsEnabled";
    public static final String KEY_BIO = "bio";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_RADIUS = "radius";

    public void setUserType(String userType) {
        put(KEY_USER_TYPE, userType);
    }


}
