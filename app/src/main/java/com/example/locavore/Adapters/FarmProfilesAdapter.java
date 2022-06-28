package com.example.locavore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.locavore.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locavore.Models.Farm;
import com.parse.ParseFile;

import java.util.List;

public class FarmProfilesAdapter extends RecyclerView.Adapter<FarmProfilesAdapter.ViewHolder> {
    private Context context;
    private List<Farm> farms;

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
            holder.bind(farm);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFarmName = itemView.findViewById(R.id.tvFarmName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
        }

        public void bind(Farm farm) {
            tvFarmName.setText(farm.getUser().getString(Farm.KEY_NAME));

            if(farm.getUser().getParseFile("profilePhoto") != null) {
                Glide.with(context).load(farm.getUser().getParseFile("profilePhoto").getUrl()).circleCrop().into(ivProfileImage);
            } else {
                ivProfileImage.setImageBitmap(null);
            }

        }
    }

}