package com.example.locavore.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.FarmsDiffCallback;
import com.example.locavore.Fragments.FarmProfileFragment;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
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
    private List<User> mFarms;
    public ExpansionResponse expansionResponse;

    public MapProfilesAdapter(Context context, List<User> farms)
    {
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
    public MapProfilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_map_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapProfilesAdapter.ViewHolder holder, int position) {
        User farm = mFarms.get(position);
        try {
            holder.normalView.setOnLongClickListener(v -> {
                expansionResponse.onExpansion(holder.getAdapterPosition());
                return false;
            });
            holder.bind(farm);
        } catch (JSONException | ParseException e) {
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
        notifyItemRangeInserted(mFarms.size()-newFarms.size(), newFarms.size());
    }

    public interface ExpansionResponse {
        void onExpansion(int pos);
        void onContraction();
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

        FlexboxLayoutManager flexboxLayoutManager;
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
                normalView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);
                expandedView.setAlpha(1f);
                createExpandedView(farm);

            } else {
                normalView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                createNormalView(farm);
            }
        }

        private void createNormalView(User farm) {
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

        private void createExpandedView(User farm) throws JSONException {
            btnContract.setOnClickListener(v -> {
                farm.expanded = false;
                expansionResponse.onContraction();
                crossfade(farm);
            });

            btnGoToFarmProfile.setOnClickListener(v -> {
                Fragment fragment = new FarmProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(User.FARM_USER_TYPE, Parcels.wrap(farm));
                fragment.setArguments(args);
                ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            });

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

            if(farm.getUser().getString(User.KEY_BIO).length() >= 128)
                tvDescription.setText(farm.getUser().getString(User.KEY_BIO).substring(0, 125) + "...");
            else
                tvDescription.setText(farm.getUser().getString(User.KEY_BIO));

            JSONArray JSONtags = farm.getUser().getJSONArray("tags");
            if(JSONtags != null) {
                List<String> tags = new ArrayList<>();
                tagsAdapter = new MapProfileTagsAdapter(context, tags);
                rvTags.setAdapter(tagsAdapter);
                flexboxLayoutManager = new FlexboxLayoutManager(context);
                flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
                rvTags.setLayoutManager(flexboxLayoutManager);

                for (int i = 0; i < JSONtags.length(); i++) {
                    tags.add(JSONtags.getString(i));
                }
                tagsAdapter.notifyItemRangeInserted(0, JSONtags.length());
            }
        }

        private void crossfade(User farm) {

            normalView.setAlpha(0f);
            normalView.setVisibility(View.VISIBLE);

            normalView.animate()
                    .alpha(1f)
                    .setDuration(longAnimationDuration)
                    .setListener(null);

            createNormalView(farm);

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
