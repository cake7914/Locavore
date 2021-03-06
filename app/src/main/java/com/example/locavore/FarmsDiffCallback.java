package com.example.locavore;

import androidx.recyclerview.widget.DiffUtil;

import com.example.locavore.Models.User;

import java.util.List;
import java.util.Objects;

public class FarmsDiffCallback extends DiffUtil.Callback{

    private final List<User> mOldFarms;
    private final List<User> mNewFarms;

    public FarmsDiffCallback(List<User> oldFarms, List<User> newFarms) {
        mOldFarms = oldFarms;
        mNewFarms = newFarms;
    }

    @Override
    public int getOldListSize() {
        return mOldFarms.size();
    }

    @Override
    public int getNewListSize() {
        return mNewFarms.size();
    }

    @Override // same id
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return Objects.equals(mOldFarms.get(oldItemPosition).getId(), mNewFarms.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldFarms.get(oldItemPosition) == mNewFarms.get(newItemPosition);
    }
}
