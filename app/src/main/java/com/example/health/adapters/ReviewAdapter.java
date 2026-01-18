package com.example.health.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.health.R;
import com.example.health.model.Review;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView patientNameText;
        private final RatingBar ratingBar;
        private final TextView commentText;
        private final TextView dateText;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameText = itemView.findViewById(R.id.reviewPatientName);
            ratingBar = itemView.findViewById(R.id.reviewRatingBar);
            commentText = itemView.findViewById(R.id.reviewComment);
            dateText = itemView.findViewById(R.id.reviewDate);
        }

        public void bind(Review review) {
            patientNameText.setText(review.getPatientName());
            ratingBar.setRating(review.getRating());

            if (review.getComment() != null && !review.getComment().isEmpty()) {
                commentText.setText(review.getComment());
                commentText.setVisibility(View.VISIBLE);
            } else {
                commentText.setVisibility(View.GONE);
            }

            // Format date
            if (review.getCreatedAt() != null) {
                try {
                    Date date;
                    if (review.getCreatedAt() instanceof Timestamp) {
                        date = ((Timestamp) review.getCreatedAt()).toDate();
                    } else if (review.getCreatedAt() instanceof Long) {
                        date = new Date((Long) review.getCreatedAt());
                    } else {
                        date = new Date();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateText.setText(sdf.format(date));
                } catch (Exception e) {
                    dateText.setText("");
                }
            } else {
                dateText.setText("");
            }
        }
    }
}
