package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
            String username = ((EditText) findViewById(R.id.usernameEditText)).getText().toString().trim();
            String email    = ((EditText) findViewById(R.id.emailEditText)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString().trim();
            boolean isStudent = ((SwitchCompat) findViewById(R.id.studentToggle)).isChecked();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = isStudent ? "student" : "professor";
            registerUser(username, email, password, role);
        });
    }

    private void registerUser(String username, String email, String password, String role) {
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
                json.put("username", username);
                json.put("email", email);
                json.put("passwordHash", password);
                json.put("role", role);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();

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