package gr.aueb.lecturelens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.lecturelens.java.AppCache;
import gr.aueb.lecturelens.java.Course;
import gr.aueb.lecturelens.java.CourseChipAdapter;
import gr.aueb.lecturelens.java.Professor;
import gr.aueb.lecturelens.java.ProfessorAdapter;

public class SearchFragment extends Fragment implements
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

    // Integrated Team Fields
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // 1. Map base elements
        recentSearchesLayout = view.findViewById(R.id.recentSearchesLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchBarContainer = view.findViewById(R.id.searchBarContainer);
        TextView cancelSearch = view.findViewById(R.id.cancelSearch);

        recommendedCoursesRecycler = view.findViewById(R.id.recommendedCoursesRecyclerView);
        recommendedProfessorsRecycler = view.findViewById(R.id.recommendedProfessorsRecyclerView);
        recommendationsScrollView = view.findViewById(R.id.recommendationsScrollView);

        // 2. Bind default recommendations lists
        recommendedCoursesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        courseChipAdapter = new CourseChipAdapter(randomCourseList, this);
        recommendedCoursesRecycler.setAdapter(courseChipAdapter);

        recommendedProfessorsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        professorAdapter = new ProfessorAdapter(randomProfessorList, this);
        recommendedProfessorsRecycler.setAdapter(professorAdapter);

        // 3. Map new search result containers
        searchResultsContainer = view.findViewById(R.id.searchResultsContainer);
        searchResultsCoursesLabel = view.findViewById(R.id.searchResultsCoursesLabel);
        searchResultsProfessorsLabel = view.findViewById(R.id.searchResultsProfessorsLabel);

        searchResultsCourses = view.findViewById(R.id.searchResultsCourses);
        searchResultsCourses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        searchCourseAdapter = new CourseChipAdapter(searchCourseResults, this);
        searchResultsCourses.setAdapter(searchCourseAdapter);

        searchResultsProfessors = view.findViewById(R.id.searchResultsProfessors);
        searchResultsProfessors.setLayoutManager(new LinearLayoutManager(getContext()));
        searchProfessorAdapter = new ProfessorAdapter(searchProfessorResults, this);
        searchResultsProfessors.setAdapter(searchProfessorAdapter);

        // 4. Input Focus Controllers
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showRecentSearches();
        });
        searchEditText.setOnClickListener(v -> showRecentSearches());
        cancelSearch.setOnClickListener(v -> hideRecentSearches());

        // 5. Setup Live Debounce Search Observers
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


        LinearLayout filtersLayout = view.findViewById(R.id.filtersLayout);
        if (filtersLayout != null) {
            filtersLayout.setOnClickListener(v -> showFilterBottomSheet());
        }

        fetchRecommendationsFromDatabase();

        return view;
    }

    @Override
    public void onCourseChipClick(Course course) {
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        intent.putExtra("CHOSEN_COURSE", course);
        startActivity(intent);
    }

    @Override
    public void onProfClick(Professor prof) {
        Intent intent = new Intent(getActivity(), ProfessorDetailsActivity.class); // Keep matching target logic details activity names
        intent.putExtra("PROFESSOR_ID", prof.getId());
        intent.putExtra("CHOSEN_PROFESSOR", prof);
        startActivity(intent);
    }

    private void fetchRecommendationsFromDatabase() {
        AppCache cache = AppCache.getInstance();
        if (cache.loaded) {
            randomCourseList.clear();
            randomProfessorList.clear();
            randomCourseList.addAll(cache.cachedCourses);
            randomProfessorList.addAll(cache.cachedProfessors);
            courseChipAdapter.notifyDataSetChanged();
            professorAdapter.notifyDataSetChanged();
            return;
        }

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
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded()) courseChipAdapter.notifyDataSetChanged();
                    });
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
                        String fName = obj.optString("firstName", "");
                        String lName = obj.optString("lastName", "");
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
                    cache.loaded = true;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded()) professorAdapter.notifyDataSetChanged();
                    });
                }
                conn.disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showFilterBottomSheet() {
        if (getActivity() == null) return;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
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

        // 1. SEARCH COURSES
        new Thread(() -> {
            try {
                String fullUrl = "http://10.0.2.2:8081/api/courses/search?q=" + encoded;
                HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                conn.setConnectTimeout(3000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) res.append(line);
                    in.close();

                    JSONArray array = new JSONArray(res.toString());
                    List<Course> results = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        results.add(new Course(
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
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded()) {
                            searchCourseResults.clear();
                            searchCourseResults.addAll(results);
                            searchCourseAdapter.notifyDataSetChanged();

                            int vis = results.isEmpty() ? View.GONE : View.VISIBLE;
                            searchResultsCoursesLabel.setVisibility(vis);
                            searchResultsCourses.setVisibility(vis);
                            showSearchResults();
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) { Log.e("SEARCH", "Course loop error", e); }
        }).start();

        // 2. SEARCH PROFESSORS
        new Thread(() -> {
            try {
                String fullUrl = "http://10.0.2.2:8081/api/professors/search?q=" + encoded;
                HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                conn.setConnectTimeout(3000);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
                        JSONObject profObj = obj.has("professor") ? obj.getJSONObject("professor") : obj;

                        Professor professor = new Professor(
                                profObj.optString("id", profObj.optString("_id")),
                                profObj.optString("firstName", ""),
                                profObj.optString("lastName", ""),
                                profObj.optString("title", "Faculty"),
                                profObj.optDouble("rating", 0.0)
                        );
                        profResults.add(professor);

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
                        if (isAdded()) {
                            searchProfessorResults.clear();
                            searchProfessorResults.addAll(profResults);
                            searchProfessorAdapter.notifyDataSetChanged();

                            if (!linkedCoursesResults.isEmpty()) {
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
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) { Log.e("SEARCH", "Professor thread error", e); }
        }).start();
    }

    private boolean containsCourse(List<Course> list, String id) {
        for (Course c : list) {
            if (c.getId().equals(id)) return true;
        }
        return false;
    }

    private void showSearchResults() {
        recentSearchesLayout.setVisibility(View.GONE);
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
        recentSearchesLayout.setVisibility(View.VISIBLE);
        searchBarContainer.setBackgroundResource(R.drawable.search_bar_focused_background);
    }

    private void viewClearFocus() {
        searchEditText.clearFocus();
    }

    private void hideRecentSearches() {
        recentSearchesLayout.setVisibility(View.GONE);
        searchBarContainer.setBackgroundResource(R.drawable.search_bar_background);
        viewClearFocus();
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        }
        if (searchEditText.getText().toString().trim().isEmpty()) {
            recommendationsScrollView.setVisibility(View.VISIBLE);
        }
    }
}