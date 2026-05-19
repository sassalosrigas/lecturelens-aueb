package gr.aueb.lecturelens.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Note: We changed these imports to use java.util instead of java.time
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import gr.aueb.lecturelens.R;
import gr.aueb.lecturelens.java.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
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

        // 1. Populate Username
        if (review.isAnonymous()) {
            holder.userName.setText(holder.itemView.getContext().getString(R.string.anonymous_user));
        } else {
            holder.userName.setText(review.getUsername() != null ? review.getUsername() : "User");
        }

        // 2. Populate Difficulty Rating
        holder.reviewRating.setText("⭐ " + review.getDifficulty() + ".0");

        // 3. HOW IT IS USED: The call remains exactly the same!
        // It passes the raw MongoDB String to our updated API-24 friendly method below.
        String formattedDate = convertIsoToReadableDate(review.getCreatedAt());
        holder.reviewDate.setText(formattedDate);

        // 4. Populate Review text content
        String mainText = review.getReviewText() != null ? review.getReviewText() : "";
        String studyHoursSubtext = "\nEstimated Weekly Study: " + (int) review.getStudyHours() + " hours";
        holder.reviewText.setText(mainText + "\n" + studyHoursSubtext);

        // 5. Handle Report action click
        holder.reportButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Review content reported.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    /**
     * API Level 24+ Compatible Date Formatter
     * Converts "2026-05-19T20:22:58.000Z" -> "May 19, 2026"
     */
    private String convertIsoToReadableDate(String isoStringFromServer) {
        if (isoStringFromServer == null || isoStringFromServer.isEmpty()) {
            return "";
        }
        try {
            // Determine structure if MongoDB included millisecond fields or omitted them
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            if (!isoStringFromServer.contains(".")) {
                sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
            }

            Date date = sourceFormat.parse(isoStringFromServer);

            // Output format mapping context setup
            SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            targetFormat.setTimeZone(TimeZone.getDefault());

            return targetFormat.format(date);
        } catch (Exception e) {
            // Safe fallback: slice the date characters "yyyy-MM-dd" out of the string if parse errors happen
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