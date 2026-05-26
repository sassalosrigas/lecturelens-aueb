package gr.aueb.lecturelens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.lecturelens.java.ReportAdapter;
import gr.aueb.lecturelens.java.ReportResponse;

public class AdminDashboardActivity extends AppCompatActivity implements ReportAdapter.OnReportActionListener {

    private TextView tvPendingCount, tvResolvedCount;
    private RecyclerView rvReports;
    private ReportAdapter reportAdapter;
    private List<ReportResponse> reportsList = new ArrayList<>();

    private final String BASE_URL = "http://10.0.2.2:8081/api/reports";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        rvReports = findViewById(R.id.rvReports);

        rvReports.setLayoutManager(new LinearLayoutManager(this));

        reportAdapter = new ReportAdapter(reportsList, this);
        rvReports.setAdapter(reportAdapter);

        fetchReportsData();
    }

    private void fetchReportsData() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line.trim());
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());

                    int pendingCounter = 0;
                    int resolvedCounter = 0;
                    final List<ReportResponse> temporaryList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String status = jsonObject.optString("status");
                        if ("PENDING".equalsIgnoreCase(status)) {
                            pendingCounter++;

                            ReportResponse report = new ReportResponse();
                            report.setId(jsonObject.optString("id"));
                            report.setReviewId(jsonObject.optString("reviewId"));
                            report.setCourseId(jsonObject.optString("courseId"));
                            report.setAuthorUsername(jsonObject.optString("authorUsername"));
                            report.setReportedBy(jsonObject.optString("reportedBy"));
                            report.setReviewText(jsonObject.optString("reviewText"));
                            report.setStatus(status);

                            temporaryList.add(report);
                        } else if ("DISMISSED".equalsIgnoreCase(status) || "DELETED".equalsIgnoreCase(status)) {
                            resolvedCounter++;
                        }
                    }

                    final int finalPending = pendingCounter;
                    final int finalResolved = resolvedCounter;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        tvPendingCount.setText(String.valueOf(finalPending));
                        tvResolvedCount.setText(String.valueOf(finalResolved));

                        reportsList.clear();
                        reportsList.addAll(temporaryList);
                        reportAdapter.notifyDataSetChanged();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error loading administrator reports metrics", e);
                showToastOnUi("Network error loading dashboard reports.");
            }
        }).start();
    }

    @Override
    public void onDismiss(ReportResponse report, int position) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/" + report.getId() + "/status?status=DISMISSED");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reportAdapter.removeAt(position);
                        fetchReportsData();
                        Toast.makeText(this, "Report dismissed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to dismiss report", Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Exception managing dismiss request execution pipeline", e);
                showToastOnUi("Network error updating report status.");
            }
        }).start();
    }

    @Override
    public void onDeleteReview(ReportResponse report, int position) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/" + report.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        reportAdapter.removeAt(position);
                        fetchReportsData();
                        Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to execute target row database erasure", Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Exception during review elimination loop operations", e);
                showToastOnUi("Network error during deletion request.");
            }
        }).start();
    }

    private void showToastOnUi(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }
}
