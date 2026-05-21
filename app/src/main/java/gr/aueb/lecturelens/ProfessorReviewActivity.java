package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import org.json.JSONObject;

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
    private boolean isEditMode = false;
    private Professor currentProfessor;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_review);

        UserSession session = new UserSession(this);
        currentUsername = session.getUsername();
        currentProfessor = (Professor) getIntent().getSerializableExtra("CHOSEN_PROFESSOR");
        ratingBar = findViewById(R.id.ratingBar);
        reviewInput = findViewById(R.id.reviewEditText);
        isAnonymous = findViewById(R.id.anonSwitch);
        TextView btnCancel = findViewById(R.id.btnCancel);
        TextView btnSubmitReview = findViewById(R.id.btnSubmitReview);
        View btnDelete = findViewById(R.id.btnDelete);
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
                        startActivity(new Intent(ProfessorReviewActivity.this, SubmissionSuccessActivity.class));
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
}
