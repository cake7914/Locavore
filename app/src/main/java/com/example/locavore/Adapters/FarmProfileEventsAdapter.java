package com.example.locavore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locavore.Models.Event;
import com.example.locavore.R;

import java.util.List;

public class FarmProfileEventsAdapter extends RecyclerView.Adapter<FarmProfileEventsAdapter.ViewHolder> {
    private Context mContext;
    private List<Event> mEvents;

    public FarmProfileEventsAdapter(Context context, List<Event> events) {
            this.mContext = context;
            this.mEvents = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_profile_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = mEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
            return mEvents.size();
            }

    public void clear() {
        int size = mEvents.size();
        mEvents.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<Event> newEvents) {
        mEvents.addAll(newEvents);
        notifyItemRangeInserted(mEvents.size() - newEvents.size(), newEvents.size());
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
        }

        public void bind(Event event) {
            tvEventName.setText(event.getName());
        }
    }

}
