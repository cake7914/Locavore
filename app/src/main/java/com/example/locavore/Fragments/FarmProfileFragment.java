package com.example.locavore.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.Activities.LoginActivity;
import com.example.locavore.Activities.MainActivity;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Objects;


public class FarmProfileFragment extends Fragment {

    public static final String TAG = "FarmProfileFragment";
    Button btnCreateEvent;
    Button btnLogout;
    TextView tvFarmName;
    TextView tvBio;
    ImageView ivBackgroundPhoto;
    ImageView ivProfileImage;

    public FarmProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_farm_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User farm = Parcels.unwrap(getArguments().getParcelable(User.FARM_USER_TYPE));
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvFarmName = view.findViewById(R.id.tvFarmName);
        ivBackgroundPhoto = view.findViewById(R.id.ivBackgroundPhoto);
        ivProfileImage = view.findViewById(R.id.ivProfilePhoto);
        tvBio = view.findViewById(R.id.tvDescription);

        if(farm == null) { // this is a logged in farm, show them their buttons
            btnCreateEvent.setOnClickListener(v -> {
                showAlertDialog();
            });

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
        } else { // user viewing the farm
            btnCreateEvent.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);

            tvFarmName.setText(farm.getName());
            tvBio.setText(farm.getUser().getString(User.KEY_BIO));

            Glide.with(getContext()).load(farm.getUser().getString(User.KEY_PROFILE_PHOTO)).circleCrop().into(ivProfileImage);
            Glide.with(getContext())
                    .load(farm.getImageUrl())
                    .centerCrop()
                    .into(ivBackgroundPhoto);
        }
    }

    private void showAlertDialog() {
        FragmentManager fragmentManager = getParentFragmentManager();
        CreateEventDialogFragment alertDialog = CreateEventDialogFragment.newInstance("New Event");
        alertDialog.show(fragmentManager, "fragment_alert");
    }

}