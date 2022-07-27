package com.example.locavore.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.locavore.Activities.LoginActivity;
import com.example.locavore.Adapters.EventsAdapter;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.UserEvent;
import com.example.locavore.R;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class LocavoreProfileFragment extends Fragment {
    public static final String TAG = "LocavoreProfileFragment";
    private Button btnLogout;
    private RecyclerView rvEvents;
    private EventsAdapter eventsAdapter;
    private List<Event> mEvents = new ArrayList<>();

    public LocavoreProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_locavore_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvEvents = view.findViewById(R.id.rvEvents);
        eventsAdapter = new EventsAdapter(requireContext(), mEvents);
        rvEvents.setAdapter(eventsAdapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        // populate events associated with this user
        getUserEvents();

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> ParseUser.logOutInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Issue with logout", e);
                Toast.makeText(getContext(), requireContext().getString(R.string.misc_logout_error), Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
                requireActivity().finish();
                Toast.makeText(getContext(), requireContext().getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void getUserEvents() {
        ParseQuery<UserEvent> query = ParseQuery.getQuery("UserEvent");
        query.whereEqualTo(UserEvent.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.findInBackground((userEvents, e) -> {
            for(UserEvent userEvent : userEvents) {
                ParseQuery<Event> eventQuery = ParseQuery.getQuery("Event");
                eventQuery.getInBackground(userEvent.getEventId(), (event, e1) -> {
                    eventsAdapter.add(event);
                });
            }
        });
    }

}