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
import gr.aueb.lecturelens.java.CourseChipAdapter;
import gr.aueb.lecturelens.java.Professor;
import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.java.ReviewAdapter;

public class ProfessorDetailsActivity extends AppCompatActivity implements CourseChipAdapter.OnCourseChipClickListener {

    private TextView profName, profDept, largeRating, reviewCount;
    private RecyclerView coursesRecyclerView, reviewsRecyclerView;
    private CourseChipAdapter courseChipAdapter;
    private ReviewAdapter reviewAdapter;

    private final List<Course> courseList = new ArrayList<>();
    private final List<Review> reviewList = new ArrayList<>();
    private String professorId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_details);

        professorId = getIntent().getStringExtra("PROFESSOR_ID");

        // 2. Fallback: If null, try to get it from the serialized Professor object
        if (professorId == null || professorId.isEmpty()) {
            Professor prof = (Professor) getIntent().getSerializableExtra("CHOSEN_PROFESSOR");
            if (prof != null) {
                professorId = String.valueOf(prof.getId());
                Log.d("LectureLensDebug", "Recovered ID from Object: " + professorId);
            }
        }

        Log.d("LectureLensDebug", "Final professorId to use: " + professorId);

        profName = findViewById(R.id.profName);
        profDept = findViewById(R.id.profDept);
        largeRating = findViewById(R.id.largeRating);
        reviewCount = findViewById(R.id.reviewCount);

        coursesRecyclerView = findViewById(R.id.professorCoursesRecyclerView);
        reviewsRecyclerView = findViewById(R.id.professorReviewsRecyclerView);

        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        courseChipAdapter = new CourseChipAdapter(courseList, this);
        coursesRecyclerView.setAdapter(courseChipAdapter);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList, false);
        reviewsRecyclerView.setAdapter(reviewAdapter);

        boolean isProfessor = getIntent().getBooleanExtra("isProfessor", false);
        Professor prof = (Professor) getIntent().getSerializableExtra("CHOSEN_PROFESSOR");

        if (prof != null) {
            populateUiElements(prof);
            fetchProfessorDetails(professorId);
        } else {
            Toast.makeText(this, "Error: Could not display professor data.", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnWriteReview = findViewById(R.id.btnWriteReview);
        if (isProfessor) {
            btnWriteReview.setVisibility(View.GONE);
        } else {
            btnWriteReview.setOnClickListener(v -> {
                Intent intent = new Intent(ProfessorDetailsActivity.this, ProfessorReviewActivity.class);
                intent.putExtra("PROFESSOR_ID", professorId);
                intent.putExtra("CHOSEN_PROFESSOR", prof);
                startActivity(intent);
            });
        }

        setupBottomNavigation(isProfessor);

        if (!professorId.isEmpty()) {
            fetchProfessorDetails(professorId);
            fetchProfessorCourses(professorId); // ADD THIS LINE to fetch the courses!
        }
    }

    private void setupBottomNavigation(boolean isProfessor) {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(ProfessorDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navSearch).setOnClickListener(v -> {
            startActivity(new Intent(ProfessorDetailsActivity.this, SearchActivity.class));
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(ProfessorDetailsActivity.this, ProfileActivity.class);
            intent.putExtra("isProfessor", isProfessor);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onCourseChipClick(Course course) {
        Intent intent = new Intent(ProfessorDetailsActivity.this, CourseDetailsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        startActivity(intent);
    }

    private void populateUiElements(Professor prof) {
        profName.setText(prof.getFullName());
        profDept.setText(prof.getTitle() + " • Informatics Department");
        largeRating.setText(prof.getRating() == 0.0 ? "N/A" : String.format("%.1f", prof.getRating()));
        reviewCount.setText(String.format("(%d reviews)", reviewList.size()));
    }


    private void fetchProfessorDetails(String professorId) {
        Log.d("LectureLensDebug", "Entering fetchProfessorDetails for ID: " + professorId);
        new Thread(() -> {
            try {
                // FIXED URL: Targets the specific professor instead of fetching ALL reviews
                URL url = new URL("http://10.0.2.2:8081/api/professor-reviews/" + professorId);
                Log.d("LectureLensDebug", "Attempting connection to: " + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                int responseCode = conn.getResponseCode();
                Log.d("LectureLensDebug", "Response Code received: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) response.append(inputLine);
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<Review> parsedReviews = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Review review = new Review();
                        review.setId(jsonObject.optString("id"));
                        review.setProfessorId(jsonObject.optString("professorId"));
                        review.setUsername(jsonObject.optString("username"));
                        review.setReviewText(jsonObject.optString("reviewText"));
                        review.setRating((float) jsonObject.optDouble("rating", 0.0));
                        review.setAnonymous(jsonObject.optBoolean("isAnonymous"));
                        review.setCreatedAt(jsonObject.optString("createdAt"));

                        parsedReviews.add(review);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        reviewList.clear();
                        reviewList.addAll(parsedReviews);
                        reviewAdapter.notifyDataSetChanged();
                        reviewCount.setText(reviewList.size() + (reviewList.size() == 1 ? " Review" : " Reviews"));
                    });
                } else {
                    Log.e("LectureLensDebug", "HTTP Error: " + responseCode);
                    // Read error stream for more detail
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    Log.e("LectureLensDebug", "Error Body: " + errorReader.readLine());
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching reviews", e);
            }
        }).start();
    }

    /*
    private void fetchProfessorCourses(String professorId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/professors/" + professorId + "/details");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) response.append(inputLine);
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray coursesArray = jsonResponse.optJSONArray("matchingCourses");

                    List<Course> parsedCourses = new ArrayList<>();
                    if (coursesArray != null) {
                        for (int i = 0; i < coursesArray.length(); i++) {
                            JSONObject cObj = coursesArray.getJSONObject(i);
                            Course course = new Course(
                                    cObj.optString("id"),
                                    cObj.optString("code"),
                                    cObj.optString("title"),
                                    cObj.optInt("semester"),
                                    cObj.optInt("ects"),
                                    cObj.optString("professorName"),
                                    cObj.optDouble("rating", 0.0),
                                    cObj.optDouble("difficulty"),
                                    cObj.optDouble("hours"),
                                    cObj.optString("description")
                            );
                            parsedCourses.add(course);
                        }
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        courseList.clear();
                        courseList.addAll(parsedCourses);
                        courseChipAdapter.notifyDataSetChanged();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching courses", e);
            }
        }).start();
    }

     */
    private void fetchProfessorCourses(String professorId) {
        new Thread(() -> {
            try {
                // New simpler endpoint — no mapping table involved
                URL url = new URL("http://10.0.2.2:8081/api/professors/" + professorId + "/courses");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) response.append(inputLine);
                    in.close();

                    JSONArray coursesArray = new JSONArray(response.toString());
                    List<Course> parsedCourses = new ArrayList<>();

                    for (int i = 0; i < coursesArray.length(); i++) {
                        JSONObject cObj = coursesArray.getJSONObject(i);
                        parsedCourses.add(new Course(
                                cObj.optString("id", cObj.optString("_id")),
                                cObj.optString("code"),
                                cObj.optString("title"),
                                cObj.optInt("semester"),
                                cObj.optInt("ects"),
                                cObj.optString("professorName"),
                                cObj.optDouble("rating", 0.0),
                                cObj.optDouble("difficulty"),
                                cObj.optDouble("hours"),
                                cObj.optString("description")
                        ));
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        courseList.clear();
                        courseList.addAll(parsedCourses);
                        courseChipAdapter.notifyDataSetChanged();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching courses", e);
            }
        }).start();
    }
}


