package gr.aueb.lecturelens;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
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

import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.Professor;
import gr.aueb.lecturelens.java.ReviewAdapter;
import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.model.UserSession;

public class ManageReviewsActivity extends AppCompatActivity implements ReviewAdapter.OnReviewActionListener {

    private String currentUsername;
    private List<Review> userReviewsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ReviewAdapter reviewsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_reviews);

        UserSession session = new UserSession(getApplicationContext());
        currentUsername = session.getUsername();

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.reviewsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        boolean isProfessor = "professor".equals(session.getRole());

        if (isProfessor) {
            reviewsAdapter = new ReviewAdapter(userReviewsList, true);
        } else {
            reviewsAdapter = new ReviewAdapter(userReviewsList, this);
        }
        recyclerView.setAdapter(reviewsAdapter);

        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO_PAGE", 0);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navSearch).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO_PAGE", 1);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO_PAGE", 2);
            startActivity(intent);
            finish();
        });

        if (currentUsername != null && !currentUsername.isEmpty()) {
            fetchUserReviews();
        } else {
            Toast.makeText(this, "Session invalid. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserReviews() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/reviews/user/" + currentUsername);
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
                    userReviewsList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Review review = new Review();
                        review.setId(jsonObject.optString("id"));
                        review.setCourseId(jsonObject.optString("courseId"));
                        review.setCourseTitle(jsonObject.optString("courseTitle"));
                        review.setUsername(jsonObject.optString("username"));
                        review.setRating((float) jsonObject.optDouble("rating"));
                        review.setDifficulty(jsonObject.optInt("difficulty"));
                        review.setStudyHours((float) jsonObject.optDouble("studyHours"));
                        review.setReviewText(jsonObject.optString("reviewText"));
                        review.setAnonymous(jsonObject.optBoolean("isAnonymous"));
                        review.setCreatedAt(jsonObject.optString("createdAt"));

                        userReviewsList.add(review);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        reviewsAdapter.notifyDataSetChanged();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error downloading user reviews data", e);
            }
        }).start();

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/professor-reviews/user/" + currentUsername);
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

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Review review = new Review();
                        review.setId(jsonObject.optString("id"));
                        review.setProfessorId(jsonObject.optString("professorId"));
                        review.setProfessorName(jsonObject.optString("professorName"));
                        review.setUsername(jsonObject.optString("username"));
                        review.setRating((float) jsonObject.optDouble("rating"));
                        review.setReviewText(jsonObject.optString("reviewText"));
                        review.setAnonymous(jsonObject.optBoolean("isAnonymous"));
                        review.setCreatedAt(jsonObject.optString("createdAt"));

                        Log.d("LectureLensDebug", jsonObject.toString());

                        userReviewsList.add(review);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        reviewsAdapter.notifyDataSetChanged();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error downloading user reviews data", e);
            }
        }).start();
    }


    @Override
    public void onEditItem(Review review) {
        if (review.getCourseId() != null && !review.getCourseId().isEmpty()) {
            fetchCourseById(review.getCourseId(), review);
        } else if (review.getProfessorId() != null && !review.getProfessorId().isEmpty()) {
            fetchProfessorById(review.getProfessorId(), review);
        }
    }

    @Override
    public void onDeleteItem(Review review, int position) {
        Log.e("LectureLensDebug", "Deleting reviewId: " + review.getId());
        showDeleteConfirmationDialog(review, position);
    }

    private void showDeleteConfirmationDialog(Review review, int position) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_delete_confirmation_dialog);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            deleteReviewFromServer(review, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteReviewFromServer(Review review, int position) {
        new Thread(() -> {
            try {
                String endpoint;
                if (review.getCourseId() != null && !review.getCourseId().isEmpty()) {
                    endpoint = "reviews";
                } else {
                    endpoint = "professor-reviews";
                }

                URL url = new URL("http://10.0.2.2:8081/api/" + endpoint + "/" + review.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        userReviewsList.remove(position);
                        reviewsAdapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Delete request crash", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Network error during deletion", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void fetchCourseById(String courseId, Review review) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/courses/" + courseId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONObject json = new JSONObject(response.toString());

                    Course course = new Course(
                            json.optString("id"),
                            json.optString("code"),
                            json.optString("title"),
                            json.optInt("semester", 0),
                            json.optInt("ects", 0),
                            json.optString("professorName"),
                            json.optDouble("rating", 0.0),
                            json.optDouble("difficulty"),
                            json.optDouble("hours"),
                            json.optString("description")
                    );

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Intent intent = new Intent(this, CourseReviewActivity.class);
                        intent.putExtra("CHOSEN_COURSE", course);
                        intent.putExtra("isEditMode", true);
                        intent.putExtra("reviewId", review.getId());
                        intent.putExtra("rating", review.getRating());
                        intent.putExtra("difficulty", review.getDifficulty());
                        intent.putExtra("hours", review.getStudyHours());
                        intent.putExtra("reviewText", review.getReviewText());
                        intent.putExtra("isAnonymousSaved", review.isAnonymous());
                        startActivity(intent);
                    });

                } else {
                    Log.e("LectureLensDebug", "Failed fetching course: " + responseCode);
                    showToastOnUi("Could not load course data.");
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching course by id", e);
                showToastOnUi("Network error loading course.");
            }
        }).start();
    }

    private void fetchProfessorById(String professorId, Review review) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/professors/" + professorId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONObject json = new JSONObject(response.toString());

                    Professor professor = new Professor(
                            json.optString("id"),
                            json.optString("firstName", "First Name"),
                            json.optString("lastName", "Last Name"),
                            json.optString("title", "title"),
                            json.optDouble("rating", 0.0)
                    );

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Intent intent = new Intent(this, ProfessorReviewActivity.class);
                        intent.putExtra("CHOSEN_PROFESSOR", professor);
                        intent.putExtra("isEditMode", true);
                        intent.putExtra("reviewId", review.getId());
                        intent.putExtra("rating", review.getRating());
                        intent.putExtra("reviewText", review.getReviewText());
                        intent.putExtra("isAnonymousSaved", review.isAnonymous());
                        startActivity(intent);
                    });

                } else {
                    Log.e("LectureLensDebug", "Failed fetching professor: " + responseCode);
                    showToastOnUi("Could not load professor data.");
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching professor by id", e);
                showToastOnUi("Network error loading professor.");
            }
        }).start();
    }

    private void showToastOnUi(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

}