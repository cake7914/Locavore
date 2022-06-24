package com.example.locavore;

import android.app.Application;

import static com.example.locavore.BuildConfig.CLIENT_KEY;
import static com.example.locavore.BuildConfig.PARSE_APPLICATION_ID;

import com.example.locavore.Models.Event;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Event.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(PARSE_APPLICATION_ID)
                .clientKey(CLIENT_KEY)
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
