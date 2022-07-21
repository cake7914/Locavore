package com.example.locavore.Fragments;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.locavore.Adapters.EventPhotosAdapter;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.UserEvent;
import com.example.locavore.R;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventDetailsFragment extends Fragment {
    TextView tvEventName;
    TextView tvEventDescription;
    TextView tvLikeCount;
    TextView tvEventLocation;
    TextView tvDate;
    TextView tvTime;
    ImageButton btnLike;
    Integer likeCount;
    EventPhotosAdapter eventPhotosAdapter;
    RecyclerView rvPhotos;

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
        tvTime = view.findViewById(R.id.tvTime);
        rvPhotos = view.findViewById(R.id.rvPhotos);

        JSONArray photosJSON = event.getPhotos();
        List<String> urls = new ArrayList<>();
        for(int i = 0; i < photosJSON.length(); i++) {
            try {
                urls.add(photosJSON.getJSONObject(i).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        eventPhotosAdapter = new EventPhotosAdapter(getContext(), urls);
        rvPhotos.setAdapter(eventPhotosAdapter);
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        tvEventName.setText(event.getName());
        tvEventDescription.setText(event.getDescription());
        tvEventLocation.setText(event.getLocationString());

        LocalDateTime startDate = event.getDate(Event.KEY_START_DATE).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDate = event.getDate(Event.KEY_END_DATE).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        JSONArray daysOfWeekJSON = event.getJSONArray(Event.KEY_DAYS_OF_WEEK);
        String daysOfWeek = "";
        for(int i = 1; i <= 7; i++) {
            try {
                if(daysOfWeekJSON.getInt(i) == 1) {
                    daysOfWeek += DateFormatSymbols.getInstance().getShortWeekdays()[i] + " ";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        tvDate.setText(daysOfWeek + ", " + startDate.format(DateTimeFormatter.ofPattern("MM/dd")) + " - " + endDate.format(DateTimeFormatter.ofPattern("MM/dd")));
        tvTime.setText(startDate.format(DateTimeFormatter.ofPattern("KK:mm a")) + " - " + endDate.format(DateTimeFormatter.ofPattern("KK:mm a")));

        ParseQuery<UserEvent> userEventQuery = ParseQuery.getQuery("UserEvent");
        userEventQuery.whereEqualTo(UserEvent.KEY_EVENT_ID, event.getObjectId());
        userEventQuery.findInBackground((userEvents, e) -> {
            for (int i = 0; i < userEvents.size(); i++) {
                if(userEvents.get(i).getLiked() == UserEvent.LIKED) {
                    likeCount++;
                    if(Objects.equals(userEvents.get(i).getUserId(), ParseUser.getCurrentUser().getObjectId())) {
                        btnLike.setColorFilter(requireContext().getResources().getColor(R.color.dark_yellow));
                    }
                }
            }
            tvLikeCount.setText(String.valueOf(likeCount));
        });
    }
}