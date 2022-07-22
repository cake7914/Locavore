package com.example.locavore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locavore.R;
import com.parse.Parse;
import com.parse.ParseFile;

import java.util.List;

public class EventPhotosAdapter extends RecyclerView.Adapter<EventPhotosAdapter.ViewHolder> {
    private Context mContext;
    private List<String> mPhotos;

    public EventPhotosAdapter(Context context, List<String> photos) {
        this.mContext = context;
        this.mPhotos = photos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = mPhotos.get(position);
        holder.bind(url);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }

        public void bind(String url) {
            Glide.with(mContext).load(url).centerCrop().into(ivPhoto);
        }
    }

}