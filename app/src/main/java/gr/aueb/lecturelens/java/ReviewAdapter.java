package gr.aueb.lecturelens.java;

import android.util.Log;
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
    private OnReviewActionListener actionListener;
    private boolean isProfessorView;

    public interface OnReviewActionListener {
        void onEditItem(Review review);
        void onDeleteItem(Review review, int position);
    }

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
        this.isProfessorView = false;
    }

    public ReviewAdapter(List<Review> reviewList, boolean isProfessorView) {
        this.reviewList = reviewList;
        this.isProfessorView = isProfessorView;
    }

    public ReviewAdapter(List<Review> reviewList, OnReviewActionListener actionListener) {
        this.reviewList = reviewList;
        this.actionListener = actionListener;
        this.isProfessorView = false;
    }

    public ReviewAdapter(List<Review> reviewList, OnReportListener reportListener) {
        this.reviewList = reviewList;
        this.reportListener = reportListener;
        this.isProfessorView = false;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (actionListener != null) ? R.layout.item_manage_review : R.layout.item_review;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        if (holder.userName != null) {
            if (review.isAnonymous() && !isProfessorView) {
                holder.userName.setText(holder.itemView.getContext().getString(R.string.anonymous_user));
            } else {
                holder.userName.setText(review.getUsername() != null ? review.getUsername() : "User");
            }
        }

        boolean isCourseReview = review.getCourseId() != null && !review.getCourseId().isEmpty();

        if (isCourseReview) {
            if (holder.courseName != null) {
                holder.courseName.setVisibility(View.VISIBLE);
                holder.courseName.setText(review.getCourseTitle() != null && !review.getCourseTitle().isEmpty() ? review.getCourseTitle() : "Course Review");
            }
            if (holder.professorName != null) {
                holder.professorName.setVisibility(View.GONE);
            }
        } else {
            if (holder.professorName != null) {
                holder.professorName.setVisibility(View.VISIBLE);
                holder.professorName.setText(review.getProfessorName() != null && !review.getProfessorName().isEmpty() ? review.getProfessorName() : "Professor Review");

                if (holder.courseName != null) {
                    holder.courseName.setVisibility(View.GONE);
                }
            } else if (holder.courseName != null) {
                holder.courseName.setVisibility(View.VISIBLE);
                holder.courseName.setText(review.getProfessorName() != null && !review.getProfessorName().isEmpty() ? review.getProfessorName() : "Professor Review");
            }
        }

        holder.reviewRating.setText("⭐ " + String.format(Locale.US, "%.1f", review.getRating()));

        holder.reviewDate.setText(convertIsoToReadableDate(review.getCreatedAt()));

        String mainText = review.getReviewText() != null ? review.getReviewText() : "";
        if (!isProfessorView && review.getStudyHours() > 0) {
            String studyHoursSubtext = "\nEstimated Weekly Study: " + (int) review.getStudyHours() + " hours";
            holder.reviewText.setText(mainText + studyHoursSubtext);
        } else {
            holder.reviewText.setText(mainText);
        }

        if (actionListener != null && !isProfessorView) {
            if (holder.btnEdit != null) holder.btnEdit.setOnClickListener(v -> actionListener.onEditItem(review));
            if (holder.btnDelete != null) holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteItem(review, position));
        } else if (!isProfessorView) {
            if (holder.reportButton != null) {
                holder.reportButton.setOnClickListener(v -> {
                    if (reportListener != null) {
                        reportListener.onReportClick(review);
                    }
                });
            }
        } else {
            if (holder.reportButton != null) holder.reportButton.setVisibility(View.VISIBLE);
            if (holder.btnEdit != null) holder.btnEdit.setVisibility(View.GONE);
            if (holder.btnDelete != null) holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    private String convertIsoToReadableDate(String isoStringFromServer) {
        if (isoStringFromServer == null || isoStringFromServer.isEmpty()) return "";
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
            return isoStringFromServer.length() >= 10 ? isoStringFromServer.substring(0, 10) : isoStringFromServer;
        }
    }

    public interface OnReportListener {
        void onReportClick(Review review);
    }
    private OnReportListener reportListener;

    public void setOnReportListener(OnReportListener reportListener) {
        this.reportListener = reportListener;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, courseName, professorName, reviewRating, reviewDate, reviewText, reportButton;
        View btnEdit, btnDelete;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewRating = itemView.findViewById(R.id.reviewRating);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            reviewText = itemView.findViewById(R.id.reviewText);
            userName = itemView.findViewById(R.id.userName);
            courseName = itemView.findViewById(R.id.courseName);
            professorName = itemView.findViewById(R.id.professorName);
            reportButton = itemView.findViewById(R.id.reportButton);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}