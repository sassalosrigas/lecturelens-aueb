package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import gr.aueb.lecturelens.model.UserSession;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize your helper session class
        UserSession session = new UserSession(this);

        // 2. Read the username straight out of storage
        String username = session.getUsername();

        // Debug printing to verify it worked!
        Log.d("LectureLensDebug", "MainActivity opened. Saved username is: " + username);

        // 3. Use it in your layout
        // 3. Log the result to Logcat
        if (username != null) {
            Log.d("LectureLensDebug", " Successfully retrieved username: " + username);
        } else {
            Log.w("LectureLensDebug", "⚠️ No username found in this session.");
        }
        View searchBar = findViewById(R.id.searchEditText);
        if (searchBar != null) {
            searchBar.setFocusable(false);
            searchBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                }
            });
        }

        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //already on main
                }
            });
        }

        View navSearch = findViewById(R.id.navSearch);
        if (navSearch != null) {
            navSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                }
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                }
            });
        }

        setupCourseClickListeners();
    }

    private void setupCourseClickListeners() {
        ViewGroup courseContainer = findViewById(android.R.id.content);
        findAndSetCourseClickListeners(courseContainer);
    }

    private void findAndSetCourseClickListeners(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getId() == R.id.courseDetailsCard) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, CourseDetailsActivity.class));
                    }
                });
            } else if (child instanceof ViewGroup) {
                findAndSetCourseClickListeners((ViewGroup) child);
            }
        }
    }
}
