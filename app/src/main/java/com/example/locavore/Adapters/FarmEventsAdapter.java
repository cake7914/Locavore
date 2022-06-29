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

public class FarmEventsAdapter extends RecyclerView.Adapter<FarmEventsAdapter.ViewHolder> {
    private Context context;
    private List<Event> events;

    public FarmEventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farm_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void clear() {
        int size = events.size();
        events.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<Event> newEvents) {
        events.addAll(newEvents);
        notifyItemRangeInserted(events.size() - newEvents.size(), newEvents.size());
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventPhoto;
        TextView tvEventFarm;
        TextView tvEventName;
        TextView tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPhoto = itemView.findViewById(R.id.ivEventPhoto);
            tvEventFarm = itemView.findViewById(R.id.tvFarm);
            tvEventName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }

        public void bind(Event event) {

            if(event.getPhoto() != null) {
                Glide.with(context)
                        .load(event.getPhoto().getUrl())
                        .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                        .into(ivEventPhoto);
            } else {
                ivEventPhoto.setImageBitmap(null);
            }

            String farmID = event.getFarm();
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", farmID);
            query.findInBackground((objects, e) -> tvEventFarm.setText(objects.get(0).getString(Farm.KEY_NAME)));

            tvEventName.setText(event.getName());

            // calculate distance from event to this user, using current location and the event's address
            // tvDistance.setText()

        }
    }

}
