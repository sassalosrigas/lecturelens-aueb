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

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        View applyButton = bottomSheetView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
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
}
