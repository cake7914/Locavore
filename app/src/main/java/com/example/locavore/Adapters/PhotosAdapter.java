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
import com.example.locavore.R;
import com.parse.ParseFile;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {
    private Context mContext;
    private List<ParseFile> mPhotos;

    public PhotosAdapter(Context context, List<ParseFile> photos) {
        this.mContext = context;
        this.mPhotos = photos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseFile file = mPhotos.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        TextView tvRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvRemove = itemView.findViewById(R.id.tvRemove);
        }

        public void bind(ParseFile file) {
            Glide.with(mContext).load(file.getUrl()).centerCrop().into(ivPhoto);

            tvRemove.setOnClickListener(v -> {
                for(int i = 0; i < mPhotos.size(); i++) {
                    if(mPhotos.get(i).equals(file)) {
                        mPhotos.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }
            });
        }
    }

}