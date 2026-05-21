package gr.aueb.lecturelens;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private View navHomeBg, navSearchBg, navProfileBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Map highlight background chips from activity_main.xml
        navHomeBg = findViewById(R.id.navHomeBg);
        navSearchBg = findViewById(R.id.navSearchBg);
        navProfileBg = findViewById(R.id.navProfileBg);

        // 2. Setup ViewPager2 canvas and anchor our modular swipe adapter
        viewPager = findViewById(R.id.viewPager);
        MainNavigationAdapter adapter = new MainNavigationAdapter(this);
        viewPager.setAdapter(adapter);

        // Keeps your fragments warm in memory so they don't reload when swiping
        viewPager.setOffscreenPageLimit(2);

        // 3. Connect navigation interactions
        setupNavigationListeners();
        setupSwipeListener();
    }

    private void setupNavigationListeners() {
        // Tapping icons moves the ViewPager scrolling window smoothly
        findViewById(R.id.navHome).setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        findViewById(R.id.navSearch).setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        findViewById(R.id.navProfile).setOnClickListener(v -> viewPager.setCurrentItem(2, true));
    }

    private void setupSwipeListener() {
        // Intercepts swiping motions to shift the navbar backgrounds instantly
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavbarHighlights(position);
            }
        });
    }

    /**
     * Public helper method so HomeFragment's fake search bar can
     * tell MainActivity to scroll over to the Search tab automatically.
     */
    public void jumpToSearchTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1, true);
        }
    }

    private void updateNavbarHighlights(int position) {
        // Match the visibility states of pink chips depending on active view index
        navHomeBg.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        navSearchBg.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        navProfileBg.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }
}