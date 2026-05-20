package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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
import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.java.ReviewAdapter;

public class CourseDetailsActivity extends AppCompatActivity {

    private TextView courseCode, courseName, largeRating, reviewCount, difficulty, hours;

    // Dynamic review list infrastructure variables
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        courseCode = findViewById(R.id.courseCode);
        courseName = findViewById(R.id.courseName);
        largeRating = findViewById(R.id.largeRating);
        reviewCount = findViewById(R.id.reviewCount);
        difficulty = findViewById(R.id.difficulty);
        hours = findViewById(R.id.hours);

        // Bind the new RecyclerView from your updated XML file
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList, true);

        // Connect the layout settings and dynamic adapter
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        boolean isProfessor = getIntent().getBooleanExtra("isProfessor", false);
        Course course = (Course) getIntent().getSerializableExtra("CHOSEN_COURSE");

        if (course != null) {
            populateUiElements(course);
            fetchCourseReviews(course.getId()); // Triggers the backend check matching this course ID
        } else {
            Toast.makeText(this, "Error: Could not display course data.", Toast.LENGTH_SHORT).show();
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        View btnWriteReview = findViewById(R.id.btnWriteReview);
        if (isProfessor) {
            btnWriteReview.setVisibility(View.GONE);
        } else {
            btnWriteReview.setOnClickListener(v -> {
                Intent intent = new Intent(CourseDetailsActivity.this, CourseReviewActivity.class);
                intent.putExtra("CHOSEN_COURSE", course);
                startActivity(intent);
            });
        }

        // Navigation listeners
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navSearch).setOnClickListener(v -> {
            startActivity(new Intent(CourseDetailsActivity.this, SearchActivity.class));
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailsActivity.this, ProfileActivity.class);
            intent.putExtra("isProfessor", isProfessor);
            startActivity(intent);
            finish();
        });
    }

    private void populateUiElements(Course course) {
        courseCode.setText(course.getCode());
        courseName.setText(course.getTitle());
        largeRating.setText(String.valueOf(course.getRating()));
        reviewCount.setText("0 Reviews");

        if (difficulty != null) {
            difficulty.setText(course.getDifficulty());
        }
        if (hours != null) {
            hours.setText(course.getHours());
        }
    }

    /**
     * Downloads and filters course reviews out of your Spring Boot endpoints
     */
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

                        // Live sync text metrics based on query output results
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
}
