package com.example.locavore.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.locavore.Models.Event;
import com.example.locavore.Models.User;
import com.example.locavore.Models.UserEvent;
import com.example.locavore.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

public class EventDetailsFragment extends Fragment {
    TextView tvEventName;
    TextView tvEventDescription;
    TextView tvLikeCount;
    TextView tvEventLocation;
    ImageButton btnLike;
    Integer likeCount;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Event event = ((Event) Parcels.unwrap(requireArguments().getParcelable("Event")));
        likeCount = 0;

        tvEventName = view.findViewById(R.id.tvEventName);
        tvEventDescription = view.findViewById(R.id.tvEventDescription);
        tvEventLocation = view.findViewById(R.id.tvLocation);
        tvLikeCount = view.findViewById(R.id.tvLikeCount);
        btnLike = view.findViewById(R.id.btnLikeEvent2);

        tvEventName.setText(event.getName());
        tvEventDescription.setText(event.getDescription());
        tvEventLocation.setText(event.getLocationString());

        ParseQuery<UserEvent> userEventQuery = ParseQuery.getQuery("UserEvent");
        userEventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
        userEventQuery.findInBackground(new FindCallback<UserEvent>() {
            @Override
            public void done(List<UserEvent> userEvents, ParseException e) {
                for (int i = 0; i < userEvents.size(); i++) {
                    if(userEvents.get(i).getLiked() == UserEvent.LIKED) {
                        likeCount++;
                        if(Objects.equals(userEvents.get(i).getUserId(), ParseUser.getCurrentUser().getObjectId())) {
                            btnLike.setColorFilter(requireContext().getResources().getColor(R.color.dark_yellow));
                        }
                    }
                }
                tvLikeCount.setText(String.valueOf(likeCount));
            }
        });

    }

}