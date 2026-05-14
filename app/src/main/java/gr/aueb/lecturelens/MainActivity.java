package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
