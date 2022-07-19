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

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EventDetailsFragment extends Fragment {
    TextView tvEventName;
    TextView tvEventDescription;
    TextView tvLikeCount;
    TextView tvEventLocation;
    TextView tvDaysOfWeek;
    TextView tvDate;
    TextView tvTime;
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
        tvDate = view.findViewById(R.id.tvDates);
        tvDaysOfWeek = view.findViewById(R.id.tvDaysOfWeek);
        tvTime = view.findViewById(R.id.tvTime);

        tvEventName.setText(event.getName());
        tvEventDescription.setText(event.getDescription());
        tvEventLocation.setText(event.getLocationString());
        //TODO: use Calendar instead
        Date startDate = event.getDate(Event.KEY_START_DATE);
        Date endDate = event.getDate(Event.KEY_END_DATE);
        tvDate.setText(startDate.getMonth() + "/" + startDate.getDay() + " - " + endDate.getMonth() + "/" + endDate.getDay());
        tvTime.setText(startDate.getHours() + ":" + startDate.getMinutes() + " - " + endDate.getHours() + ":" + endDate.getMinutes());
        JSONArray daysOfWeekJSON = event.getJSONArray(Event.KEY_DAYS_OF_WEEK);
        String daysOfWeek = "";
        for(int i = 0; i < 7; i++) {
            try {
                if(daysOfWeekJSON.getInt(i) == 1) {
                    daysOfWeek += getDay(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        tvDaysOfWeek.setText(daysOfWeek);

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

    public String getDay(int pos) {
        switch(pos) {
            case 0:
                return "M ";
            case 1:
                return "T ";
            case 2:
                return "W ";
            case 3:
                return "Th ";
            case 4:
                return "F ";
            case 5:
                return "S ";
            case 6:
                return "Su ";
        }
        return null;
    }
}