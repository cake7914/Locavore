package com.example.locavore.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.locavore.Activities.LoginActivity;
import com.example.locavore.R;
import com.parse.ParseUser;

public class LocavoreProfileFragment extends Fragment {
    public static final String TAG = "LocavoreProfileFragment";
    Button btnLogout;

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

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> ParseUser.logOutInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Issue with logout", e);
                Toast.makeText(getContext(), requireContext().getString(R.string.misc_logout_error), Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
                Toast.makeText(getContext(), requireContext().getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            }
        }));
    }
}