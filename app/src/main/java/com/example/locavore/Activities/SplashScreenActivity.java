package com.example.locavore.Activities;

import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.DataManager;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends Activity {

    public static final String TAG = "SplashScreenActivity";
    private Location currentLocation;
    private LocationManager locationManager;
    private String bestProvider;
    DataManager dataManager;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // need to request location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else { // already have permission
            loadLocation();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void loadLocation() {
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        locationManager.getCurrentLocation(
                bestProvider,
                null,
                getMainExecutor(),
                location -> {
                    currentLocation = location;
                    if (ParseUser.getCurrentUser() != null) {
                        dataManager = DataManager.getInstance(currentLocation);
                        new FarmFetcher().execute();
                    } else { // go straight to login screen. make request while map screen loading.
                        // can't do default radius because the user data is required to do event ordering
                        Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                        SplashScreenActivity.this.startActivity(i);
                        SplashScreenActivity.this.finish();
                    }
                });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) { // neither permission granted
            Toast.makeText(this, getString(R.string.location_permission), Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            // instead of requesting permissions again, go to login and when the map loads if we don't have permissions, display alert
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            loadLocation();
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
                dataManager.getFarms(currentLocation);
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