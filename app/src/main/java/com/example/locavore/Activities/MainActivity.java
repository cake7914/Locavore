package com.example.locavore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.locavore.Fragments.FarmProfileFragment;
import com.example.locavore.Fragments.FeedFragment;
import com.example.locavore.Fragments.LocavoreProfileFragment;
import com.example.locavore.Fragments.MapFragment;
import com.example.locavore.Fragments.OrdersFragment;
import com.example.locavore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()) {// do something here
                case R.id.feed_screen: // go to compose screen
                    // do something here
                    fragment = new FeedFragment();
                    break;
                case R.id.map_screen: // go to my profile
                    // do something here
                    fragment = new MapFragment();
                    break;
                case R.id.order_screen:
                    fragment = new OrdersFragment();
                    break;
                case R.id.profile_screen:
                    if(Objects.equals(ParseUser.getCurrentUser().getString("userType"), "farms") || Objects.equals(ParseUser.getCurrentUser().getString("userType"), "farmersmarket")) {
                        fragment = new FarmProfileFragment();
                    } else {
                        fragment = new LocavoreProfileFragment();
                    }
                    break;
                default: // go to map
                    fragment = new MapFragment();
                    break;
            }
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            return true;
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.map_screen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.btnLogout) {
            ParseUser.logOutInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "Issue with logout", e);
                    Toast.makeText(MainActivity.this, "Issue with logout!", Toast.LENGTH_SHORT).show();
                } else {
                    //go to the login activity
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    Toast.makeText(MainActivity.this, "Success in logging out!", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}