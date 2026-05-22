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
import gr.aueb.lecturelens.model.UserSession;

public class ProfessorDetailsActivity extends AppCompatActivity implements CourseChipAdapter.OnCourseChipClickListener {

    private TextView profName, profDept, largeRating, reviewCount;
    private RecyclerView coursesRecyclerView, reviewsRecyclerView;
    private CourseChipAdapter courseChipAdapter;
    private ReviewAdapter reviewAdapter;
    private final List<Course> courseList = new ArrayList<>();
    private final List<Review> reviewList = new ArrayList<>();
    private TextView btnWriteReview;
    private boolean userHasReview = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_details);

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
        reviewAdapter = new ReviewAdapter(reviewList, true);
        reviewsRecyclerView.setAdapter(reviewAdapter);

        UserSession session = new UserSession(this);
        boolean isProfessor = "professor".equals(session.getRole());
        Professor professor = (Professor) getIntent().getSerializableExtra("CHOSEN_PROFESSOR");
        Log.d("Professor details", String.valueOf(professor.getRating()));
        if (professor != null) {
            populateUiElements(professor);
            fetchProfessorRating(professor.getId()); // ← fetches fresh rating from DB
            fetchProfessorReviews(professor.getId());
            fetchProfessorCourses(professor.getId());
            if (!isProfessor) {
                checkExistingReview(professor);
            }
        } else {
            Toast.makeText(this, "Error: Could not display professor data.", Toast.LENGTH_SHORT).show();
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnWriteReview = findViewById(R.id.btnWriteReview);
        if (isProfessor) {
            btnWriteReview.setVisibility(View.GONE);
        } else {
            btnWriteReview.setOnClickListener(v -> {
                checkExistingReview(professor);
            });
        }

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

    private void populateUiElements(Professor professor) {
        profName.setText(professor.getFullName());
        profDept.setText(professor.getTitle() + " • Informatics Department");
        largeRating.setText(professor.getRating() == 0.0 ? "N/A" : String.format("%.1f", professor.getRating()));
        reviewCount.setText(String.format("(%d reviews)", reviewList.size()));
    }
    @Override
    public void onCourseChipClick(Course course) {
        Intent intent = new Intent(ProfessorDetailsActivity.this, CourseDetailsActivity.class);
        intent.putExtra("CHOSEN_COURSE", course);
        startActivity(intent);
    }

    private void fetchProfessorReviews(String professorId) {
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
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    Log.e("LectureLensDebug", "Error Body: " + errorReader.readLine());
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching reviews", e);
            }
        }).start();
    }

    private void checkExistingReview(Professor professor) {
        String username = new UserSession(this).getUsername();

        new Thread(() -> {
            try {
                String endpoint = "http://10.0.2.2:8081/api/professor-reviews/check"
                        + "?professorId=" + professor.getId()
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
                            Intent intent = new Intent(this, ProfessorReviewActivity.class);
                            intent.putExtra("CHOSEN_PROFESSOR", professor);
                            intent.putExtra("isEditMode", true);
                            intent.putExtra("rating", (float) existing.optDouble("rating", 4.0));
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
                            Intent intent = new Intent(this, ProfessorReviewActivity.class);
                            intent.putExtra("CHOSEN_PROFESSOR", professor);
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

    private void fetchProfessorRating(String professorId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/professors/" + professorId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    double freshRating = json.optDouble("rating", 0.0);

                    new Handler(Looper.getMainLooper()).post(() ->
                            largeRating.setText(freshRating == 0.0 ? "N/A" : String.format("%.1f", freshRating))
                    );
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error fetching professor rating", e);
            }
        }).start();
    }
    private void showToastOnUi(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }


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


