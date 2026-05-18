package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfessorProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHANGE_PASSWORD = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_profile);

        ImageView navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ImageView navSearch = findViewById(R.id.navSearch);
        navSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorProfileActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        View logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        View seeReviewsAction = findViewById(R.id.seeReviewsAction);
        seeReviewsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorProfileActivity.this, ProfessorSeeReviewsActivity.class);
                startActivity(intent);
            }
        });

        View changePasswordAction = findViewById(R.id.changePasswordAction);
        changePasswordAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorProfileActivity.this, ProfessorChangePasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD && resultCode == RESULT_OK) {
            View alertBox = findViewById(R.id.alertBox);
            if (alertBox != null) {
                alertBox.setVisibility(View.VISIBLE);
                // Hide after 3 seconds
                alertBox.postDelayed(() -> alertBox.setVisibility(View.GONE), 3000);
            }
        }
    }
}
