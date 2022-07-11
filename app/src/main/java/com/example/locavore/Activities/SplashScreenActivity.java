package com.example.locavore.Activities;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.DataManager;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends Activity {

    public static final String TAG = "SplashScreenActivity";
    private Location currentLocation;
    private LocationManager locationManager;
    private String bestProvider;
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, false);
        currentLocation = locationManager.getLastKnownLocation(bestProvider);

        if(ParseUser.getCurrentUser() != null) { // user is logged in
            dataManager = DataManager.getInstance();
            new FarmFetcher().execute();
        } else { // go straight to login screen
            Log.i(TAG, "no user logged in");
            Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
            SplashScreenActivity.this.startActivity(i);
            SplashScreenActivity.this.finish();
        }
    }

    private class FarmFetcher extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { // keep showing splash screen
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) { // find all of the farms and save them in the singleton instance
            try {
                dataManager.queryFarms(User.FARM_USER_TYPE, currentLocation);
                dataManager.queryFarms(User.FARMERS_MARKET_USER_TYPE, currentLocation);

                // make the yelp call when the app first loads to check for any new farms nearby
                dataManager.yelpRequest(User.FARM_USER_TYPE, currentLocation);
                dataManager.yelpRequest(User.FARMERS_MARKET_USER_TYPE, currentLocation);

            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) { // go to main activity
            // super.onPostExecute(unused);
            Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
            SplashScreenActivity.this.startActivity(i);
            SplashScreenActivity.this.finish();
        }
    }
}