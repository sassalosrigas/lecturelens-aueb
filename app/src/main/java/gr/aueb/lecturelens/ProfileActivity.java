package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

    @Override
    protected void onResume() {
        super.onResume();
        View manageReviewsAction = findViewById(R.id.manageReviewsAction);
        if (manageReviewsAction != null) {
            manageReviewsAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, ManageReviewsActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
