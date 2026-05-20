package gr.aueb.lecturelens.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import gr.aueb.lecturelens.R;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;
    private boolean isCourseReview;
    public ReviewAdapter(List<Review> reviewList, boolean isCourseReview) {
        this.reviewList = reviewList;
        this.isCourseReview = isCourseReview;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        if (review.isAnonymous()) {
            holder.userName.setText(holder.itemView.getContext().getString(R.string.anonymous_user));
        } else {
            holder.userName.setText(review.getUsername() != null ? review.getUsername() : "User");
        }

        if (isCourseReview) {
            holder.reviewRating.setText("⭐ " + review.getDifficulty() + ".0");
        } else {
            holder.reviewRating.setText(String.format(Locale.getDefault(), "⭐ %.1f", review.getRating()));
        }

        String formattedDate = convertIsoToReadableDate(review.getCreatedAt());
        holder.reviewDate.setText(formattedDate);

        String mainText = review.getReviewText() != null ? review.getReviewText() : "";

        if (isCourseReview) {
            String studyHoursSubtext = "\nEstimated Weekly Study: " + (int) review.getStudyHours() + " hours";
            holder.reviewText.setText(mainText + "\n" + studyHoursSubtext);
        } else {
            holder.reviewText.setText(mainText);
        }

        holder.reportButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Review content reported.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    private String convertIsoToReadableDate(String isoStringFromServer) {
        if (isoStringFromServer == null || isoStringFromServer.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            if (!isoStringFromServer.contains(".")) {
                sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
            }
            Date date = sourceFormat.parse(isoStringFromServer);
            SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            targetFormat.setTimeZone(TimeZone.getDefault());
            return targetFormat.format(date);
        } catch (Exception e) {
            if (isoStringFromServer.length() >= 10) {
                return isoStringFromServer.substring(0, 10);
            }
            return isoStringFromServer;
        }
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, reviewRating, reviewDate, reviewText, reportButton;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            reviewRating = itemView.findViewById(R.id.reviewRating);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            reviewText = itemView.findViewById(R.id.reviewText);
            reportButton = itemView.findViewById(R.id.reportButton);
        }
    }
}