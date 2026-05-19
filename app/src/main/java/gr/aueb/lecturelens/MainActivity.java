package gr.aueb.lecturelens;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.CourseAdapter;

public class MainActivity extends AppCompatActivity implements CourseAdapter.OnCourseClickListener {

    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private final List<Course> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView Layout Structure
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(courseList, this);
        coursesRecyclerView.setAdapter(courseAdapter);

        // Search Bar Redirection Event Mapping
        View searchBar = findViewById(R.id.searchEditText);
        if (searchBar != null) {
            searchBar.setFocusable(false);
            searchBar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        }

        // Navigation Bar Listeners Setup
        setupNavigationListeners();

        // Stream Database Collection Stream Items
        fetchCoursesFromBackend();
    }

    private void setupNavigationListeners() {
        View navSearch = findViewById(R.id.navSearch);
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        }
    }

    @Override
    public void onCourseClick(Course course) {
        // Enforce state transition by attaching clicked metadata context to tracking intent context
        Intent intent = new Intent(MainActivity.this, CourseDetailsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        intent.putExtra("COURSE_CODE", course.getCode());
        startActivity(intent);
    }

    private void fetchCoursesFromBackend() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/courses");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // Parse JSON payload schema directly matching MongoDB records mapping structures
                    JSONArray jsonArray = new JSONArray(response.toString());
                    courseList.clear();
                    /*
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        courseList.add(new Course(
                                obj.optString("id", obj.optString("_id")),
                                obj.optString("code", ""),
                                obj.optString("title", "Unknown Course"),
                                obj.optInt("semester", 0),
                                obj.optInt("ects", 0)
                        ));
                        Log.d("course debug", obj.toString());
                    }
                    */


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        // Parse out everything matching your new model fields
                        String id = obj.optString("id", obj.optString("_id", ""));
                        String code = obj.optString("code", "N/A");
                        String title = obj.optString("title", "Unknown Course");
                        int semester = obj.optInt("semester", 0);
                        int ects = obj.optInt("ects", 0);

                        // Fields matching your item_course_card requirements
                        String profName = obj.optString("professorName", "Staff");
                        double rating = obj.optDouble("rating", 0.0);
                        String difficulty = obj.optString("difficulty", "Medium");
                        String hours = obj.optString("studyHours", "4-6");
                        String description = obj.optString("description", "No description provided.");

                        courseList.add(new Course(id, code, title, semester, ects, profName, rating, difficulty, hours, description));
                    }

// Safely refresh your adapter on the UI thread
                    new Handler(Looper.getMainLooper()).post(() -> courseAdapter.notifyDataSetChanged());

// Global count validation check before reaching the adapter refresh
                    Log.d("CourseDebug", "================================================");
                    Log.d("CourseDebug", "TOTAL ITEMS ADDED TO COURSE LIST: " + courseList.size());
                    Log.d("CourseDebug", "================================================");

                }
                conn.disconnect();
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(MainActivity.this, "Network error reading database parameters.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}