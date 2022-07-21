package com.example.locavore.Fragments;

import static com.example.locavore.BuildConfig.YELP_API_KEY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.locavore.Activities.LoginActivity;
import com.example.locavore.Adapters.FarmProfileEventsAdapter;
import com.example.locavore.Adapters.FarmProfileReviewsAdapter;
import com.example.locavore.Adapters.MapProfileTagsAdapter;
import com.example.locavore.DataManager;
import com.example.locavore.Models.Event;
import com.example.locavore.Models.FarmReviewsSearchResult;
import com.example.locavore.Models.Review;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.YelpService;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FarmProfileFragment extends Fragment {

    public static final String TAG = "FarmProfileFragment";
    public static final String BASE_URL = "https://api.yelp.com/v3/";
    Button btnLogout;
    Button btnFollow;
    FloatingActionButton fabCreateEvent;

    TextView tvFarmName;
    TextView tvBio;
    TextView tvPhoneNumber;
    TextView tvAddress;
    TextView tvRating;
    TextView tvReviewCount;

    ImageView ivBackgroundPhoto;
    ImageView ivProfileImage;

    ScrollView scrollView;

    RecyclerView rvEvents;
    FarmProfileEventsAdapter eventsAdapter;
    RecyclerView rvReviews;
    FarmProfileReviewsAdapter reviewsAdapter;
    RecyclerView rvTags;
    MapProfileTagsAdapter tagsAdapter;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabCreateEvent = view.findViewById(R.id.fabCreateEvent);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvFarmName = view.findViewById(R.id.tvFarmName);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        ivBackgroundPhoto = view.findViewById(R.id.ivBackgroundPhoto);
        ivProfileImage = view.findViewById(R.id.ivProfilePhoto);
        tvBio = view.findViewById(R.id.tvDescription);
        rvReviews = view.findViewById(R.id.rvReviews);
        rvEvents = view.findViewById(R.id.rvEvents);
        rvTags = view.findViewById(R.id.rvTags);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvRating = view.findViewById(R.id.tvRating);
        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        scrollView = view.findViewById(R.id.scrollView);
        btnFollow = view.findViewById(R.id.btnFollow2);

        eventsAdapter = new FarmProfileEventsAdapter(getContext(), mEvents);
        rvEvents.setAdapter(eventsAdapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvReviews = view.findViewById(R.id.rvReviews);
        reviewsAdapter = new FarmProfileReviewsAdapter(getContext(), mReviews);
        rvReviews.setAdapter(reviewsAdapter);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if(getArguments() != null) { // this is a user viewing the farm
            farm = ((User)Parcels.unwrap(getArguments().getParcelable(User.FARM_USER_TYPE))).getUser();
            fabCreateEvent.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
        } else { // farmer viewing their own page
            farm = ParseUser.getCurrentUser();
            fabCreateEvent.setOnClickListener(v -> {
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

        // allow scrolling within child scrollView
        scrollView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

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

        JSONArray JSONtags = farm.getJSONArray("tags");
        if(JSONtags != null) {
            List<String> tags = new ArrayList<>();
            MapProfileTagsAdapter tagsAdapter = new MapProfileTagsAdapter(requireContext(), tags);
            rvTags.setAdapter(tagsAdapter);
            FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(requireContext());
            flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
            rvTags.setLayoutManager(flexboxLayoutManager);

            for (int i = 0; i < JSONtags.length(); i++) {
                try {
                    tags.add(JSONtags.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tagsAdapter.notifyItemRangeInserted(0, JSONtags.length());
        }

        try {
            if(checkUserFollowingFarm(farm.getObjectId(), ParseUser.getCurrentUser().getJSONArray(User.KEY_FARMS_FOLLOWING)) == -1) {
                btnFollow.setText(R.string.follow);
                btnFollow.setBackgroundColor(requireContext().getResources().getColor(R.color.light_yellow));
            } else {
                btnFollow.setText(R.string.following);
                btnFollow.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_yellow));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnFollow.setOnClickListener(v -> {
            ParseUser user = ParseUser.getCurrentUser();
            try {
                // initializing params for cloudcode call
                HashMap<String, String> params = new HashMap();
                params.put("objectId", farm.getObjectId());
                params.put("followerId", user.getObjectId());

                int pos = checkUserFollowingFarm(farm.getObjectId(), user.getJSONArray(User.KEY_FARMS_FOLLOWING));
                if(pos == -1) {
                    user.add(User.KEY_FARMS_FOLLOWING, farm.getObjectId());
                    btnFollow.setText(R.string.following);
                    btnFollow.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_yellow));
                    params.put("following", "true");
                } else {
                    JSONArray farms = user.getJSONArray(User.KEY_FARMS_FOLLOWING);
                    assert farms != null;
                    farms.remove(pos);
                    user.put(User.KEY_FARMS_FOLLOWING, farms);
                    btnFollow.setText(R.string.follow);
                    btnFollow.setBackgroundColor(requireContext().getResources().getColor(R.color.light_yellow));
                    params.put("following", "false");
                }
                ParseCloud.callFunctionInBackground("updateFollowers", params, (FunctionCallback<ParseObject>) (obj, e) -> {
                    if (e == null) {
                        Log.i(TAG, "non error");
                    }else{
                        Log.i(TAG, "error" + e.getMessage());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            user.saveInBackground(e -> {
                if (e != null) {
                    Log.i(TAG, "Something has happened", e);
                } else {
                    Log.i(TAG, "Save successful");
                }
            });
        });
    }

    protected int checkUserFollowingFarm(String farmID, JSONArray farmsFollowing) throws JSONException {
        if(farmsFollowing != null) {
            for (int i = 0; i < farmsFollowing.length(); i++) {
                if(farmsFollowing.getString(i).equals(farmID)) {
                    return i;
                }
            }
        }
        return -1;
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