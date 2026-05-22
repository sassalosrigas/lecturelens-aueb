package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }

            loginUser(email,password);
        });

        MaterialButton registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/users/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("passwordHash", password);

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
                    android.util.Log.d("LectureLensDebug", "Raw Server Response: " + response.toString());

                    JSONObject responseJson = new JSONObject(response.toString());
                    final String fullName = responseJson.optString("fullName", "Full Name");
                    final String username = responseJson.optString("username", "User");
                    final String currentEmail = responseJson.optString("email", "aaa@aueb.gr");
                    final String currentPassword = responseJson.optString("passwordHash", "password");
                    final String role = responseJson.optString("role", "role");
                    final String createdAt = responseJson.optString("createdAt", "2026-05-21T14:47:00Z");

                    gr.aueb.lecturelens.model.UserSession session = new gr.aueb.lecturelens.model.UserSession(LoginActivity.this);
                    session.createLoginSession(fullName, username, currentEmail, currentPassword, role, createdAt);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                } else if (responseCode == 401) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
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