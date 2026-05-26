package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.java.ReviewAdapter;
import gr.aueb.lecturelens.model.UserSession;

public class ProfessorSeeReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_see_reviews);

        List<Review> professorReviewsList = (List<Review>) getIntent().getSerializableExtra("PROFESSOR_REVIEWS_LIST");
        double professorAvgRating = getIntent().getDoubleExtra("PROFESSOR_AVG_RATING", -1.0);

        if (professorReviewsList == null || professorAvgRating == -1.0) {
            UserSession session = new UserSession(this);
            String username = session.getUsername();
            if (username != null && !username.isEmpty()) {
                fetchProfessorMetrics(username);
            }
        } else {
            updateUi(professorReviewsList, professorAvgRating);
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorSeeReviewsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        ImageView navSearch = findViewById(R.id.navSearch);
        navSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorSeeReviewsActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ImageView navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorSeeReviewsActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void updateUi(List<Review> reviews, double avgRating) {
        TextView ratingDisplayHeader = findViewById(R.id.profAvgRating);
        if (ratingDisplayHeader != null) {
            ratingDisplayHeader.setText(String.format(Locale.US, "%.1f", avgRating));
        }

        RecyclerView recyclerView = findViewById(R.id.professorReviewsRecyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            ReviewAdapter adapter = new ReviewAdapter(reviews);

            UserSession session = new UserSession(this);
            adapter.setOnReportListener(review -> {
                submitReportToDatabase(review, session.getUsername());
            });

            recyclerView.setAdapter(adapter);
        }
    }

    private void fetchProfessorMetrics(String username) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/professor-reviews/user/" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line.trim());
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    double ratingSum = 0.0;
                    List<Review> reviews = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Review review = new Review();
                        review.setId(jsonObject.optString("id"));
                        review.setProfessorId(jsonObject.optString("professorId"));
                        review.setProfessorName(jsonObject.optString("professorName"));
                        review.setUsername(jsonObject.optString("username"));

                        double singleRating = jsonObject.optDouble("rating", 0.0);
                        review.setRating((float) singleRating);
                        ratingSum += singleRating;

                        review.setReviewText(jsonObject.optString("reviewText"));
                        review.setAnonymous(jsonObject.optBoolean("isAnonymous"));
                        review.setCreatedAt(jsonObject.optString("createdAt"));

                        reviews.add(review);
                    }

                    double avgRating = 0.0;
                    if (!reviews.isEmpty()) {
                        avgRating = Math.round((ratingSum / reviews.size()) * 10.0) / 10.0;
                    }

                    final double finalAvgRating = avgRating;
                    new Handler(Looper.getMainLooper()).post(() -> updateUi(reviews, finalAvgRating));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching professor metrics", e);
            }
        }).start();
    }

    private void submitReportToDatabase(Review review, String reporterUsername) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/reports");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("reviewId", review.getId());
                jsonParam.put("courseId", review.getProfessorId());
                jsonParam.put("authorUsername", review.getUsername());
                jsonParam.put("reportedBy", reporterUsername);
                jsonParam.put("reviewText", review.getReviewText());
                jsonParam.put("status", "PENDING");

                conn.getOutputStream().write(jsonParam.toString().getBytes("UTF-8"));

                int responseCode = conn.getResponseCode();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        Toast.makeText(ProfessorSeeReviewsActivity.this, "Review reported successfully. Admins will review it.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfessorSeeReviewsActivity.this, "Failed to submit report. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error submitting report", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(ProfessorSeeReviewsActivity.this, "Network error. Report not sent.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
