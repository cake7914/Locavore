package com.example.locavore.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.locavore.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locavore.Models.Farm;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class FarmProfilesAdapter extends RecyclerView.Adapter<FarmProfilesAdapter.ViewHolder> {
    private Context context;
    private List<Farm> farms;
    public static final String TAG = "FarmProfilesAdapter";

    public FarmProfilesAdapter(Context context, List<Farm> farms) {
        this.context = context;
        this.farms = farms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farm_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Farm farm = farms.get(position);
        try {
            holder.bind(farm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return farms.size();
    }

    public void clear() {
        int size = farms.size();
        farms.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<Farm> newFarms) {
        farms.addAll(newFarms);
        notifyItemRangeInserted(farms.size() - newFarms.size(), newFarms.size());
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFarmName;
        ImageView ivProfileImage;
        Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFarmName = itemView.findViewById(R.id.tvFarmName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }

        public void bind(Farm farm) throws JSONException {
            String farmName = farm.getUser().getString(Farm.KEY_NAME);
            if(farmName.length() >= 28) {
                tvFarmName.setText(farmName.substring(0, 25) + "...");
            } else {
                tvFarmName.setText(farm.getUser().getString(Farm.KEY_NAME));
            }

            if(farm.getUser().getString(Farm.KEY_PROFILE_PHOTO) != null) {
                Glide.with(context).load(farm.getUser().getString(Farm.KEY_PROFILE_PHOTO)).circleCrop().into(ivProfileImage);
            } else {
                ivProfileImage.setImageBitmap(null);
            }

            if(checkUserFollowingFarm(farm.getUser().getObjectId(), ParseUser.getCurrentUser().getJSONArray(Farm.KEY_FARMS_FOLLOWING)) == -1) {
                btnFollow.setText(R.string.follow);
                btnFollow.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
            } else {
                btnFollow.setText(R.string.following);
                btnFollow.setBackgroundColor(context.getResources().getColor(R.color.dark_yellow));
            }

            btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser user = ParseUser.getCurrentUser();
                    try {
                        int pos = checkUserFollowingFarm(farm.getUser().getObjectId(), user.getJSONArray(Farm.KEY_FARMS_FOLLOWING));
                        if(pos == -1) {
                            user.add(Farm.KEY_FARMS_FOLLOWING, farm.getUser().getObjectId());
                            btnFollow.setText(R.string.following);
                            btnFollow.setBackgroundColor(context.getResources().getColor(R.color.dark_yellow));
                        } else {
                            JSONArray farms = user.getJSONArray(Farm.KEY_FARMS_FOLLOWING);
                            assert farms != null;
                            farms.remove(pos);
                            user.put(Farm.KEY_FARMS_FOLLOWING, farms);
                            btnFollow.setText(R.string.follow);
                            btnFollow.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.i(TAG, "Something has happened", e);
                            } else {
                                Log.i(TAG, "Save successful");
                            }
                        }
                    });
                }
            });

        }
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

}
