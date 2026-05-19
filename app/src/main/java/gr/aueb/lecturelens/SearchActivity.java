package gr.aueb.lecturelens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.slider.RangeSlider;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SearchActivity extends AppCompatActivity {

    private View recentSearchesLayout;
    private EditText searchEditText;
    private View searchBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recentSearchesLayout = findViewById(R.id.recentSearchesLayout);
        searchEditText = findViewById(R.id.searchEditText);
        searchBarContainer = findViewById(R.id.searchBarContainer);
        TextView cancelSearch = findViewById(R.id.cancelSearch);

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showRecentSearches();
                }
            }
        });

        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecentSearches();
            }
        });

        cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRecentSearches();
            }
        });

        populateRecentSearches();
        setupCourseChipClickListeners();
        setupProfessorClickListeners();

        ImageView navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Add more listeners as needed
        ImageView navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        LinearLayout filtersLayout = findViewById(R.id.filtersLayout);
        filtersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterBottomSheet();
            }
        });
    }

    private float[] selectedDifficulty = {1f, 5f};
    private float[] selectedHours = {1f, 9f};
    private float selectedRating = 4f;

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(
                R.layout.layout_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // --- Rating Bar ---
        android.widget.RatingBar ratingBar =
                bottomSheetView.findViewById(R.id.ratingBar);
        ratingBar.setRating(selectedRating);

        // --- Difficulty Slider ---
        RangeSlider difficultySlider =
                bottomSheetView.findViewById(R.id.difficultySlider);
        difficultySlider.setValues(selectedDifficulty[0], selectedDifficulty[1]);
        difficultySlider.setLabelFormatter(value ->
                String.valueOf((int) value));

        // --- Hours Slider ---
        RangeSlider hoursSlider =
                bottomSheetView.findViewById(R.id.hoursSlider);
        hoursSlider.setValues(selectedHours[0], selectedHours[1]);
        hoursSlider.setLabelFormatter(value ->
                value >= 9f ? "9+" : String.valueOf((int) value));

        // --- Clear All ---
        TextView clearAll = bottomSheetView.findViewById(R.id.clearAll);
        clearAll.setOnClickListener(v -> {
            ratingBar.setRating(4f);
            difficultySlider.setValues(1f, 5f);
            hoursSlider.setValues(1f, 9f);
        });

        // --- Apply Button ---
        View applyButton = bottomSheetView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(v -> {
            // Save selected values for next time sheet opens
            selectedRating = ratingBar.getRating();

            List<Float> diffVals = difficultySlider.getValues();
            selectedDifficulty[0] = diffVals.get(0);
            selectedDifficulty[1] = diffVals.get(1);

            List<Float> hourVals = hoursSlider.getValues();
            selectedHours[0] = hourVals.get(0);
            selectedHours[1] = hourVals.get(1);

            performSearch(searchEditText.getText().toString());

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void performSearch(String query) {
        // Implementation for filtering courses based on query, selectedRating, selectedDifficulty, and selectedHours
    }

    private void showRecentSearches() {
        recentSearchesLayout.setVisibility(View.VISIBLE);
        searchBarContainer.setBackgroundResource(R.drawable.search_bar_focused_background);
    }

    private void hideRecentSearches() {
        recentSearchesLayout.setVisibility(View.GONE);
        searchBarContainer.setBackgroundResource(R.drawable.search_bar_background);
        searchEditText.clearFocus();
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    private void populateRecentSearches() {
        LinearLayout list = findViewById(R.id.recentSearchesList);
        String[] demoSearches = {
                getString(R.string.search_demo_1),
                getString(R.string.search_demo_2),
                getString(R.string.search_demo_3),
                getString(R.string.search_demo_4)
        };

        for (int i = 0; i < list.getChildCount() && i < demoSearches.length; i++) {
            View item = list.getChildAt(i);
            TextView text = item.findViewById(R.id.searchText);
            if (text != null) {
                text.setText(demoSearches[i]);
            }
        }
    }

    private void setupCourseChipClickListeners() {
        ViewGroup container = findViewById(android.R.id.content);
        findAndSetChipClickListeners(container);
    }

    private void findAndSetChipClickListeners(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getId() == R.id.courseChipCard) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(SearchActivity.this, CourseDetailsActivity.class));
                    }
                });
            } else if (child instanceof ViewGroup) {
                findAndSetChipClickListeners((ViewGroup) child);
            }
        }
    }

    private void setupProfessorClickListeners() {
        ViewGroup container = findViewById(android.R.id.content);
        findAndSetProfessorClickListeners(container);
    }

    private void findAndSetProfessorClickListeners(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getId() == R.id.professorChipContainer) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(SearchActivity.this, ProfessorDetailsActivity.class));
                    }
                });
            } else if (child instanceof ViewGroup) {
                findAndSetProfessorClickListeners((ViewGroup) child);
            }
        }
    }
}
