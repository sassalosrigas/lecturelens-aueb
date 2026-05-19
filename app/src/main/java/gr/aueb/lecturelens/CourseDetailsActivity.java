package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CourseDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        boolean isProfessor = getIntent().getBooleanExtra("isProfessor", false);

        // Back button
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Write Review — only visible for students
        View btnWriteReview = findViewById(R.id.btnWriteReview);
        if (isProfessor) {
            btnWriteReview.setVisibility(View.GONE);
        } else {
            btnWriteReview.setOnClickListener(v -> {
                startActivity(new Intent(CourseDetailsActivity.this, CourseReviewActivity.class));
            });
        }

        // Nav bar
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
            intent.putExtra("isProfessor", isProfessor); // pass the flag through
            startActivity(intent);
            finish();
        });
    }
}