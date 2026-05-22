package gr.aueb.lecturelens;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE_CHANGE_PASSWORD = 1001;
    private TextView infoValue1, infoValue2;
    private View alertBox;

    // Track fetched reviews and calculated metrics to pass to the next activity
    private final List<Review> fetchedProfessorReviews = new ArrayList<>();
    private double calculatedProfessorAvgRating = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the custom profile layout template
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        alertBox = view.findViewById(R.id.alertBox);

        // 2. Read user details dynamically out of the active user session cache layers
        UserSession session = new UserSession(requireContext());
        String fullName = session.getFullName();
        String username = session.getUsername();
        String email = session.getEmail();
        String role = session.getRole();
        String date = session.getCreationDate();

        // 3. Connect individual text views and data cards
        infoValue1 = view.findViewById(R.id.infoValue1);
        TextView infoLabel1 = view.findViewById(R.id.infoLabel1);
        infoValue2 = view.findViewById(R.id.infoValue2);
        TextView infoLabel2 = view.findViewById(R.id.infoLabel2);
        TextView primaryActionLabel = view.findViewById(R.id.primaryActionLabel);
        TextView profileName = view.findViewById(R.id.profileName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);

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

        // 5. Setup Action Click listeners targeting separate workflow sub-activities
        View logoutButton = view.findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        View changePasswordAction = view.findViewById(R.id.changePasswordAction);
        if (changePasswordAction != null) {
            changePasswordAction.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD);
            });
        }

        View manageReviewsAction = view.findViewById(R.id.primaryAction);
        if (manageReviewsAction != null) {
            manageReviewsAction.setOnClickListener(v -> {
                Intent intent;
                if (isProfessor) {
                    // Navigate to ProfessorSeeReviewsActivity, passing bundled metrics
                    intent = new Intent(getActivity(), ProfessorSeeReviewsActivity.class);
                    intent.putExtra("PROFESSOR_REVIEWS_LIST", (Serializable) fetchedProfessorReviews);
                    intent.putExtra("PROFESSOR_AVG_RATING", calculatedProfessorAvgRating);
                } else {
                    intent = new Intent(getActivity(), ManageReviewsActivity.class);
                }
                startActivity(intent);
            });
        }

        return view;

    }

    private String calculateActiveDuration(String isoDateString) {
        try {
            java.time.Instant creationInstant = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                creationInstant = Instant.parse(isoDateString);
            }
            java.time.LocalDate creationDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                creationDate = creationInstant.atZone(ZoneId.systemDefault()).toLocalDate();
            }

            java.time.LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
            }

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

            if (years > 0) {
                return years + " yr, " + months + " mo";
            } else {
                return months + " mo";
            }
        } catch (Exception e) {
            Log.e("LectureLensDebug", "Error parsing account creation date string", e);
            return "New User";
        }
    }

    private void fetchAndCalculateProfessorMetrics(String username) {
        new Thread(() -> {
            try {
                UserSession methodSession = new UserSession(requireContext());
                String professorFullName = methodSession.getFullName(); // e.g., "Panagiotis Katerinis"

                // URL-encode the string to turn spaces into %20 safely
                String encodedName = java.net.URLEncoder.encode(professorFullName, "UTF-8");

                // Construct the endpoint query URL
                URL url = new URL("http://10.0.2.2:8081/api/professor-reviews/by-name?fullName=" + encodedName);

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

                    // LOG 1: See the entire raw JSON string array coming from the server
                    Log.d("LectureLensDebug", "RAW JSON RESPONSE: " + response.toString());

                    JSONArray jsonArray = new JSONArray(response.toString());

                    // LOG 2: Check total item count parsed initially
                    Log.d("LectureLensDebug", "Total reviews parsed for professor: " + jsonArray.length());

                    double ratingSum = 0.0;
                    List<Review> localReviewsList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        // LOG 3: Print individual objects to see internal key-value pairings (id, professorName, rating)
                        Log.d("LectureLensDebug", "Processing Review index [" + i + "]: " + jsonObject.toString());

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

                    // LOG 4: Confirm calculated average output before dispatching to UI
                    Log.d("LectureLensDebug", "Calculated Average Rating: " + finalAvgRating);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        fetchedProfessorReviews.clear();
                        fetchedProfessorReviews.addAll(localReviewsList);
                        calculatedProfessorAvgRating = finalAvgRating;

                        if (isAdded() && infoValue2 != null) {
                            if (fetchedProfessorReviews.isEmpty()) {
                                infoValue2.setText("N/A");
                            } else {
                                infoValue2.setText(String.format(Locale.US, "%.1f/5", finalAvgRating));
                            }
                        }
                    });
                } else {
                    Log.e("LectureLensDebug", "Server error. Response code: " + conn.getResponseCode());
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating professor reviews metrics", e);
            }
        }).start();
    }

    private void fetchAndCalculateUserReviewCount(String username) {
        new Thread(() -> {
            int totalReviewsCount = 0;

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
                    while ((line = in.readLine()) != null) response.append(line.trim());
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    totalReviewsCount += jsonArray.length();
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating user course reviews metrics count", e);
            }

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
                    totalReviewsCount += jsonArray.length();
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating user professor reviews metrics count", e);
            }

            final int finalSum = totalReviewsCount;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded() && infoValue2 != null) {
                    infoValue2.setText(String.valueOf(finalSum));
                }
            });
        }).start();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD && resultCode == Activity.RESULT_OK) {
            if (alertBox != null) {
                alertBox.setVisibility(View.VISIBLE);
                alertBox.postDelayed(() -> {
                    if (isAdded() && alertBox != null) {
                        alertBox.setVisibility(View.GONE);
                    }
                }, 3000);
            }
        }
    }
}