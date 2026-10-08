package com.apptech.streakly.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.apptech.streakly.R;
import com.apptech.streakly.ui.MainActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "streakly_onboarding_prefs";
    private static final String KEY_SEEN = "has_seen_onboarding";

    private final List<View> dots = new ArrayList<>();

    public static boolean hasSeenOnboarding(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SEEN, false);
    }

    private static void markSeen(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_SEEN, true).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        List<OnboardingPage> pages = new ArrayList<>();
        pages.add(new OnboardingPage("🔥", R.string.onboarding_1_title, R.string.onboarding_1_subtitle));
        pages.add(new OnboardingPage("🔒", R.string.onboarding_2_title, R.string.onboarding_2_subtitle));
        pages.add(new OnboardingPage("✨", R.string.onboarding_3_title, R.string.onboarding_3_subtitle));

        ViewPager2 pager = findViewById(R.id.onboarding_pager);
        pager.setAdapter(new OnboardingPagerAdapter(pages));

        LinearLayout dotsContainer = findViewById(R.id.dots_container);
        buildDots(dotsContainer, pages.size());

        MaterialButton nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> {
            int next = pager.getCurrentItem() + 1;
            if (next >= pages.size()) {
                finishOnboarding();
            } else {
                pager.setCurrentItem(next, true);
            }
        });

        findViewById(R.id.skip_button).setOnClickListener(v -> finishOnboarding());

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                nextButton.setText(position == pages.size() - 1
                        ? R.string.onboarding_get_started
                        : R.string.onboarding_next);
            }
        });
    }

    private void buildDots(LinearLayout dotsContainer, int count) {
        int size = dp(8);
        int margin = dp(4);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginStart(margin);
            params.setMarginEnd(margin);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            dotsContainer.addView(dot);
            dots.add(dot);
        }
    }

    private void updateDots(int activeIndex) {
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setBackgroundResource(i == activeIndex ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        }
    }

    private void finishOnboarding() {
        markSeen(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
