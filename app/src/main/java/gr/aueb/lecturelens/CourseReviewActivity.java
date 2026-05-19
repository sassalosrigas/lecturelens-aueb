package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.slider.Slider;

public class CourseReviewActivity extends AppCompatActivity {

    private TextView[] diffButtons;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_review);

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        // Change submit button label based on mode
        TextView btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnSubmitReview.setText(isEditMode
                ? getString(R.string.save_changes)
                : getString(R.string.submit_review));

        btnSubmitReview.setOnClickListener(v -> {
            if (isEditMode) {
                // Save changes and go back
                finish();
            } else {
                // New review — go to success screen
                startActivity(new Intent(CourseReviewActivity.this, SubmissionSuccessActivity.class));
                finish();
            }
        });

        Slider hoursSlider = findViewById(R.id.hoursSlider);
        hoursSlider.setLabelFormatter(value -> (int) value + " hours");

        int initialDifficulty = 3;
        if (isEditMode) {
            initialDifficulty = getIntent().getIntExtra("difficulty", 3);
            hoursSlider.setValue(getIntent().getFloatExtra("hours", 5.0f));

            String reviewText = getIntent().getStringExtra("reviewText");
            TextView reviewInput = findViewById(R.id.reviewEditText);
            if (reviewText != null && reviewInput != null) reviewInput.setText(reviewText);
        }

        setupDifficultySelection(initialDifficulty);
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
}