package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import gr.aueb.lecturelens.R;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OCR = 2002;

    private MaterialButton registerButton;
    private SwitchCompat studentToggle;

    // Security Caches: Temporarily hold inputs while the scanner activity handles the card
    private String cachedUsername = "";
    private String cachedEmail = "";
    private String cachedPassword = "";
    private String cachedRole = "student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView signInTextView = findViewById(R.id.signInTextView);
        signInTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        studentToggle = findViewById(R.id.studentToggle);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            cachedUsername = ((EditText) findViewById(R.id.usernameEditText)).getText().toString().trim();
            cachedPassword = ((EditText) findViewById(R.id.passwordEditText)).getText().toString().trim();
            boolean isStudent = studentToggle.isChecked();
            cachedRole = isStudent ? "student" : "professor";

            // If it's a Professor, they still fill out their email explicitly via input forms
            if (!isStudent) {
                cachedEmail = ((EditText) findViewById(R.id.emailEditText)).getText().toString().trim();
                String fullName = ((EditText) findViewById(R.id.fullNameEditText)).getText().toString().trim();

                if (fullName.isEmpty() || cachedUsername.isEmpty() || cachedEmail.isEmpty() || cachedPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cachedEmail).matches()) {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerButton.setEnabled(false);
                sendRegistrationToServer(fullName, cachedUsername, cachedEmail, cachedPassword, cachedRole, "");
            } else {
                // STUDENT FLOW: Email and Full Name are derived automatically from the ID card.
                // We only require a username and password to start the scan.
                if (cachedUsername.isEmpty() || cachedPassword.isEmpty()) {
                    Toast.makeText(this, "Please enter a Username and Password first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerButton.setEnabled(false);

                // Fire up the high-res CameraX picture engine
                Intent intent = new Intent(RegisterActivity.this, OcrScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OCR);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Always re-enable the register button if the user returns or cancels
        if (registerButton != null) {
            registerButton.setEnabled(true);
        }

        if (requestCode == REQUEST_CODE_OCR && resultCode == RESULT_OK && data != null) {
            String scannedName = data.getStringExtra("EXTRACTED_NAME"); // Holds English Name
            String scannedAM   = data.getStringExtra("EXTRACTED_AM");   // Holds clean AM digits

            if (scannedName == null || scannedAM == null || scannedAM.isEmpty()) {
                Toast.makeText(this, "Failed to capture card data cleanly. Please re-scan.", Toast.LENGTH_LONG).show();
                return;
            }

            // ========================================================
            // CRITICAL VERIFICATION: CROSS-CHECK THE GENERATED EMAIL
            // ========================================================
            // AUEB student accounts use the following structural pattern: p[AM]@aueb.gr
            String computedStudentEmail = "p" + scannedAM + "@aueb.gr";

            // If you want to check an optional email field value entered by the user:
            EditText emailInput = findViewById(R.id.emailEditText);
            if (emailInput != null) {
                String typedEmail = emailInput.getText().toString().trim();
                if (!typedEmail.isEmpty() && !typedEmail.equalsIgnoreCase(computedStudentEmail)) {
                    Toast.makeText(this, "Cross-check failed! Your email does not match this Academic ID code.", Toast.LENGTH_LONG).show();
                    return; // Prevent registration fraud
                }
            }

            // Autofill validation succeeded -> Set the derived values globally
            cachedEmail = computedStudentEmail;

            // Send the completed account package down to your Spring Boot API
            sendRegistrationToServer(scannedName, cachedUsername, cachedEmail, cachedPassword, cachedRole, scannedAM);

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Registration cancelled. ID verification required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRegistrationToServer(String fullName, String username, String email, String password, String role, String studentAM) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                JSONObject json = new JSONObject();
                json.put("fullName", fullName);
                json.put("username", username);
                json.put("email", email);
                json.put("passwordHash", password);
                json.put("role", role);
                if (role.equals("student")) {
                    json.put("studentAM", studentAM);
                }

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                responseCode == 200 || responseCode == 201 ? conn.getInputStream() : conn.getErrorStream()
                        )
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                br.close();
                conn.disconnect();

                if (responseCode == 200 || responseCode == 201) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Account verified & created!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
                } else if (responseCode == 409) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Email, username, or AM already exists.", Toast.LENGTH_SHORT).show();
                        registerButton.setEnabled(true);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registration failed on server. Try again.", Toast.LENGTH_SHORT).show();
                        registerButton.setEnabled(true);
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Could not connect to backend server.", Toast.LENGTH_SHORT).show();
                    registerButton.setEnabled(true);
                });
            }
        }).start();
    }
}