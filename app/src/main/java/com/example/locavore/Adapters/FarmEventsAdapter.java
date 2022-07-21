package com.example.locavore.Adapters;

import static android.location.LocationManager.NETWORK_PROVIDER;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.DataManager;
import com.example.locavore.EventsDiffCallback;
import com.example.locavore.Fragments.EventDetailsFragment;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.Models.UserEvent;
import com.example.locavore.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class FarmEventsAdapter extends RecyclerView.Adapter<FarmEventsAdapter.ViewHolder> implements Filterable {
    public static final String TAG = "FarmEventsAdapter";
    private static final double METERS_TO_MILE = 1609.34;

    private Context mContext;
    private List<Event> mEvents;
    private List<Event> mFilteredEvents;
    private EventsFilter mFilter = new EventsFilter();
    DataManager dataManager = DataManager.getInstance(null);


    public FarmEventsAdapter(Context context, List<Event> events) {
        this.mContext = context;
        this.mEvents = new ArrayList<>(events);
        this.mFilteredEvents = new ArrayList<>(events);

    }

    public void updateList(List <Event> newEvents, boolean filtered) {
        EventsDiffCallback diffCallback = new EventsDiffCallback(mFilteredEvents, newEvents);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        if(!filtered) {
            mEvents.clear();
            mEvents.addAll(newEvents);
        }

        mFilteredEvents.clear();
        mFilteredEvents.addAll(newEvents);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_farm_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = mFilteredEvents.get(position);
        try {
            holder.bind(event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mFilteredEvents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventPhoto;
        TextView tvEventFarm;
        TextView tvEventName;
        TextView tvDistance;
        ImageButton btnAttendedEvent;
        ImageButton btnLikeEvent;
        ImageButton btnDislikeEvent;
        View containerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPhoto = itemView.findViewById(R.id.ivEventPhoto);
            tvEventFarm = itemView.findViewById(R.id.tvFarm);
            tvEventName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnAttendedEvent = itemView.findViewById(R.id.btnAttended);
            btnLikeEvent = itemView.findViewById(R.id.btnLikeEvent);
            btnDislikeEvent = itemView.findViewById(R.id.btnDislikeEvent);
            containerView = itemView;
        }

        public void bind(Event event) throws JSONException {

            containerView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        ParseQuery<UserEvent> userEventQuery = ParseQuery.getQuery("UserEvent");
                        userEventQuery.whereEqualTo(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
                        userEventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
                        userEventQuery.getFirstInBackground((userEvent, err) -> {
                            if(userEvent != null) { // is attended
                                if(userEvent.getLiked() != UserEvent.LIKED){  // like; change color to liked color & save in background
                                    btnLikeEvent.setColorFilter(mContext.getResources().getColor(R.color.dark_yellow));
                                    btnDislikeEvent.setVisibility(View.GONE);
                                    userEvent.setLiked(UserEvent.LIKED);
                                    userEvent.saveInBackground();
                                }
                            }
                        });
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                        Fragment eventDetailsFragment = new EventDetailsFragment();
                        Bundle args = new Bundle();
                        args.putParcelable("Event", Parcels.wrap(event));
                        eventDetailsFragment.setArguments(args);
                        fragmentManager.beginTransaction().add(R.id.flContainer, eventDetailsFragment).addToBackStack(null).commit();
                        super.onLongPress(e);
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            if(event.getPhotos() != null) {
                Glide.with(mContext)
                        .load(event.getPhotos().getJSONObject(0).getString("url"))
                        .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)))
                        .into(ivEventPhoto);
            } else {
                ivEventPhoto.setImageBitmap(null);
            }

            tvEventName.setText(event.getName());
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(User.KEY_YELP_ID, event.getFarm());
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser farm, ParseException e) {
                    tvEventFarm.setText(farm.getString(User.KEY_NAME));
                    Location eventLocation = new Location(NETWORK_PROVIDER);
                    eventLocation.setLongitude(event.getLocation().getLongitude());
                    eventLocation.setLatitude(event.getLocation().getLatitude());
                    tvDistance.setText(String.format(mContext.getResources().getString(R.string.distance_calc), eventLocation.distanceTo(dataManager.mLocation) / METERS_TO_MILE));
                }
            });

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

    public Filter getFilter() {
        return mFilter;
    }

    private class EventsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String query = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            List<Event> filteredEvents = new ArrayList<>();

            for (int i = 0; i < mEvents.size(); i++) {
                if ((mEvents.get(i).getName().toLowerCase().contains(query)) || (mEvents.get(i).getFarm().toLowerCase().contains(query))) {
                    filteredEvents.add(mEvents.get(i));
                }
            }

            results.values = filteredEvents;
            results.count = filteredEvents.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            updateList((ArrayList<Event>) results.values, true);
        }
    }
}
