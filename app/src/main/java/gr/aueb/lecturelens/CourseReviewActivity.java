package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.Review;
import gr.aueb.lecturelens.model.UserSession;

public class CourseReviewActivity extends AppCompatActivity {

    private TextView[] diffButtons;
    private boolean isEditMode;
    private int selectedDifficulty = 3; // Keep track of selection state globally
    private Slider hoursSlider;
    private EditText reviewInput;
    private Course currentCourse;
    private String currentUsername;
    private SwitchCompat isAnonymous;

    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_review);

        UserSession session = new UserSession(this);
        currentUsername = session.getUsername();
        currentCourse = (Course) getIntent().getSerializableExtra("CHOSEN_COURSE");


        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        // Bind layout views
        ratingBar = findViewById(R.id.ratingBar);
        hoursSlider = findViewById(R.id.hoursSlider);
        reviewInput = findViewById(R.id.reviewEditText);
        isAnonymous = findViewById(R.id.anonSwitch);
        View btnDelete = findViewById(R.id.btnDelete);
        TextView btnSubmitReview = findViewById(R.id.btnSubmitReview);

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        hoursSlider.setLabelFormatter(value -> (int) value + " hours");

        // UI Setup depending on create vs edit mode context
        btnSubmitReview.setText(isEditMode ? getString(R.string.save_changes) : getString(R.string.submit_review));

        int initialDifficulty = 3;
        if (isEditMode) {
            initialDifficulty = getIntent().getIntExtra("difficulty", 3);
            hoursSlider.setValue(getIntent().getFloatExtra("hours", 5.0f));
            ratingBar.setRating(getIntent().getFloatExtra("rating", 4.0f));
            String reviewText = getIntent().getStringExtra("reviewText");
            btnDelete.setVisibility(View.VISIBLE);
            if (reviewText != null && reviewInput != null) reviewInput.setText(reviewText);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        selectedDifficulty = initialDifficulty;
        setupDifficultySelection(initialDifficulty);

        btnDelete.setOnClickListener(v -> {
            String reviewId = getIntent().getStringExtra("reviewId");
            if (reviewId == null || reviewId.isEmpty()) return;

            new Thread(() -> {
                try {
                    URL url = new URL("http://10.0.2.2:8081/api/reviews/" + reviewId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");
                    conn.setConnectTimeout(5000);

                    int responseCode = conn.getResponseCode();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                            /*userReviewsList.remove(position);
                            reviewsAdapter.notifyItemRemoved(position);*/
                            Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
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
        });

        btnSubmitReview.setOnClickListener(v -> executeReviewSubmission());
    }

    private void setupDifficultySelection(int initialDifficulty) {
        diffButtons = new TextView[]{
                findViewById(R.id.diff1),
                findViewById(R.id.diff2),
                findViewById(R.id.diff3),
                findViewById(R.id.diff4),
                findViewById(R.id.diff5)
        };

        for (int i = 0; i < diffButtons.length; i++) {
            final int difficulty = i + 1;
            diffButtons[i].setOnClickListener(v -> selectDifficulty(difficulty));
        }

        selectDifficulty(initialDifficulty);
    }

    private void selectDifficulty(int difficulty) {
        selectedDifficulty = difficulty; // Update tracking coordinate
        for (int i = 0; i < diffButtons.length; i++) {
            if (i + 1 == difficulty) {
                diffButtons[i].setBackgroundResource(R.drawable.circle_pink);
                diffButtons[i].setTextColor(ContextCompat.getColor(this, android.R.color.white));
            } else {
                diffButtons[i].setBackgroundResource(R.drawable.difficulty_circle_selector);
                diffButtons[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
        }
    }

    private void executeReviewSubmission() {
        String reviewText = reviewInput != null ? reviewInput.getText().toString().trim() : "";
        float studyHours = hoursSlider.getValue();
        String courseId = (currentCourse != null) ? currentCourse.getId() : "";
        boolean anonymousChecked = isAnonymous != null && isAnonymous.isChecked();
        if (courseId.isEmpty()) {
            Toast.makeText(this, "Error: Missing target course ID reference", Toast.LENGTH_SHORT).show();
            return;
        }
        String reviewId = getIntent().getStringExtra("reviewId");
        if (isEditMode && (reviewId == null || reviewId.isEmpty())) {
            Toast.makeText(this, "Error: Missing review ID for update", Toast.LENGTH_SHORT).show();
            return;
        }

        String endpoint = isEditMode
                ? "http://10.0.2.2:8081/api/reviews/" + reviewId + "/update"
                : "http://10.0.2.2:8081/api/reviews";
        // Fire off network thread
        new Thread(() -> {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);

                // Build Request JSON payload body
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("courseId", courseId);
                jsonParam.put("username", currentUsername != null ? currentUsername : "Anonymous");
                jsonParam.put("courseTitle", currentCourse.getTitle());
                jsonParam.put("rating", ratingBar.getRating());
                jsonParam.put("difficulty", selectedDifficulty);
                jsonParam.put("studyHours", Math.floor(studyHours));
                jsonParam.put("reviewText", reviewText);
                jsonParam.put("isAnonymous", anonymousChecked);

                // Write string output data stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Navigate to success state on UI Thread loop
                    new Handler(Looper.getMainLooper()).post(() -> {
                        startActivity(new Intent(CourseReviewActivity.this, SubmissionSuccessActivity.class));
                        finish();
                    });
                } else {
                    Log.e("LectureLensDebug", "Review submission failed with code: " + responseCode);
                    showToastOnUi("Server validation failed. Try again.");
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Network connection execution exception thrown", e);
                showToastOnUi("Network error writing review payload.");
            }
        }).start();
    }

    private void showToastOnUi(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(CourseReviewActivity.this, msg, Toast.LENGTH_SHORT).show()
        );
    }


}