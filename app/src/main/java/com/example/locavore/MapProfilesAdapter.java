package com.example.locavore;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.Models.Farm;
import com.google.android.material.shape.RoundedCornerTreatment;

import org.json.JSONException;

import java.util.List;

public class MapProfilesAdapter extends RecyclerView.Adapter<MapProfilesAdapter.ViewHolder> {
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
        notifyItemRangeInserted(farms.size()-newFarms.size(), newFarms.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackgroundPhoto;
        TextView tvFarmName;
        TextView tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackgroundPhoto = itemView.findViewById(R.id.ivBackgroundPhoto);
            tvFarmName = itemView.findViewById(R.id.tvFarmName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }

        public void bind(Farm farm) throws JSONException {
            tvFarmName.setText(farm.getName());

            if(farm.getImageUrl() != null) {
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
