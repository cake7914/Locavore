package com.example.locavore.Adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locavore.Models.Review;
import com.example.locavore.Models.User;
import com.example.locavore.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FarmProfileReviewsAdapter extends RecyclerView.Adapter<FarmProfileReviewsAdapter.ViewHolder> {
    private Context mContext;
    private List<Review> mReviews;

    public FarmProfileReviewsAdapter(Context context, List<Review> reviews) {
        this.mContext = context;
        this.mReviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = mReviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public void clear() {
        int size = mReviews.size();
        mReviews.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<Review> newReviews) {
        mReviews.addAll(newReviews);
        notifyItemRangeInserted(mReviews.size() - newReviews.size(), newReviews.size());
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvReview;
        TextView tvReviewer;
        TextView tvTimePosted;
        ImageView ivReviewerPhoto;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReview = itemView.findViewById(R.id.tvReview);
            tvReviewer = itemView.findViewById(R.id.tvReviewer);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivReviewerPhoto = itemView.findViewById(R.id.ivReviewerPhoto);
            tvTimePosted = itemView.findViewById(R.id.tvTimePosted);
        }

        public void bind(Review review) {
            tvReview.setText(review.getText());
            tvReviewer.setText(review.getUser().getName());
            ratingBar.setRating(review.getRating());

            if(review.getUser().getImageUrl() != null) {
                Glide.with(mContext).load(review.getUser().getImageUrl()).circleCrop().into(ivReviewerPhoto);
            } else {
                ivReviewerPhoto.setImageBitmap(null);
            }
            tvTimePosted.setText(getRelativeDateTime(review.getTimeCreated()));
        }

        public String getRelativeDateTime(String dateTime) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            sf.setLenient(true);
            String relativeDate = "";
            try {
                long dateMillis = sf.parse(dateTime).getTime();
                relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return relativeDate;
        }
    }

}
