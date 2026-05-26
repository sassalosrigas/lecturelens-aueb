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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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

    private float selectedCourseRating = 0f;
    private float[] selectedCourseDifficulty = {0f, 5f};
    private float[] selectedCourseHours = {0f, 20f};
    private float selectedProfRating = 0f;

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

    private TextView searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recentSearchesLayout = view.findViewById(R.id.recentSearchesLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchBarContainer = view.findViewById(R.id.searchBarContainer);
        TextView cancelSearch = view.findViewById(R.id.cancelSearch);
        if (cancelSearch != null) {
            cancelSearch.setOnClickListener(v -> {
                searchEditText.setText("");

                hideRecentSearches();

                hideSearchDropdown();
            });
        }

        searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) performSearch(query);
        });

        recommendedCoursesRecycler = view.findViewById(R.id.recommendedCoursesRecyclerView);
        recommendedProfessorsRecycler = view.findViewById(R.id.recommendedProfessorsRecyclerView);
        recommendationsScrollView = view.findViewById(R.id.recommendationsScrollView);

        recommendedCoursesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        courseChipAdapter = new CourseChipAdapter(randomCourseList, this);
        recommendedCoursesRecycler.setAdapter(courseChipAdapter);

        recommendedProfessorsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        professorAdapter = new ProfessorAdapter(randomProfessorList, this);
        recommendedProfessorsRecycler.setAdapter(professorAdapter);

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

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            searchBarContainer.setBackgroundResource(R.drawable.search_bar_focused_background);

            if (cancelSearch != null) {
                cancelSearch.setVisibility(View.VISIBLE);
            }
        });

        LinearLayout filtersLayout = view.findViewById(R.id.filtersLayout);
        filtersLayout.setOnClickListener(v -> showFilterBottomSheet());

        fetchRecommendationsFromDatabase();

        return view;
    }

    @Override
    public void onCourseChipClick(Course course) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        intent.putExtra("CHOSEN_COURSE", course);
        startActivity(intent);
    }

    @Override
    public void onProfClick(Professor prof) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), ProfessorDetailsActivity.class);
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
        if (getContext() == null) return;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        android.widget.RatingBar courseRatingBar = bottomSheetView.findViewById(R.id.courseRatingBar);
        RangeSlider courseDifficultySlider = bottomSheetView.findViewById(R.id.courseDifficultySlider);
        RangeSlider courseHoursSlider = bottomSheetView.findViewById(R.id.courseHoursSlider);

        courseRatingBar.setRating(selectedCourseRating);
        courseDifficultySlider.setValues(selectedCourseDifficulty[0], selectedCourseDifficulty[1]);
        courseHoursSlider.setValues(selectedCourseHours[0], selectedCourseHours[1]);

        android.widget.RatingBar profRatingBar = bottomSheetView.findViewById(R.id.profRatingBar);
        profRatingBar.setRating(selectedProfRating);

        TextView clearAll = bottomSheetView.findViewById(R.id.clearAll);
        clearAll.setOnClickListener(v -> {
            courseRatingBar.setRating(0f);
            courseDifficultySlider.setValues(0f, 5.0f);
            courseHoursSlider.setValues(0f, 20.0f);
            profRatingBar.setRating(0f);
        });

        View applyButton = bottomSheetView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(v -> {
            selectedCourseRating = courseRatingBar.getRating();
            selectedCourseDifficulty[0] = courseDifficultySlider.getValues().get(0);
            selectedCourseDifficulty[1] = courseDifficultySlider.getValues().get(1);
            selectedCourseHours[0] = courseHoursSlider.getValues().get(0);
            selectedCourseHours[1] = courseHoursSlider.getValues().get(1);

            selectedProfRating = profRatingBar.getRating();

            bottomSheetDialog.dismiss();

            applyFiltersToRecommendations();

            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });

        bottomSheetDialog.show();
    }

    private void applyFiltersToRecommendations() {
        AppCache cache = AppCache.getInstance();
        if (!cache.loaded) return;

        List<Course> filteredCourses = new ArrayList<>();
        for (Course c : cache.cachedCourses) {

            boolean matchesRating = c.getRating() >= selectedCourseRating;

            boolean matchesDifficulty = (c.getDifficulty() >= selectedCourseDifficulty[0] && c.getDifficulty() <= selectedCourseDifficulty[1])
                    || c.getDifficulty() == 0.0;

            boolean matchesHours = (c.getHours() >= selectedCourseHours[0] && c.getHours() <= selectedCourseHours[1])
                    || c.getHours() == 0.0;

            if (matchesRating && matchesDifficulty && matchesHours) {
                filteredCourses.add(c);
            }
        }
        randomCourseList.clear();
        randomCourseList.addAll(filteredCourses);
        courseChipAdapter.notifyDataSetChanged();

        List<Professor> filteredProfs = new ArrayList<>();
        for (Professor p : cache.cachedProfessors) {
            if (p.getRating() >= selectedProfRating) {
                filteredProfs.add(p);
            }
        }
        randomProfessorList.clear();
        randomProfessorList.addAll(filteredProfs);
        professorAdapter.notifyDataSetChanged();
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;

        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8");
        } catch (Exception e) {
            Log.e("SEARCH", "Encoding error", e);
            return;
        }

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
                    List<Course> courseResults = new ArrayList<>();
                    List<Professor> linkedProfessorsResults = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject wrapperObj = array.getJSONObject(i);

                        JSONObject courseObj = wrapperObj.getJSONObject("course");
                        String profName = courseObj.optString("professorName", "Staff");

                        Course course = new Course(
                                courseObj.optString("id", courseObj.optString("_id")),
                                courseObj.optString("code", ""),
                                courseObj.optString("title", ""),
                                courseObj.optInt("semester", 1),
                                courseObj.optInt("ects", 6),
                                profName,
                                courseObj.optDouble("rating", 0.0),
                                courseObj.optDouble("difficulty", 0.0),
                                courseObj.optDouble("studyHours", 0.0),
                                courseObj.optString("description", "")
                        );
                        courseResults.add(course);

                        if (wrapperObj.has("professors")) {
                            JSONArray profsArray = wrapperObj.getJSONArray("professors");
                            for (int j = 0; j < profsArray.length(); j++) {
                                JSONObject profObj = profsArray.getJSONObject(j);
                                linkedProfessorsResults.add(new Professor(
                                        profObj.optString("id", profObj.optString("_id")),
                                        profObj.optString("firstName", ""),
                                        profObj.optString("lastName", ""),
                                        profObj.optString("title", "Faculty"),
                                        profObj.optDouble("rating", 0.0)
                                ));
                            }
                        }
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded()) {
                            searchCourseResults.clear();
                            searchCourseResults.addAll(courseResults);
                            searchCourseAdapter.notifyDataSetChanged();

                            if (!linkedProfessorsResults.isEmpty()) {
                                for (Professor p : linkedProfessorsResults) {
                                    if (!containsProfessor(searchProfessorResults, p.getId())) {
                                        searchProfessorResults.add(p);
                                    }
                                }
                                searchProfessorAdapter.notifyDataSetChanged();
                                searchResultsProfessorsLabel.setVisibility(View.VISIBLE);
                                searchResultsProfessors.setVisibility(View.VISIBLE);
                            }

                            int vis = courseResults.isEmpty() ? View.GONE : View.VISIBLE;
                            searchResultsCoursesLabel.setVisibility(vis);
                            searchResultsCourses.setVisibility(vis);
                            showSearchResults();
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("SEARCH", "Course search error", e);
            }
        }).start();

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
            } catch (Exception e) {
                Log.e("SEARCH", "Professor search error", e);
            }
        }).start();
    }

    private boolean containsProfessor(List<Professor> list, String id) {
        for (Professor p : list) {
            if (p.getId().equals(id)) return true;
        }
        return false;
    }

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

        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        }


        if (getView() != null) {
            View cancelBtn = getView().findViewById(R.id.cancelSearch);
            if (cancelBtn != null) {
                cancelBtn.setVisibility(View.GONE);
            }
        }

        if (searchEditText.getText().toString().trim().isEmpty()) {
            recommendationsScrollView.setVisibility(View.VISIBLE);
        }
    }
}