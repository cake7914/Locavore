package com.example.locavore.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.locavore.Models.User;
import com.example.locavore.Models.YelpLocation;
import com.example.locavore.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater mInflater;
    TextView tvFarmName;
    TextView tvFarmAddress;
    TextView tvFarmBio;
    User farm;
    YelpLocation location;

    public CustomWindowAdapter(LayoutInflater i){
        mInflater = i;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View v = mInflater.inflate(R.layout.activity_custom_window_adapter, null);
        farm = (User) marker.getTag();
        location = farm.getLocation();

        tvFarmName = (TextView) v.findViewById(R.id.tvFarmName);
        tvFarmName.setText(farm.getName());

        tvFarmAddress = (TextView) v.findViewById(R.id.tvAddress);
        tvFarmAddress.setText(location.getAddress1() + ", " + location.getCity() + ", " + location.getState());

        tvFarmBio = (TextView) v.findViewById(R.id.tvBio);


        return v;
    }
    // border and arrow surrounding the contents above
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

}