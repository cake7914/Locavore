package com.example.locavore.Fragments;

import static com.example.locavore.BuildConfig.YELP_API_KEY;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.locavore.Activities.SplashScreenActivity;
import com.example.locavore.Adapters.FarmProfileEventsAdapter;
import com.example.locavore.Adapters.FarmProfileReviewsAdapter;
import com.example.locavore.DataManager;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.FarmReviewsSearchResult;
import com.example.locavore.Models.Review;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.YelpService;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FarmProfileFragment extends Fragment {

    public static final String TAG = "FarmProfileFragment";
    public static final String BASE_URL = "https://api.yelp.com/v3/";
    Button btnCreateEvent;
    Button btnLogout;

    TextView tvFarmName;
    TextView tvBio;
    TextView tvPhoneNumber;
    TextView tvAddress;
    TextView tvRating;
    TextView tvReviewCount;

    ImageView ivBackgroundPhoto;
    ImageView ivProfileImage;

    RecyclerView rvEvents;
    FarmProfileEventsAdapter eventsAdapter;
    RecyclerView rvReviews;
    FarmProfileReviewsAdapter reviewsAdapter;
    List<Event> mEvents = new ArrayList<>();
    List<Review> mReviews = new ArrayList<>();
    DataManager dataManager = DataManager.getInstance(null);

    ParseUser farm;

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

        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvFarmName = view.findViewById(R.id.tvFarmName);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        ivBackgroundPhoto = view.findViewById(R.id.ivBackgroundPhoto);
        ivProfileImage = view.findViewById(R.id.ivProfilePhoto);
        tvBio = view.findViewById(R.id.tvDescription);
        rvReviews = view.findViewById(R.id.rvReviews);
        rvEvents = view.findViewById(R.id.rvEvents);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvRating = view.findViewById(R.id.tvRating);
        tvReviewCount = view.findViewById(R.id.tvReviewCount);

        eventsAdapter = new FarmProfileEventsAdapter(getContext(), mEvents);
        rvEvents.setAdapter(eventsAdapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvReviews = view.findViewById(R.id.rvReviews);
        reviewsAdapter = new FarmProfileReviewsAdapter(getContext(), mReviews);
        rvReviews.setAdapter(reviewsAdapter);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if(getArguments() != null) { // this is a user viewing the farm
            farm = ((User)Parcels.unwrap(getArguments().getParcelable(User.FARM_USER_TYPE))).getUser();
            btnCreateEvent.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
        } else { // farmer viewing their own page
            farm = ParseUser.getCurrentUser();
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
        }

        // parse events for this farm from data
        getFarmEvents(dataManager.mEvents, farm);

        // fetch reviews for this farm
        yelpReviewsRequest(farm);

        tvFarmName.setText(farm.getString(User.KEY_NAME));
        tvBio.setText(farm.getString(User.KEY_BIO));
        tvAddress.setText(farm.getString(User.KEY_ADDRESS));
        tvPhoneNumber.setText(farm.getString(User.KEY_PHONE));
        tvRating.setText(String.valueOf(farm.getInt(User.KEY_RATING)));
        tvReviewCount.setText(String.valueOf(farm.getInt(User.KEY_REVIEW_COUNT)));

        tvPhoneNumber.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + farm.getString(User.KEY_PHONE)));
            startActivity(intent);
        });

        tvAddress.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + farm.getString(User.KEY_ADDRESS)));
            startActivity(intent);
        });

        Glide.with(getContext()).load(farm.getString(User.KEY_PROFILE_PHOTO)).circleCrop().into(ivProfileImage);
        Glide.with(getContext())
                .load(farm.getString(User.KEY_PROFILE_BACKDROP))
                .centerCrop()
                .into(ivBackgroundPhoto);
    }

    private void showAlertDialog() {
        FragmentManager fragmentManager = getParentFragmentManager();
        CreateEventDialogFragment alertDialog = CreateEventDialogFragment.newInstance("New Event");
        alertDialog.show(fragmentManager, "fragment_alert");
    }

    private void getFarmEvents(List<Event> allEvents, ParseUser farm) {
        for(Event event: allEvents) {
            if(event.getFarm().equals(farm.getString(User.KEY_YELP_ID))) {
                mEvents.add(event);
            }
        }
        eventsAdapter.notifyItemRangeInserted(0, mEvents.size());
    }

    private void yelpReviewsRequest(ParseUser farm) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        YelpService yelpService = retrofit.create(YelpService.class);
        Call<FarmReviewsSearchResult> call = yelpService.findBusinessReviews("Bearer " + YELP_API_KEY, farm.getString(User.KEY_YELP_ID));
        call.enqueue(new Callback<FarmReviewsSearchResult>() {
            @Override
            public void onResponse(Call<FarmReviewsSearchResult> call, Response<FarmReviewsSearchResult> response) {
                Log.i(TAG, "Success! ");
                if(response.body() != null)
                    reviewsAdapter.addAll(response.body().getReviews());
            }

            @Override
            public void onFailure(Call<FarmReviewsSearchResult> call, Throwable t) {
                Log.i(TAG, "Failure " + t);
            }
        });
    }
}