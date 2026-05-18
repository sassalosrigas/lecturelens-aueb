package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- AUTOMATIC TEST CONNECTION ON STARTUP ---
        sendDummyUserToBackend();
        // ---------------------------------------------

        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
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

    /**
     * Spawns a background thread to send an HTTP POST request to the Spring Boot server
     */
    private void sendDummyUserToBackend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // NOTE: '10.0.2.2' is a special loopback IP address that tells the
                    // Android Emulator to look at your computer's 'localhost' where Spring Boot runs.
                    // If you are using a physical device, use your computer's local IP address (e.g., 192.168.1.x).
                    URL url = new URL("http://10.0.2.2:8080/api/users/test");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    // Get the server response code
                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Success! Check your Logcat console in Android Studio
                        android.util.Log.d("LectureLens_DB_Test", "SUCCESS: Dummy user posted through backend to MongoDB!");
                    } else {
                        android.util.Log.e("LectureLens_DB_Test", "FAILED: Server returned response code: " + responseCode);
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    android.util.Log.e("LectureLens_DB_Test", "CRITICAL ERROR: Could not connect to local server.", e);
                }
            }
        }).start();
    }
}