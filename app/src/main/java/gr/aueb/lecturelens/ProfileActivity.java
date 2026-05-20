package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import gr.aueb.lecturelens.model.UserSession;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHANGE_PASSWORD = 1001;
    private TextView infoValue2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ImageView navHome = findViewById(R.id.navHome);
        UserSession session = new UserSession(this);
        String username = session.getUsername();
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
        infoValue2 = findViewById(R.id.infoValue2);
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
            profileName.setText(username);
            profileEmail.setText(getString(R.string.student_email_demo));
            infoValue1.setText("3");
            infoLabel1.setText(getString(R.string.years_member));
            infoValue2.setText("-");
            infoLabel2.setText(getString(R.string.reviews_count));
            primaryActionLabel.setText(getString(R.string.manage_my_reviews));

            if (username != null && !username.isEmpty()) {
                fetchAndCalculateUserReviewCount(username);
            }
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

    private void fetchAndCalculateUserReviewCount(String username) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/reviews/user/" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line.trim());
                    }
                    in.close();

                    // Parse payload into a temporary array array block to derive structural size data
                    JSONArray jsonArray = new JSONArray(response.toString());
                    final int count = jsonArray.length();

                    // Dispatch result seamlessly onto main thread run loop loop safely
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (infoValue2 != null) {
                            infoValue2.setText(String.valueOf(count));
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating user metrics profile stats count", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (infoValue2 != null) {
                        infoValue2.setText("0"); // Fallback safety state if offline
                    }
                });
            }
        }).start();
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
