package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.Professor;

public class SubmissionSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_success);

        boolean isProfessorReview = getIntent().getBooleanExtra("isProfessorReview", false);
        Professor professor = (Professor) getIntent().getSerializableExtra("CHOSEN_PROFESSOR");
        Course course = (Course) getIntent().getSerializableExtra("CHOSEN_COURSE");

        View btnReturnHome = findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            Intent intent;
            if (isProfessorReview && professor != null) {
                intent = new Intent(SubmissionSuccessActivity.this, ProfessorDetailsActivity.class);
                intent.putExtra("CHOSEN_PROFESSOR", professor);
                intent.putExtra("isProfessor", false);
            } else if (!isProfessorReview && course != null) {
                intent = new Intent(SubmissionSuccessActivity.this, CourseDetailsActivity.class);
                intent.putExtra("CHOSEN_COURSE", course);
                intent.putExtra("isProfessor", false);
            } else {
                intent = new Intent(SubmissionSuccessActivity.this, MainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        View btnViewReview = findViewById(R.id.btnViewReview);
        btnViewReview.setOnClickListener(v -> {
            Intent intent = new Intent(SubmissionSuccessActivity.this, ManageReviewsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
