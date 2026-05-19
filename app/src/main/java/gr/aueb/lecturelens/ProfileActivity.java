package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHANGE_PASSWORD = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ImageView navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ImageView navSearch = findViewById(R.id.navSearch);
        navSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        View logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        View changePasswordAction = findViewById(R.id.changePasswordAction);
        changePasswordAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD);
            }
        });

        boolean isProfessor = getIntent().getBooleanExtra("isProfessor", false);

        TextView infoValue1 = findViewById(R.id.infoValue1);
        TextView infoLabel1 = findViewById(R.id.infoLabel1);
        TextView infoValue2 = findViewById(R.id.infoValue2);
        TextView infoLabel2 = findViewById(R.id.infoLabel2);
        TextView primaryActionLabel = findViewById(R.id.primaryActionLabel);
        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);

        if (isProfessor) {
            profileName.setText(getString(R.string.professor_name));
            profileEmail.setText(getString(R.string.professor_email_demo));
            infoValue1.setText("3");
            infoLabel1.setText(getString(R.string.courses_count));
            infoValue2.setText("3.5/5");
            infoLabel2.setText(getString(R.string.rating_label));
            primaryActionLabel.setText(getString(R.string.see_my_reviews));
        } else {
            profileName.setText(getString(R.string.giorgos_papadopoulos));
            profileEmail.setText(getString(R.string.student_email_demo));
            infoValue1.setText("3");
            infoLabel1.setText(getString(R.string.years_member));
            infoValue2.setText("5");
            infoLabel2.setText(getString(R.string.reviews_count));
            primaryActionLabel.setText(getString(R.string.manage_my_reviews));
        }

        View manageReviewsAction = findViewById(R.id.primaryAction);
        manageReviewsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ManageReviewsActivity.class);
                startActivity(intent);
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
                // Hide after 3000ms
                alertBox.postDelayed(() -> alertBox.setVisibility(View.GONE), 3000);
            }
        }
    }
}
