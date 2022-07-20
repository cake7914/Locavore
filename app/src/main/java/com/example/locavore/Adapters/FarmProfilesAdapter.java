package com.example.locavore.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.locavore.FarmsDiffCallback;
import com.example.locavore.Fragments.FarmProfileFragment;
import com.example.locavore.Fragments.FeedFragment;
import com.example.locavore.R;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locavore.Models.User;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.List;

public class FarmProfilesAdapter extends RecyclerView.Adapter<FarmProfilesAdapter.ViewHolder> {
    private Context context;
    private List<User> mFarms;
    public static final String TAG = "FarmProfilesAdapter";

    public FarmProfilesAdapter(Context context, List<User> farms) {
        this.context = context;
        this.mFarms = farms;
    }

    public void updateList(List <User> newFarms) {
        FarmsDiffCallback diffCallback = new FarmsDiffCallback(mFarms, newFarms);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        mFarms.clear();
        mFarms.addAll(newFarms);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farm_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User farm = mFarms.get(position);
        try {
            holder.bind(farm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mFarms.size();
    }

    public void clear() {
        int size = mFarms.size();
        mFarms.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<User> newFarms) {
        mFarms.addAll(newFarms);
        notifyItemRangeInserted(mFarms.size() - newFarms.size(), newFarms.size());
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

        public void bind(User farm) throws JSONException {
            String farmName = farm.getUser().getString(User.KEY_NAME);
            if(farmName.length() >= 28) {
                tvFarmName.setText(farmName.substring(0, 25) + "...");
            } else {
                tvFarmName.setText(farm.getUser().getString(User.KEY_NAME));
            }

            if(farm.getUser().getString(User.KEY_PROFILE_PHOTO) != null) {
                Glide.with(context).load(farm.getUser().getString(User.KEY_PROFILE_PHOTO)).circleCrop().into(ivProfileImage);
            } else {
                ivProfileImage.setImageBitmap(null);
            }

            if(checkUserFollowingFarm(farm.getUser().getObjectId(), ParseUser.getCurrentUser().getJSONArray(User.KEY_FARMS_FOLLOWING)) == -1) {
                btnFollow.setText(R.string.follow);
                btnFollow.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
            } else {
                btnFollow.setText(R.string.following);
                btnFollow.setBackgroundColor(context.getResources().getColor(R.color.dark_yellow));
            }

            btnFollow.setOnClickListener(v -> {
                ParseUser user = ParseUser.getCurrentUser();
                try {
                    // initializing params for cloudcode call
                    HashMap<String, String> params = new HashMap();
                    params.put("objectId", farm.getUser().getObjectId());
                    params.put("followerId", user.getObjectId());

                    int pos = checkUserFollowingFarm(farm.getUser().getObjectId(), user.getJSONArray(User.KEY_FARMS_FOLLOWING));
                    if(pos == -1) {
                        user.add(User.KEY_FARMS_FOLLOWING, farm.getUser().getObjectId());
                        btnFollow.setText(R.string.following);
                        btnFollow.setBackgroundColor(context.getResources().getColor(R.color.dark_yellow));
                        params.put("following", "true");
                    } else {
                        JSONArray farms = user.getJSONArray(User.KEY_FARMS_FOLLOWING);
                        assert farms != null;
                        farms.remove(pos);
                        user.put(User.KEY_FARMS_FOLLOWING, farms);
                        btnFollow.setText(R.string.follow);
                        btnFollow.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
                        params.put("following", "false");
                    }
                    ParseCloud.callFunctionInBackground("updateFollowers", params, (FunctionCallback<ParseObject>) (obj, e) -> {
                        if (e == null) {
                            Log.i(TAG, "non error");
                        }else{
                            Log.i(TAG, "error" + e.getMessage());
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.saveInBackground(e -> {
                    if (e != null) {
                        Log.i(TAG, "Something has happened", e);
                    } else {
                        Log.i(TAG, "Save successful");
                    }
                });
            });

            itemView.setOnClickListener(v -> {
                Fragment fragment = new FarmProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(User.FARM_USER_TYPE, Parcels.wrap(farm));
                fragment.setArguments(args);
                ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
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
