package com.example.locavore.Adapters;

import android.content.Context;
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
import com.example.locavore.Models.Event;
import com.example.locavore.Models.Farm;
import com.example.locavore.R;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MapProfileTagsAdapter extends RecyclerView.Adapter<MapProfileTagsAdapter.ViewHolder> {
    private Context context;
    private List<String> tags;

    public MapProfileTagsAdapter(Context context, List<String> tags) {
        this.context = context;
        this.tags = tags;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.bind(tag);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void clear() {
        int size = tags.size();
        tags.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<String> newTags) {
        tags.addAll(newTags);
        notifyItemRangeInserted(tags.size() - newTags.size(), newTags.size());
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTagName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagName = itemView.findViewById(R.id.tvTag);
        }

        public void bind(String tag) {
            tvTagName.setText(tag);
        }
    }

}

