package com.example.locavore.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.Fragments.EventDetailsFragment;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.Models.UserEvent;
import com.example.locavore.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.parceler.Parcels;

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
        try {
            holder.bind(event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        ImageView ivEventPhoto;
        ImageButton btnAttendedEvent;
        ImageButton btnLikeEvent;
        ImageButton btnDislikeEvent;
        View containerView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvName);
            ivEventPhoto = itemView.findViewById(R.id.ivEventPhoto);
            btnAttendedEvent = itemView.findViewById(R.id.btnAttended);
            btnLikeEvent = itemView.findViewById(R.id.btnLikeEvent);
            btnDislikeEvent = itemView.findViewById(R.id.btnDislikeEvent);
            containerView = itemView;
        }

        public void bind(Event event) throws JSONException {
            tvEventName.setText(event.getName());

            if(event.getPhotos().length() != 0) {
                Glide.with(mContext)
                        .load(event.getPhotos().getJSONObject(0).getString("url"))
                        .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                        .into(ivEventPhoto);
            } else {
                ivEventPhoto.setImageBitmap(null);
            }

            // Event Details View
            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                    Fragment eventDetailsFragment = new EventDetailsFragment();
                    Bundle args = new Bundle();
                    args.putParcelable("Event", Parcels.wrap(event));
                    eventDetailsFragment.setArguments(args);
                    fragmentManager.beginTransaction().add(R.id.flContainer, eventDetailsFragment).addToBackStack(null).commit();
                }
            });

            // only display this information if this is a user viewing a farm's profile
            // if a farm is viewing it's own profile, provide edit functionality
            if(!ParseUser.getCurrentUser().getString(User.KEY_YELP_ID).equals(event.getFarm())) {
                // display depending on attended or not, and liked or not
                ParseQuery<UserEvent> eventQuery = ParseQuery.getQuery("UserEvent");
                eventQuery.whereEqualTo(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
                eventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
                eventQuery.getFirstInBackground((object, e) -> {
                    if(object != null) {
                        btnAttendedEvent.setVisibility(View.VISIBLE);
                        btnAttendedEvent.setColorFilter(mContext.getResources().getColor(R.color.light_green));
                        if(object.getLiked() == UserEvent.NEUTRAL)
                        {
                            btnLikeEvent.setVisibility(View.VISIBLE);
                            btnDislikeEvent.setVisibility(View.VISIBLE);
                        }
                        else if(object.getLiked() == UserEvent.LIKED)
                        {
                            btnLikeEvent.setColorFilter(mContext.getResources().getColor(R.color.dark_yellow));
                            btnLikeEvent.setVisibility(View.VISIBLE);
                            btnDislikeEvent.setVisibility(View.GONE);
                        } else if(object.getLiked() == UserEvent.DISLIKED){
                            btnDislikeEvent.setColorFilter(mContext.getResources().getColor(R.color.dark_yellow));
                            btnDislikeEvent.setVisibility(View.VISIBLE);
                            btnLikeEvent.setVisibility(View.INVISIBLE);
                        }
                    } else { // set normal coloring & hide buttons
                        btnAttendedEvent.setVisibility(View.VISIBLE);
                        btnAttendedEvent.setColorFilter(mContext.getResources().getColor(R.color.gray));
                        btnDislikeEvent.setVisibility(View.INVISIBLE);
                        btnLikeEvent.setVisibility(View.INVISIBLE);
                    }
                });

                btnAttendedEvent.setOnClickListener(v -> {
                    ParseQuery<UserEvent> userEventQuery = ParseQuery.getQuery("UserEvent");
                    userEventQuery.whereEqualTo(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
                    userEventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
                    userEventQuery.getFirstInBackground((object, e) -> {
                        if(object != null) { // it already exists; they want to mark as no longer attending
                            object.deleteInBackground();
                            btnAttendedEvent.setColorFilter(mContext.getResources().getColor(R.color.gray));
                            // set visibilities of like/dislike buttons to visible // remove color filters
                            btnLikeEvent.setColorFilter(mContext.getResources().getColor(R.color.gray));
                            btnDislikeEvent.setColorFilter(mContext.getResources().getColor(R.color.gray));
                            btnDislikeEvent.setVisibility(View.INVISIBLE);
                            btnLikeEvent.setVisibility(View.INVISIBLE);
                        } else { // it does not exist; they want to mark as attending
                            // change color to attended color
                            btnAttendedEvent.setColorFilter(mContext.getResources().getColor(R.color.light_green));
                            // set visibilities of like/dislike buttons to visible
                            btnDislikeEvent.setVisibility(View.VISIBLE);
                            btnLikeEvent.setVisibility(View.VISIBLE);

                            // save attending into database: create new UserEvent
                            ParseObject userEvent = ParseObject.create("UserEvent");
                            userEvent.put(UserEvent.KEY_EVENT_ID, event.getObjectId());
                            userEvent.put(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
                            userEvent.put(UserEvent.KEY_FARM_ID, event.getFarm());
                            userEvent.saveInBackground();
                        }
                    });
                });

                btnDislikeEvent.setOnClickListener(v -> {
                    ParseQuery<UserEvent> userEventQuery = ParseQuery.getQuery("UserEvent");
                    userEventQuery.whereEqualTo(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
                    userEventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
                    userEventQuery.getFirstInBackground((userEvent, e) -> {
                        if(userEvent != null) {
                            if(userEvent.getLiked() == UserEvent.DISLIKED) { // already disliked: undislike
                                btnDislikeEvent.setColorFilter(mContext.getResources().getColor(R.color.gray));
                                btnLikeEvent.setVisibility(View.VISIBLE);
                                userEvent.put(UserEvent.KEY_LIKED, UserEvent.NEUTRAL);
                            } else { // dislike; change color to disliked color & save in background
                                btnDislikeEvent.setColorFilter(mContext.getResources().getColor(R.color.dark_yellow));
                                btnLikeEvent.setVisibility(View.INVISIBLE);
                                userEvent.setLiked(UserEvent.DISLIKED);
                            }
                            userEvent.saveInBackground();
                        }
                    });
                });

                btnLikeEvent.setOnClickListener(v -> {
                    ParseQuery<UserEvent> userEventQuery = ParseQuery.getQuery("UserEvent");
                    userEventQuery.whereEqualTo(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
                    userEventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
                    userEventQuery.getFirstInBackground((userEvent, e) -> {
                        if(userEvent != null) {
                            if(userEvent.getLiked() == UserEvent.LIKED) { // already liked: unlike
                                btnLikeEvent.setColorFilter(mContext.getResources().getColor(R.color.gray));
                                btnDislikeEvent.setVisibility(View.VISIBLE);
                                userEvent.put(UserEvent.KEY_LIKED, UserEvent.NEUTRAL);
                            } else { // like; change color to liked color & save in background
                                btnLikeEvent.setColorFilter(mContext.getResources().getColor(R.color.dark_yellow));
                                btnDislikeEvent.setVisibility(View.GONE);
                                userEvent.setLiked(UserEvent.LIKED);
                            }
                            userEvent.saveInBackground();
                        }
                    });
                });
            }
        }
    }

}
