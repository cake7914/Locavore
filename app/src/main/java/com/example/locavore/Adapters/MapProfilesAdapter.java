package com.example.locavore.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.Models.Farm;
import com.example.locavore.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MapProfilesAdapter extends RecyclerView.Adapter<MapProfilesAdapter.ViewHolder> {
    private static final double METERS_TO_MILE = 1609.34;
    public static final String TAG = "MapProfilesAdapter";

    private Context context;
    private List<Farm> farms;

    public MapProfilesAdapter(Context context, List<Farm> farms)
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
        Farm farm = farms.get(position);
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

    public void addAll(List<Farm> newFarms) {
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
        }

        public void bind(Farm farm) throws JSONException, ParseException {
            if(farm.expanded) {
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
                tvDistanceExpanded.setText(String.format("%.2f miles", farm.getDistance() / METERS_TO_MILE));
                if (farm.getUser() != null) {
                    if (farm.getUser().getString(Farm.KEY_BIO) != null) {
                        tvDescription.setText(farm.getUser().getString(Farm.KEY_BIO));
                    } else {
                        Log.i(TAG, "bio is null " + farm.getUser().getUsername());
                    }
                } else {
                    Log.i(TAG, "farm user is null");
                }

                JSONArray JSONtags = farm.getUser().getJSONArray("tags");
                if(JSONtags != null) {
                    List<String> tags = new ArrayList<>();
                    for (int i = 0; i < JSONtags.length(); i++) {
                        tags.add(JSONtags.getString(i));
                    }
                    tagsAdapter = new MapProfileTagsAdapter(context, tags);
                    rvTags.setAdapter(tagsAdapter);
                    linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    rvTags.setLayoutManager(linearLayoutManager);
                }

            } else {
                normalView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);

                tvFarmName.setText(farm.getName());
                tvDistance.setText(String.format("%.2f miles", farm.getDistance() / METERS_TO_MILE));

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
    }

}
