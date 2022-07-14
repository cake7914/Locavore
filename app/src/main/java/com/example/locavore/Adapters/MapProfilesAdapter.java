package com.example.locavore.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.Activities.MainActivity;
import com.example.locavore.Fragments.FarmProfileFragment;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class MapProfilesAdapter extends RecyclerView.Adapter<MapProfilesAdapter.ViewHolder> {
    private static final double METERS_TO_MILE = 1609.34;
    public static final String TAG = "MapProfilesAdapter";

    private Context context;
    private List<User> farms;

    public MapProfilesAdapter(Context context, List<User> farms)
    {
        this.context = context;
        this.farms = farms;
    }

    @NonNull
    @Override
    public MapProfilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_map_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapProfilesAdapter.ViewHolder holder, int position) {
        User farm = farms.get(position);
        try {
            holder.bind(farm);
        } catch (JSONException | ParseException e) {
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

    public void addAll(List<User> newFarms) {
        farms.addAll(newFarms);
        notifyItemRangeInserted(farms.size()-newFarms.size(), newFarms.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackgroundPhoto;
        ImageView ivBackgroundEnlarged;

        TextView tvFarmName;
        TextView tvFarmNameExpanded;

        TextView tvDistance;
        TextView tvDistanceExpanded;

        View expandedView;
        View normalView;

        TextView tvDescription;

        RecyclerView rvTags;
        MapProfileTagsAdapter tagsAdapter;
        LinearLayoutManager linearLayoutManager;
        Button btnContract;
        Button btnGoToFarmProfile;

        int shortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        int longAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_longAnimTime);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            expandedView = itemView.findViewById(R.id.expandedView);
            normalView = itemView.findViewById(R.id.normalView);

            ivBackgroundPhoto = itemView.findViewById(R.id.ivBackgroundPhoto);
            ivBackgroundEnlarged = itemView.findViewById(R.id.ivBackgroundEnlarged);

            tvFarmName = itemView.findViewById(R.id.tvFarmName);
            tvFarmNameExpanded = itemView.findViewById(R.id.tvFarmNameExpanded);

            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDistanceExpanded = itemView.findViewById(R.id.tvFarmDistanceExpanded);

            tvDescription = itemView.findViewById(R.id.tvFarmDescription);

            rvTags = itemView.findViewById(R.id.rvTags);

            btnContract = itemView.findViewById(R.id.btnContract);
            btnGoToFarmProfile = itemView.findViewById(R.id.btnGoToFarmProfile);
        }

        public void bind(User farm) throws JSONException, ParseException {
            if(farm.expanded) {
                btnContract.setOnClickListener(v -> {
                    farm.expanded = false;
                    crossfade(farm);
                });

                btnGoToFarmProfile.setOnClickListener(v -> {
                    Fragment fragment = new FarmProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(User.FARM_USER_TYPE, Parcels.wrap(farm));
                    fragment.setArguments(args);
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                });

                normalView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);

                if (farm.getImageUrl() != null) {
                    Glide.with(context)
                            .load(farm.getImageUrl())
                            .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                            .into(ivBackgroundEnlarged);
                } else {
                    Glide.with(context)
                            .load(R.drawable.farm_background)
                            .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                            .into(ivBackgroundEnlarged);
                }

                tvFarmNameExpanded.setText(farm.getName());
                tvDistanceExpanded.setText(String.format(context.getResources().getString(R.string.distance_calc), farm.getDistance() / METERS_TO_MILE));
                if (farm.getUser() != null) {
                    if (farm.getUser().getString(User.KEY_BIO) != null) {
                        tvDescription.setText(farm.getUser().getString(User.KEY_BIO));
                    } else {
                        Log.i(TAG, "bio is null " + farm.getUser().getUsername());
                    }
                } else {
                    Log.i(TAG, "farm user is null");
                }

                JSONArray JSONtags = farm.getUser().getJSONArray("tags");
                if(JSONtags != null) {
                    List<String> tags = new ArrayList<>();
                    tagsAdapter = new MapProfileTagsAdapter(context, tags);
                    rvTags.setAdapter(tagsAdapter);
                    linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    rvTags.setLayoutManager(linearLayoutManager);

                    for (int i = 0; i < JSONtags.length(); i++) {
                        tags.add(JSONtags.getString(i));
                        Log.i(TAG, tags.get(i));
                    }
                    tagsAdapter.notifyDataSetChanged();
                }

            } else {
                normalView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);

                tvFarmName.setText(farm.getName());
                tvDistance.setText(String.format(context.getResources().getString(R.string.distance_calc), farm.getDistance() / METERS_TO_MILE));

                if (farm.getImageUrl() != null) {
                    Glide.with(context)
                            .load(farm.getImageUrl())
                            .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                            .into(ivBackgroundPhoto);
                } else {
                    Glide.with(context)
                            .load(R.drawable.farm_background)
                            .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                            .into(ivBackgroundPhoto);
                }
            }
        }

        private void crossfade(User farm) {

            normalView.setAlpha(0f);
            normalView.setVisibility(View.VISIBLE);

            normalView.animate()
                    .alpha(1f)
                    .setDuration(longAnimationDuration)
                    .setListener(null);

            tvFarmName.setText(farm.getName());
            tvDistance.setText(String.format(context.getResources().getString(R.string.distance_calc), farm.getDistance() / METERS_TO_MILE));

            if (farm.getImageUrl() != null) {
                Glide.with(context)
                        .load(farm.getImageUrl())
                        .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                        .into(ivBackgroundPhoto);
            } else {
                Glide.with(context)
                        .load(R.drawable.farm_background)
                        .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                        .into(ivBackgroundPhoto);
            }

            expandedView.animate()
                    .alpha(0f)
                    .setDuration(longAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            expandedView.setVisibility(View.GONE);
                        }
                    });
            expandedView.setAlpha(1f);
        }
    }

}
