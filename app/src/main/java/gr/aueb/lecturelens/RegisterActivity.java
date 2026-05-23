package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private String cachedUsername = "";
    private String cachedEmail = "";

    private String cachedPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView signInTextView = findViewById(R.id.signInTextView);
        signInTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        MaterialButton registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            String fullName = ((EditText) findViewById(R.id.fullNameEditText)).getText().toString().trim();
            String username = ((EditText) findViewById(R.id.usernameEditText)).getText().toString().trim();
            String email    = ((EditText) findViewById(R.id.emailEditText)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString().trim();
            boolean isStudent = ((SwitchCompat) findViewById(R.id.studentToggle)).isChecked();
            cachedUsername = ((EditText) findViewById(R.id.usernameEditText)).getText().toString().trim();
            cachedEmail    = ((EditText) findViewById(R.id.emailEditText)).getText().toString().trim();
            cachedPassword = ((EditText) findViewById(R.id.passwordEditText)).getText().toString().trim();

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            if (cachedUsername.isEmpty() || cachedEmail.isEmpty() || cachedPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = isStudent ? "student" : "professor";
            registerButton.setEnabled(false);
            registerUser(fullName, username, email, password, role);
            // Automatically open the CameraX scanner activity instead of submitting immediately
            Intent intent = new Intent(RegisterActivity.this, OcrScanActivity.class);
            startActivityForResult(intent, REQUEST_CODE_OCR);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OCR && resultCode == RESULT_OK && data != null) {
            String scannedAM = data.getStringExtra("EXTRACTED_AM");
            if (scannedAM != null && !scannedAM.isEmpty()) {
                // Camera verification succeeded -> Proceed to server registration with the captured A.M.
                registerUser(cachedUsername, cachedEmail, cachedPassword, scannedAM);
            }
        } else {
            Toast.makeText(this, "Registration cancelled. ID verification required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser(String username, String email, String password, String studentAM) {
    private void registerUser(String fullName, String username, String email, String password, String role) {
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
                json.put("studentAM", studentAM);
                json.put("role", "student");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()
                        )
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                br.close();
                conn.disconnect();

                if (responseCode == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
                } else if (responseCode == 409) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Email or username already exists", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Could not connect to server", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}