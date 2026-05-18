package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.slider.Slider;

public class CourseReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_review);

        TextView btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> finish());

        View btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnSubmitReview.setOnClickListener(v -> {
            Intent intent = new Intent(CourseReviewActivity.this, SubmissionSuccessActivity.class);
            startActivity(intent);
            finish();
        });

        Slider hoursSlider = findViewById(R.id.hoursSlider);
        hoursSlider.setLabelFormatter(value -> (int) value + " hours");

        setupDifficultySelection();
    }

    private void setupDifficultySelection() {
        TextView[] diffButtons = {
                findViewById(R.id.diff1),
                findViewById(R.id.diff2),
                findViewById(R.id.diff3),
                findViewById(R.id.diff4),
                findViewById(R.id.diff5)
        };

        for (int i = 0; i < diffButtons.length; i++) {
            final int index = i;
            diffButtons[i].setOnClickListener(v -> {
                for (int j = 0; j < diffButtons.length; j++) {
                    if (j == index) {
                        diffButtons[j].setSelected(true);
                        diffButtons[j].setTextColor(ContextCompat.getColor(this, android.R.color.white));
                    } else {
                        diffButtons[j].setSelected(false);
                        diffButtons[j].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    }
                }
            });
        }
        
        // Initial state
        diffButtons[2].setSelected(true);
        diffButtons[2].setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }
}
