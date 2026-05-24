package gr.aueb.lecturelens;

import android.content.Context;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.Professor;
import gr.aueb.lecturelens.model.UserSession;

public class ProfessorReviewActivity extends AppCompatActivity {

    private EditText reviewInput;
    private RatingBar ratingBar;
    private SwitchCompat isAnonymous;
    private boolean isEditMode;
    private Professor currentProfessor;
    private String currentUsername;
    private TextView profName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_review);

        UserSession session = new UserSession(this);
        if ("professor".equals(session.getRole())) {
            Toast.makeText(this, "Professors cannot submit reviews.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUsername = session.getUsername();
        currentProfessor = (Professor) getIntent().getSerializableExtra("CHOSEN_PROFESSOR");

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        profName = findViewById(R.id.profNameBox);
        ratingBar = findViewById(R.id.ratingBar);
        reviewInput = findViewById(R.id.reviewEditText);
        isAnonymous = findViewById(R.id.anonSwitch);
        TextView btnCancel = findViewById(R.id.btnCancel);
        View btnDelete = findViewById(R.id.btnDelete);
        TextView btnSubmitReview = findViewById(R.id.btnSubmitReview);
        profName.setText(currentProfessor.getFullName());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        btnSubmitReview.setText(isEditMode ? getString(R.string.save_changes) : getString(R.string.submit_review));

        if (isEditMode) {
            ratingBar.setRating(getIntent().getFloatExtra("rating", 4.0f));
            String reviewText = getIntent().getStringExtra("reviewText");
            btnDelete.setVisibility(View.VISIBLE);
            if (reviewText != null && reviewInput != null) reviewInput.setText(reviewText);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnDelete.setOnClickListener(v -> {
            String reviewId = getIntent().getStringExtra("reviewId");
            if (reviewId == null || reviewId.isEmpty()) return;

            new Thread(() -> {
                try {
                    URL url = new URL("http://10.0.2.2:8081/api/professor-reviews/" + reviewId);
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

    private void executeReviewSubmission() {
        String reviewText = reviewInput != null ? reviewInput.getText().toString().trim() : "";
        String professorId = (currentProfessor != null) ? currentProfessor.getId() : "";
        boolean anonymousChecked = isAnonymous != null && isAnonymous.isChecked();
        if (professorId.isEmpty()) {
            Toast.makeText(this, "Error: Missing target professor ID reference", Toast.LENGTH_SHORT).show();
            return;
        }
        String reviewId = getIntent().getStringExtra("reviewId");
        if (isEditMode && (reviewId == null || reviewId.isEmpty())) {
            Toast.makeText(this, "Error: Missing review ID for update", Toast.LENGTH_SHORT).show();
            return;
        }

        String endpoint = isEditMode
                ? "http://10.0.2.2:8081/api/professor-reviews/" + reviewId + "/update"
                : "http://10.0.2.2:8081/api/professor-reviews";
        new Thread(() -> {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("professorId", professorId);
                jsonParam.put("username", currentUsername != null ? currentUsername : "Anonymous");
                jsonParam.put("professorName", currentProfessor.getFullName());
                jsonParam.put("rating", ratingBar.getRating());
                jsonParam.put("reviewText", reviewText);
                jsonParam.put("isAnonymous", anonymousChecked);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        triggerHapticFeedback();
                        Intent intent = new Intent(ProfessorReviewActivity.this, SubmissionSuccessActivity.class);
                        intent.putExtra("CHOSEN_PROFESSOR", currentProfessor);
                        intent.putExtra("isProfessorReview", true);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.e("LectureLensDebug", "Review submission failed with code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Network connection execution exception thrown", e);
            }
        }).start();


    }

    private void triggerHapticFeedback() {
        android.util.Log.d("LectureLensDebug", "HAPTIC TRIGGERED: Vibrate command sent to system!");
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                // Modern API 31+ approach using VibratorManager
                android.os.VibratorManager vibratorManager = (android.os.VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    // A quick doublet-tap pattern (Predefined Success effect)
                    vibratorManager.getDefaultVibrator().vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK));
                }
            } else {
                // Legacy fallback approach for older API levels
                android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        // API 26 to 30: Vibrate for 150ms at standard amplitude strength
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // Ancient API fallback
                        vibrator.vibrate(150);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("LectureLensDebug", "Failed to perform haptic feedback rumble", e);
        }
    }

}