package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.model.UserSession;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHANGE_PASSWORD = 1001;
    private TextView infoValue1, infoValue2;

    // Track fetched reviews and calculated metrics to pass to the next activity
    private final List<Review> fetchedProfessorReviews = new ArrayList<>();
    private double calculatedProfessorAvgRating = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ImageView navHome = findViewById(R.id.navHome);
        UserSession session = new UserSession(this);
        String fullName = session.getFullName();
        String username = session.getUsername();
        String email = session.getEmail();
        String role = session.getRole();
        String date = session.getCreationDate();

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ImageView navSearch = findViewById(R.id.navSearch);
        navSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        View logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        View changePasswordAction = findViewById(R.id.changePasswordAction);
        changePasswordAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD);
            }
        });


        TextView infoValue1 = findViewById(R.id.infoValue1);
        TextView infoLabel1 = findViewById(R.id.infoLabel1);
        infoValue2 = findViewById(R.id.infoValue2);
        TextView infoLabel2 = findViewById(R.id.infoLabel2);
        TextView primaryActionLabel = findViewById(R.id.primaryActionLabel);
        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);

        profileName.setText(fullName);
        profileEmail.setText(email);

        boolean isProfessor = "professor".equalsIgnoreCase(role);

        if (isProfessor) {
            if (date != null && !date.isEmpty()) {
                infoValue1.setText(calculateActiveDuration(date));
            } else {
                infoValue1.setText("0 mos");
            }
            infoLabel1.setText(getString(R.string.courses_count));
            infoValue2.setText("-/5");
            infoLabel2.setText(getString(R.string.rating_label));
            primaryActionLabel.setText(getString(R.string.see_my_reviews));
            if (username != null && !username.isEmpty()) {
                fetchAndCalculateProfessorMetrics(username);
            }
        } else {
            if (date != null && !date.isEmpty()) {
                infoValue1.setText(calculateActiveDuration(date));
            } else {
                infoValue1.setText("0 mos");
            }
            infoLabel1.setText(getString(R.string.years_member));
            infoValue2.setText("-");
            infoLabel2.setText(getString(R.string.reviews_count));
            primaryActionLabel.setText(getString(R.string.manage_my_reviews));

            if (username != null && !username.isEmpty()) {
                fetchAndCalculateUserReviewCount(username);
            }
        }

        View manageReviewsAction = findViewById(R.id.primaryAction);
        manageReviewsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (isProfessor) {
                    intent = new Intent(ProfileActivity.this, ProfessorSeeReviewsActivity.class);
                    intent.putExtra("PROFESSOR_REVIEWS_LIST", (Serializable) fetchedProfessorReviews);
                    intent.putExtra("PROFESSOR_AVG_RATING", calculatedProfessorAvgRating);
                } else {
                    intent = new Intent(ProfileActivity.this, ManageReviewsActivity.class);
                }
                startActivity(intent);
            }
        });
    }


    private String calculateActiveDuration(String isoDateString) {
        try {
            // 1. Parse the server ISO timestamp (e.g., "2026-05-18T14:30:00Z") into a Local Date
            java.time.Instant creationInstant = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                creationInstant = Instant.parse(isoDateString);
            }
            java.time.LocalDate creationDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                creationDate = creationInstant.atZone(ZoneId.systemDefault()).toLocalDate();
            }

            // 2. Get today's local date
            java.time.LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
            }

            // 3. Calculate the absolute period gap between registration and today
            java.time.Period period = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                period = Period.between(creationDate, today);
            }
            int years = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                years = period.getYears();
            }
            int months = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                months = period.getMonths();
            }

            // 4. Return a compact, clean string that fits beautifully in your layout
            if (years > 0) {
                return years + " yr, " + months + " mo";
            } else {
                // If they registered less than a year ago, just show the months
                return months + " mo";
            }

        } catch (Exception e) {
            android.util.Log.e("LectureLensDebug", "Error parsing account creation date string", e);
            return "New User"; // Clean safe fallback UI presentation
        }
    }

    private void fetchAndCalculateProfessorMetrics(String username) {
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
                    List<Review> localReviewsList = new ArrayList<>();

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

                        localReviewsList.add(review);
                    }

                    double avgRating = 0.0;
                    if (!localReviewsList.isEmpty()) {
                        avgRating = Math.round((ratingSum / localReviewsList.size()) * 10.0) / 10.0;
                    }

                    final double finalAvgRating = avgRating;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        fetchedProfessorReviews.clear();
                        fetchedProfessorReviews.addAll(localReviewsList);
                        calculatedProfessorAvgRating = finalAvgRating;

                        if (infoValue2 != null) {
                            if (fetchedProfessorReviews.isEmpty()) {
                                infoValue2.setText("N/A");
                            } else {
                                infoValue2.setText(String.format(Locale.US, "%.1f/5", finalAvgRating));
                            }
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating professor reviews metrics", e);
            }
        }).start();
    }

    private void fetchAndCalculateUserReviewCount(String username) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/reviews/user/" + username);
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

                    // Parse payload into a temporary array array block to derive structural size data
                    JSONArray jsonArray = new JSONArray(response.toString());
                    final int count = jsonArray.length();

                    // Dispatch result seamlessly onto main thread run loop loop safely
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (infoValue2 != null) {
                            infoValue2.setText(String.valueOf(count));
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating user metrics profile stats count", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (infoValue2 != null) {
                        infoValue2.setText("0"); // Fallback safety state if offline
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD && resultCode == RESULT_OK) {
            View alertBox = findViewById(R.id.alertBox);
            if (alertBox != null) {
                alertBox.setVisibility(View.VISIBLE);
                // Hide after 3000ms
                alertBox.postDelayed(() -> alertBox.setVisibility(View.GONE), 3000);
            }
        }
    }
}
