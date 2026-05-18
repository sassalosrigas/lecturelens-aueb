package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfessorReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_review);

        TextView btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> finish());

        View btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnSubmitReview.setOnClickListener(v -> {
            Intent intent = new Intent(ProfessorReviewActivity.this, SubmissionSuccessActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
