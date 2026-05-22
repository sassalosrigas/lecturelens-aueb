package gr.aueb.lecturelens;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.slider.RangeSlider;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import gr.aueb.lecturelens.java.AppCache;
import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.CourseChipAdapter;
import gr.aueb.lecturelens.java.Professor;
import gr.aueb.lecturelens.java.ProfessorAdapter;

public class SearchActivity extends AppCompatActivity implements
        CourseChipAdapter.OnCourseChipClickListener, ProfessorAdapter.OnProfClickListener {

    private View recentSearchesLayout;
    private EditText searchEditText;
    private View searchBarContainer;

    private RecyclerView recommendedCoursesRecycler;
    private RecyclerView recommendedProfessorsRecycler;
    private CourseChipAdapter courseChipAdapter;
    private ProfessorAdapter professorAdapter;

    private final List<Course> randomCourseList = new ArrayList<>();
    private final List<Professor> randomProfessorList = new ArrayList<>();

    private float[] selectedDifficulty = {1f, 5f};
    private float[] selectedHours = {1f, 9f};
    private float selectedRating = 4f;

    private View recommendationsScrollView;
    private View searchResultsContainer;
    private View searchResultsCoursesLabel;
    private View searchResultsProfessorsLabel;
    private RecyclerView searchResultsCourses;
    private RecyclerView searchResultsProfessors;
    private CourseChipAdapter searchCourseAdapter;
    private ProfessorAdapter searchProfessorAdapter;
    private final List<Course> searchCourseResults = new ArrayList<>();
    private final List<Professor> searchProfessorResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recentSearchesLayout = findViewById(R.id.recentSearchesLayout);
        searchEditText = findViewById(R.id.searchEditText);
        searchBarContainer = findViewById(R.id.searchBarContainer);
        TextView cancelSearch = findViewById(R.id.cancelSearch);

        recommendedCoursesRecycler = findViewById(R.id.recommendedCoursesRecyclerView);
        recommendedProfessorsRecycler = findViewById(R.id.recommendedProfessorsRecyclerView);
        recommendationsScrollView = findViewById(R.id.recommendationsScrollView);
        recommendedCoursesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        courseChipAdapter = new CourseChipAdapter(randomCourseList, this);
        recommendedCoursesRecycler.setAdapter(courseChipAdapter);

        recommendedProfessorsRecycler.setLayoutManager(new LinearLayoutManager(this));
        professorAdapter = new ProfessorAdapter(randomProfessorList, this);
        recommendedProfessorsRecycler.setAdapter(professorAdapter);


        searchResultsContainer = findViewById(R.id.searchResultsContainer);
        searchResultsCoursesLabel = findViewById(R.id.searchResultsCoursesLabel);
        searchResultsProfessorsLabel = findViewById(R.id.searchResultsProfessorsLabel);

        searchResultsCourses = findViewById(R.id.searchResultsCourses);
        searchResultsCourses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        searchCourseAdapter = new CourseChipAdapter(searchCourseResults, this);
        searchResultsCourses.setAdapter(searchCourseAdapter);

        searchResultsProfessors = findViewById(R.id.searchResultsProfessors);
        searchResultsProfessors.setLayoutManager(new LinearLayoutManager(this));
        searchProfessorAdapter = new ProfessorAdapter(searchProfessorResults, this);
        searchResultsProfessors.setAdapter(searchProfessorAdapter);

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showRecentSearches();
        });

        searchEditText.setOnClickListener(v -> showRecentSearches());
        cancelSearch.setOnClickListener(v -> hideRecentSearches());

        Handler searchHandler = new Handler(Looper.getMainLooper());
        Runnable[] searchRunnable = {null};


        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable[0] != null) searchHandler.removeCallbacks(searchRunnable[0]);
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    hideSearchDropdown();
                    return;
                }
                searchRunnable[0] = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable[0], 350);
            }
        });

        setupNavigationListeners();

        LinearLayout filtersLayout = findViewById(R.id.filtersLayout);
        filtersLayout.setOnClickListener(v -> showFilterBottomSheet());

        fetchRecommendationsFromDatabase();
    }

    private void setupNavigationListeners() {
        ImageView navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, MainActivity.class));
            finish();
        });

        ImageView navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
            finish();
        });
    }

    @Override
    public void onCourseChipClick(Course course) {
        Intent intent = new Intent(SearchActivity.this, CourseDetailsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        intent.putExtra("CHOSEN_COURSE", course);
        startActivity(intent);
    }

    @Override
    public void onProfClick(Professor prof) {
        Intent intent = new Intent(SearchActivity.this, ProfessorDetailsActivity.class);
        intent.putExtra("PROFESSOR_ID", prof.getId());
        intent.putExtra("CHOSEN_PROFESSOR", prof);
        startActivity(intent);
    }

    private void fetchRecommendationsFromDatabase() {
        AppCache cache = AppCache.getInstance();

        if (cache.loaded) {
            randomCourseList.addAll(cache.cachedCourses);
            randomProfessorList.addAll(cache.cachedProfessors);
            courseChipAdapter.notifyDataSetChanged();
            professorAdapter.notifyDataSetChanged();
            return;
        }

        // Courses thread
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/courses/random");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) res.append(line);
                    in.close();

                    JSONArray array = new JSONArray(res.toString());
                    randomCourseList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        randomCourseList.add(new Course(
                                obj.optString("id", obj.optString("_id")),
                                obj.optString("code", ""),
                                obj.optString("title", ""),
                                obj.optInt("semester", 1),
                                obj.optInt("ects", 6),
                                obj.optString("professorName", "Staff"),
                                obj.optDouble("rating", 0.0),
                                obj.optDouble("difficulty", 0.0),
                                obj.optDouble("studyHours", 0.0),
                                obj.optString("description", "")
                        ));
                    }
                    cache.cachedCourses.clear();
                    cache.cachedCourses.addAll(randomCourseList);

                    new Handler(Looper.getMainLooper()).post(() ->
                            courseChipAdapter.notifyDataSetChanged()
                    );
                }
                conn.disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8081/api/professors/random");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) res.append(line);
                    in.close();

                    JSONArray array = new JSONArray(res.toString());
                    randomProfessorList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Log.d("ObjectName", obj.toString());
                        String fName = obj.optString("firstName", "");
                        String lName = obj.optString("lastName", "");
                        Log.d("ProfName", fName + lName);
                        String combinedName = (fName + " " + lName).trim();
                        if (combinedName.isEmpty()) combinedName = "Unknown Professor";

                        randomProfessorList.add(new Professor(
                                obj.optString("id", obj.optString("_id")),
                                obj.optString("firstName", ""),
                                obj.optString("lastName", ""),
                                obj.optString("title", "Faculty"),
                                obj.optDouble("rating", 0.0)
                        ));
                    }
                    cache.cachedProfessors.clear();
                    cache.cachedProfessors.addAll(randomProfessorList);
                    cache.loaded = true; // Mark as loaded only after both threads finish

                    new Handler(Looper.getMainLooper()).post(() ->
                            professorAdapter.notifyDataSetChanged()
                    );
                }
                conn.disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        android.widget.RatingBar ratingBar = bottomSheetView.findViewById(R.id.ratingBar);
        ratingBar.setRating(selectedRating);

        RangeSlider difficultySlider = bottomSheetView.findViewById(R.id.difficultySlider);
        difficultySlider.setValues(selectedDifficulty[0], selectedDifficulty[1]);

        RangeSlider hoursSlider = bottomSheetView.findViewById(R.id.hoursSlider);
        hoursSlider.setValues(selectedHours[0], selectedHours[1]);

        TextView clearAll = bottomSheetView.findViewById(R.id.clearAll);
        clearAll.setOnClickListener(v -> {
            ratingBar.setRating(4f);
            difficultySlider.setValues(1f, 5f);
            hoursSlider.setValues(1f, 9f);
        });

        View applyButton = bottomSheetView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(v -> {
            selectedRating = ratingBar.getRating();
            selectedDifficulty[0] = difficultySlider.getValues().get(0);
            selectedDifficulty[1] = difficultySlider.getValues().get(1);
            selectedHours[0] = hoursSlider.getValues().get(0);
            selectedHours[1] = hoursSlider.getValues().get(1);

            performSearch(searchEditText.getText().toString());
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void performSearch(String query) {
        hideRecentSearches();
        if (query == null || query.trim().isEmpty()) return;
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8");
        } catch (Exception e) {
            Log.e("SEARCH", "Encoding error", e);
            return;
        }

        // 1. SEARCH COURSES (Displays course details + embedded professor text labels)
        new Thread(() -> {
            try {
                String fullUrl = "http://10.0.2.2:8081/api/courses/search?q=" + encoded;
                HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                conn.setConnectTimeout(3000);
                int code = conn.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) res.append(line);
                    in.close();

                    JSONArray array = new JSONArray(res.toString());
                    List<Course> results = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        // Extracts professorName automatically to show who teaches it inside the card layout
                        String profName = obj.optString("professorName", "Staff");

                        results.add(new Course(
                                obj.optString("id", obj.optString("_id")),
                                obj.optString("code", ""),
                                obj.optString("title", ""),
                                obj.optInt("semester", 1),
                                obj.optInt("ects", 6),
                                profName,
                                obj.optDouble("rating", 0.0),
                                obj.optDouble("difficulty", 0.0),
                                obj.optDouble("studyHours", 0.0),
                                obj.optString("description", "")
                        ));
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        searchCourseResults.clear();
                        searchCourseResults.addAll(results);
                        searchCourseAdapter.notifyDataSetChanged();

                        int vis = results.isEmpty() ? View.GONE : View.VISIBLE;
                        searchResultsCoursesLabel.setVisibility(vis);
                        searchResultsCourses.setVisibility(vis);
                        showSearchResults();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("SEARCH", "Course search execution error", e);
            }
        }).start();

        // 2. SEARCH PROFESSORS (Pulls professor + lists their taught courses)
        new Thread(() -> {
            try {
                String fullUrl = "http://10.0.2.2:8081/api/professors/search?q=" + encoded;
                HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                conn.setConnectTimeout(3000);
                int code = conn.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) res.append(line);
                    in.close();

                    JSONArray array = new JSONArray(res.toString());
                    List<Professor> profResults = new ArrayList<>();
                    List<Course> linkedCoursesResults = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        // Pull the structural profile details
                        JSONObject profObj = obj.has("professor") ? obj.getJSONObject("professor") : obj;

                        Professor professor = new Professor(
                                profObj.optString("id", profObj.optString("_id")),
                                profObj.optString("firstName", ""),
                                profObj.optString("lastName", ""),
                                profObj.optString("title", "Faculty"),
                                profObj.optDouble("rating", 0.0)
                        );
                        profResults.add(professor);

                        // If your backend payload attaches a "courses" list inside this professor node:
                        if (obj.has("courses")) {
                            JSONArray coursesArray = obj.getJSONArray("courses");
                            for (int j = 0; j < coursesArray.length(); j++) {
                                JSONObject cObj = coursesArray.getJSONObject(j);
                                linkedCoursesResults.add(new Course(
                                        cObj.optString("id", cObj.optString("_id")),
                                        cObj.optString("code", ""),
                                        cObj.optString("title", ""),
                                        cObj.optInt("semester", 1),
                                        cObj.optInt("ects", 6),
                                        professor.getFullName(),
                                        cObj.optDouble("rating", 0.0),
                                        cObj.optDouble("difficulty", 0.0),
                                        cObj.optDouble("studyHours", 0.0),
                                        cObj.optString("description", "")
                                ));
                            }
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        searchProfessorResults.clear();
                        searchProfessorResults.addAll(profResults);
                        searchProfessorAdapter.notifyDataSetChanged();

                        // If a professor search reveals taught courses, append them to the course horizontal row
                        if (!linkedCoursesResults.isEmpty()) {
                            // Blend them cleanly without duplicates
                            for (Course c : linkedCoursesResults) {
                                if (!containsCourse(searchCourseResults, c.getId())) {
                                    searchCourseResults.add(c);
                                }
                            }
                            searchCourseAdapter.notifyDataSetChanged();
                            searchResultsCoursesLabel.setVisibility(View.VISIBLE);
                            searchResultsCourses.setVisibility(View.VISIBLE);
                        }

                        int vis = profResults.isEmpty() ? View.GONE : View.VISIBLE;
                        searchResultsProfessorsLabel.setVisibility(vis);
                        searchResultsProfessors.setVisibility(vis);
                        showSearchResults();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("SEARCH", "Professor search execution error", e);
            }
        }).start();
    }

    // Quick helper check utility to prevent duplicate cards filling the list rows
    private boolean containsCourse(List<Course> list, String id) {
        for (Course c : list) {
            if (c.getId().equals(id)) return true;
        }
        return false;
    }
    private void showSearchResults() {
        recommendationsScrollView.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void hideSearchDropdown() {
        searchResultsContainer.setVisibility(View.GONE);
        recommendationsScrollView.setVisibility(View.VISIBLE);
        searchCourseResults.clear();
        searchProfessorResults.clear();
        searchCourseAdapter.notifyDataSetChanged();
        searchProfessorAdapter.notifyDataSetChanged();
        searchResultsCoursesLabel.setVisibility(View.GONE);
        searchResultsProfessorsLabel.setVisibility(View.GONE);
        searchResultsCourses.setVisibility(View.GONE);
        searchResultsProfessors.setVisibility(View.GONE);
    }
    private void showRecentSearches() {
        searchBarContainer.setBackgroundResource(R.drawable.search_bar_focused_background);
    }

    private void viewClearFocus() {
        searchEditText.clearFocus();
    }

    private void hideRecentSearches() {
        searchBarContainer.setBackgroundResource(R.drawable.search_bar_background);
        viewClearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
        if (searchEditText.getText().toString().trim().isEmpty()) {
            recommendationsScrollView.setVisibility(View.VISIBLE);
        }
    }


}