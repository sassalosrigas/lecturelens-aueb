package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import gr.aueb.lecturelens.java.ReviewAdapter;
import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.model.UserSession;

public class CourseDetailsActivity extends AppCompatActivity {

    private TextView courseCode, courseName,courseProfessor, largeRating, reviewCount, difficulty, hours;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private TextView btnWriteReview;

    private boolean userHasReview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        courseCode = findViewById(R.id.courseCode);
        courseName = findViewById(R.id.courseName);
        courseProfessor = findViewById(R.id.courseProfessor);
        largeRating = findViewById(R.id.largeRating);
        reviewCount = findViewById(R.id.reviewCount);
        difficulty = findViewById(R.id.difficulty);
        hours = findViewById(R.id.hours);

        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList, true);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        boolean isProfessor = getIntent().getBooleanExtra("isProfessor", false);
        Course course = (Course) getIntent().getSerializableExtra("CHOSEN_COURSE");

        if (course != null) {
            populateUiElements(course);
            fetchCourseReviews(course.getId());
            if (!isProfessor) {
                checkExistingReview(course);
            }
        } else {
            Toast.makeText(this, "Error: Could not display course data.", Toast.LENGTH_SHORT).show();
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnWriteReview = findViewById(R.id.btnWriteReview);
        if (isProfessor) {
            btnWriteReview.setVisibility(View.GONE);
        } else {
            btnWriteReview.setOnClickListener(v -> {
                checkExistingReview(course);
            });
        }

        // Navigation listeners
        // 1. HOME BUTTON: Brings back the warm MainActivity and jumps straight to the Home page
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailsActivity.this, MainActivity.class);
            // FLAG_ACTIVITY_CLEAR_TOP brings back the active instance instead of recreating it
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO_PAGE", 0); // Target position index 0 (HomeFragment)
            startActivity(intent);
            finish();
        });

        // 2. SEARCH BUTTON: Brings back MainActivity and slides smoothly over to the Search screen tab
        findViewById(R.id.navSearch).setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO_PAGE", 1); // Target position index 1 (SearchFragment)
            startActivity(intent);
            finish();
        });

        // 3. PROFILE BUTTON: Brings back MainActivity and slides directly to the Profile card layout
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO_PAGE", 2); // Target position index 2 (ProfileFragment)
            intent.putExtra("isProfessor", isProfessor); // Carry over your teammate's role flags
            startActivity(intent);
            finish();
        });
    }

    private void populateUiElements(Course course) {
        courseCode.setText(course.getCode());
        courseName.setText(course.getTitle());
        largeRating.setText(String.valueOf(course.getRating()));
        courseProfessor.setText(course.getProfessorName());
        reviewCount.setText("0 Reviews");

        if (difficulty != null) {
            difficulty.setText(String.valueOf(course.getDifficulty()));
        }
        if (hours != null) {
            hours.setText(String.valueOf(course.getHours()));
        }
    }

    private void fetchCourseReviews(String courseId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/reviews");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<Review> parsedReviews = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String targetCourseId = jsonObject.optString("courseId");

                        // Check if this item belongs to our current active course
                        if (targetCourseId.equals(courseId)) {
                            Review review = new Review();
                            review.setId(jsonObject.optString("id"));
                            review.setCourseId(targetCourseId);
                            review.setRating((float) jsonObject.optDouble("rating", 0.0));
                            review.setUsername(jsonObject.optString("username"));
                            review.setDifficulty(jsonObject.optInt("difficulty"));
                            review.setStudyHours((float) jsonObject.optDouble("studyHours"));
                            review.setReviewText(jsonObject.optString("reviewText"));
                            review.setAnonymous(jsonObject.optBoolean("isAnonymous"));
                            review.setCreatedAt(jsonObject.optString("createdAt"));


                            parsedReviews.add(review);
                        }
                    }

                    // Safely dispatch list view rendering onto your application main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        reviewList.clear();
                        reviewList.addAll(parsedReviews);
                        reviewAdapter.notifyDataSetChanged();

                        reviewCount.setText(reviewList.size() + (reviewList.size() == 1 ? " Review" : " Reviews"));
                    });

                } else {
                    Log.e("LectureLensDebug", "Failed fetching backend reviews: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error inside fetch stream runtime", e);
            }
        }).start();
    }

    private void checkExistingReview(Course course) {
        String username = new UserSession(this).getUsername();

        new Thread(() -> {
            try {
                String endpoint = "http://10.0.2.2:8081/api/reviews/check"
                        + "?courseId=" + course.getId()
                        + "&username=" + username;

                HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Review exists — parse it and open in edit mode
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONObject existing = new JSONObject(response.toString());

                    new Handler(Looper.getMainLooper()).post(() -> {
                        btnWriteReview.setText(getString(R.string.edit_review));

                        btnWriteReview.setOnClickListener(v -> {
                            Intent intent = new Intent(this, CourseReviewActivity.class);
                            intent.putExtra("CHOSEN_COURSE", course);
                            intent.putExtra("isEditMode", true);
                            intent.putExtra("rating", (float) existing.optDouble("rating", 4.0));
                            intent.putExtra("difficulty", existing.optInt("difficulty", 3));
                            intent.putExtra("hours", (float) existing.optDouble("studyHours", 5.0));
                            intent.putExtra("reviewText", existing.optString("reviewText", ""));
                            String parsedId = existing.has("id") ? existing.optString("id") : existing.optString("_id");
                            Log.e("LectureLensDebug", "Parsed reviewId: " + parsedId);
                            intent.putExtra("reviewId", parsedId);
                            startActivity(intent);
                        });
                    });

                } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    userHasReview = false;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        btnWriteReview.setText(getString(R.string.write_a_review));

                        btnWriteReview.setOnClickListener(v -> {
                            Intent intent = new Intent(this, CourseReviewActivity.class);
                            intent.putExtra("CHOSEN_COURSE", course);
                            startActivity(intent);
                        });
                    });

                } else {
                    Log.e("LectureLensDebug", "Check review failed: " + responseCode);
                    showToastOnUi("Could not check for existing review.");
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error checking existing review", e);
                showToastOnUi("Network error. Try again.");
            }
        }).start();
    }

    private void showToastOnUi(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }
}