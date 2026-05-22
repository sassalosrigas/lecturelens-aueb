package gr.aueb.lecturelens;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import gr.aueb.lecturelens.model.UserSession;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE_CHANGE_PASSWORD = 1001;
    private TextView infoValue1, infoValue2;
    private View alertBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the custom profile layout template
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        alertBox = view.findViewById(R.id.alertBox);

        // 2. Read user details dynamically out of the active user session cache layers
        UserSession session = new UserSession(requireContext());
        String fullName = session.getFullName();
        String username = session.getUsername();
        String email = session.getEmail();
        String role = session.getRole();
        String date = session.getCreationDate();

        // 3. Connect individual text views and data cards
        infoValue1 = view.findViewById(R.id.infoValue1);
        TextView infoLabel1 = view.findViewById(R.id.infoLabel1);
        infoValue2 = view.findViewById(R.id.infoValue2);
        TextView infoLabel2 = view.findViewById(R.id.infoLabel2);
        TextView primaryActionLabel = view.findViewById(R.id.primaryActionLabel);
        TextView profileName = view.findViewById(R.id.profileName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);

        profileName.setText(fullName);
        profileEmail.setText(email);

        boolean isProfessor = "professor".equalsIgnoreCase(role);

        if (isProfessor) {
            if (date != null && !date.isEmpty()) {
                infoValue1.setText(calculateActiveDuration(date));
            } else {
                infoValue1.setText("0 mos");
            }
            infoLabel1.setText(getString(R.string.courses_count));
            infoValue2.setText("3.5/5");
            infoLabel2.setText(getString(R.string.rating_label));
            primaryActionLabel.setText(getString(R.string.see_my_reviews));
        } else {
            if (date != null && !date.isEmpty()) {
                infoValue1.setText(calculateActiveDuration(date));
            } else {
                infoValue1.setText("0 mos");
            }
            infoLabel1.setText(getString(R.string.years_member));
            infoValue2.setText("-");
            infoLabel2.setText(getString(R.string.reviews_count));
            primaryActionLabel.setText(getString(R.string.manage_my_reviews));

            if (username != null && !username.isEmpty()) {
                fetchAndCalculateUserReviewCount(username);
            }
        }

        // 5. Setup Action Click listeners targeting separate workflow sub-activities
        View logoutButton = view.findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        View changePasswordAction = view.findViewById(R.id.changePasswordAction);
        if (changePasswordAction != null) {
            changePasswordAction.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD);
            });
        }

        View manageReviewsAction = view.findViewById(R.id.primaryAction);
        if (manageReviewsAction != null) {
            manageReviewsAction.setOnClickListener(v -> {
                Intent intent;
                if (isProfessor) {
                    intent = new Intent(getActivity(), ProfessorSeeReviewsActivity.class);
                } else {
                    intent = new Intent(getActivity(), ManageReviewsActivity.class);
                }
                startActivity(intent);
            });
        }

        return view;
    }

    private String calculateActiveDuration(String isoDateString) {
        try {
            java.time.Instant creationInstant = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                creationInstant = Instant.parse(isoDateString);
            }
            java.time.LocalDate creationDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                creationDate = creationInstant.atZone(ZoneId.systemDefault()).toLocalDate();
            }

            java.time.LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
            }

            java.time.Period period = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                period = Period.between(creationDate, today);
            }
            int years = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                years = period.getYears();
            }
            int months = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                months = period.getMonths();
            }

            if (years > 0) {
                return years + " yr, " + months + " mo";
            } else {
                return months + " mo";
            }
        } catch (Exception e) {
            Log.e("LectureLensDebug", "Error parsing account creation date string", e);
            return "New User";
        }
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

                    JSONArray jsonArray = new JSONArray(response.toString());
                    final int count = jsonArray.length();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Added isAdded() check to protect UI thread context from dead reference crashes
                        if (isAdded() && infoValue2 != null) {
                            infoValue2.setText(String.valueOf(count));
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("LectureLensDebug", "Error calculating user metrics profile stats count", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && infoValue2 != null) {
                        infoValue2.setText("0");
                    }
                });
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD && resultCode == Activity.RESULT_OK) {
            if (alertBox != null) {
                alertBox.setVisibility(View.VISIBLE);
                alertBox.postDelayed(() -> {
                    if (isAdded() && alertBox != null) {
                        alertBox.setVisibility(View.GONE);
                    }
                }, 3000);
            }
        }
    }
}