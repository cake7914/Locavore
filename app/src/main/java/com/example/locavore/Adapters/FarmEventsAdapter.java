package com.example.locavore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locavore.Models.Event;
import com.example.locavore.Models.Farm;
import com.example.locavore.R;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_farm_profile, parent, false);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Event event) {

        }
    }

}
