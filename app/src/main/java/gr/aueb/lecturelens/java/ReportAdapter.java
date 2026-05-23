package gr.aueb.lecturelens.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import java.util.List;

import gr.aueb.lecturelens.R;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<ReportResponse> reportList;
    private OnReportActionListener actionListener;

    public interface OnReportActionListener {
        void onDismiss(ReportResponse report, int position);
        void onDeleteReview(ReportResponse report, int position);
    }

    public ReportAdapter(List<ReportResponse> reportList, OnReportActionListener actionListener) {
        this.reportList = reportList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportResponse report = reportList.get(position);

        // Bind data fields cleanly to match the layout context mapping definitions
        holder.tvReviewText.setText(report.getReviewText());
        holder.tvCourseName.setText("Course ID: " + report.getCourseId());
        holder.tvReportedCountText.setText("By: " + report.getReportedBy() + " | Author: " + report.getAuthorUsername());

        // Wire up interacting listener callbacks tracking the item instances
        holder.btnIgnore.setOnClickListener(v -> actionListener.onDismiss(report, position));
        holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteReview(report, position));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void removeAt(int position) {
        reportList.remove(position);
        notifyItemRemoved(position);
        // Notifies structural modifications keeping position references aligned
        notifyItemRangeChanged(position, reportList.size() - position);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewText, tvCourseName, tvReportedCountText;
        MaterialButton btnIgnore, btnDelete;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            // Matched view variables directly to the IDs inside item_report.xml
            tvReviewText = itemView.findViewById(R.id.reviewText);
            tvCourseName = itemView.findViewById(R.id.courseName);
            tvReportedCountText = itemView.findViewById(R.id.reportedCountText);
            btnIgnore = itemView.findViewById(R.id.btnIgnore);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}