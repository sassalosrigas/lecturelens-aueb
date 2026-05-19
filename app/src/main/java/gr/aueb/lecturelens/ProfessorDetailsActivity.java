package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfessorDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_details_st);

        boolean isProfessor = getIntent().getBooleanExtra("isProfessor", false);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Hide Write Review button for professors
        View btnWriteReview = findViewById(R.id.btnWriteReview);
        if (isProfessor) {
            btnWriteReview.setVisibility(View.GONE);
        } else {
            btnWriteReview.setOnClickListener(v ->
                    startActivity(new Intent(ProfessorDetailsActivity.this, ProfessorReviewActivity.class))
            );
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
}