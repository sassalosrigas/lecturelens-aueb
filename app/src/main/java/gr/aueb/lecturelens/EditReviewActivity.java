package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.slider.Slider;

public class EditReviewActivity extends AppCompatActivity {

    private TextView[] diffButtons;
    private int selectedDifficulty = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_review);

        TextView btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> finish());

        View btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnSubmitReview.setOnClickListener(v -> {
            // For demo purposes, we just go back to ManageReviewsActivity or show success
            // The Figma design says "save changes"
            finish();
        });

        Slider hoursSlider = findViewById(R.id.hoursSlider);
        hoursSlider.setLabelFormatter(value -> (int) value + " hours");

        setupDifficultySelection();
    }

    private void setupDifficultySelection() {
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
        
        // Initialize with 3
        selectDifficulty(3);
    }

    private void selectDifficulty(int difficulty) {
        selectedDifficulty = difficulty;
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
