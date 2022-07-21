package com.example.locavore;

import androidx.recyclerview.widget.DiffUtil;

import com.example.locavore.Models.Event;

import java.util.List;
import java.util.Objects;

public class EventsDiffCallback extends DiffUtil.Callback{

    private final List<Event> mOldEvents;
    private final List<Event> mNewEvents;

    public EventsDiffCallback(List<Event> oldEvents, List<Event> newEvents) {
        mOldEvents = oldEvents;
        mNewEvents = newEvents;
    }

    @Override
    public int getOldListSize() {
        return mOldEvents.size();
    }

    @Override
    public int getNewListSize() {
        return mNewEvents.size();
    }

    @Override // same id
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return Objects.equals(mOldEvents.get(oldItemPosition).getObjectId(), mNewEvents.get(newItemPosition).getObjectId());
    }

    @Override // same contents
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldEvents.get(oldItemPosition) == mNewEvents.get(newItemPosition);
    }
}