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

        navHomeBg = findViewById(R.id.navHomeBg);
        navSearchBg = findViewById(R.id.navSearchBg);
        navProfileBg = findViewById(R.id.navProfileBg);

        viewPager = findViewById(R.id.viewPager);
        MainNavigationAdapter adapter = new MainNavigationAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(2);

        setupNavigationListeners();
        setupSwipeListener();
    }

    private void setupNavigationListeners() {
        findViewById(R.id.navHome).setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        findViewById(R.id.navSearch).setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        findViewById(R.id.navProfile).setOnClickListener(v -> viewPager.setCurrentItem(2, true));
    }

    private void setupSwipeListener() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavbarHighlights(position);
            }
        });
    }

    public void jumpToSearchTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1, true);
        }
    }

    private void updateNavbarHighlights(int position) {
        navHomeBg.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        navSearchBg.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        navProfileBg.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }
}