package gr.aueb.lecturelens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import gr.aueb.lecturelens.model.UserSession;

public class HomeFragment extends Fragment implements CourseAdapter.OnCourseClickListener {

    private RecyclerView coursesRecyclerView;
    private TextView greeting;
    private CourseAdapter courseAdapter;
    private final List<Course> courseList = new ArrayList<>();

    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        UserSession session = new UserSession(requireContext());
        String fullName = session.getFullName();
        String username = session.getUsername();

        Log.d("LectureLensDebug", "HomeFragment view mounted. Saved name is: " + fullName);
        if (username != null) {
            Log.d("LectureLensDebug", "Successfully retrieved username: " + username);
        } else {
            Log.w("LectureLensDebug", "No username found in this session.");
        }

        greeting = view.findViewById(R.id.greetingTextView);
        if (fullName != null && !fullName.isEmpty()) {
            greeting.setText("Hello, " + fullName.trim().split("\\s+")[0] + "!");
        } else {
            greeting.setText("Hello!");
        }


        coursesRecyclerView = view.findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courseAdapter = new CourseAdapter(courseList, this);
        coursesRecyclerView.setAdapter(courseAdapter);

        // 3. Setup fake Search Bar click interaction to scroll the viewPager to the search tab automatically
        View searchBar = view.findViewById(R.id.searchEditText);
        if (searchBar != null) {
            searchBar.setFocusable(false);
            searchBar.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).jumpToSearchTab();
                }
            });
        }

        // 4. Run the network thread to retrieve your MongoDB information parameters
        fetchCoursesFromBackend();

        return view;
    }

    @Override
    public void onCourseClick(Course course) {
        // Merged Team Change: Route click actions to details screen with full object bundle matching their implementation
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        intent.putExtra("CHOSEN_COURSE", course);
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

                    JSONArray jsonArray = new JSONArray(response.toString());
                    courseList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        String id = obj.optString("id", obj.optString("_id", ""));
                        String code = obj.optString("code", "N/A");
                        String title = obj.optString("title", "Unknown Course");
                        int semester = obj.optInt("semester", 0);
                        int ects = obj.optInt("ects", 0);

                        String profName = obj.optString("professorName", "Staff");
                        double rating = obj.optDouble("rating", 0.0);

                        // CRITICAL MERGED TEAM FIX: Swapped out strings for doubles to match their new Course constructor fields perfectly!
                        double difficulty = obj.optDouble("difficulty", 0.0);
                        double hours = obj.optDouble("hours", 0.0);

                        String description = obj.optString("description", "No description provided.");

                        courseList.add(new Course(id, code, title, semester, ects, profName, rating, difficulty, hours, description));
                    }

                    // Dispatch data changes back onto the safe main UI render thread
                    new Handler(Looper.getMainLooper()).post(() -> courseAdapter.notifyDataSetChanged());

                    Log.d("CourseDebug", "================================================");
                    Log.d("CourseDebug", "TOTAL ITEMS ADDED TO FRAGMENT COURSE LIST: " + courseList.size());
                    Log.d("CourseDebug", "================================================");
                }
                conn.disconnect();
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Safety check protecting against loose context crashes if user backs out during call routine
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Network error reading database parameters.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}