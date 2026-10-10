package com.apptech.streakly.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.apptech.streakly.R;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.onboarding.OnboardingActivity;
import com.apptech.streakly.ui.settings.SettingsFragment;
import com.apptech.streakly.ui.stats.StatsFragment;
import com.apptech.streakly.ui.today.TodayFragment;

public class MainActivity extends AppCompatActivity {

    private enum Tab { TODAY, STATS, SETTINGS }

    private View navToday;
    private View navStats;
    private View navSettings;
    private Tab selectedTab = Tab.TODAY;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (!OnboardingActivity.hasSeenOnboarding(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        requestNotificationPermissionIfNeeded();
        resyncReminders();

        navToday = findViewById(R.id.nav_today);
        navStats = findViewById(R.id.nav_stats);
        navSettings = findViewById(R.id.nav_settings);

        navToday.setOnClickListener(v -> selectTab(Tab.TODAY));
        navStats.setOnClickListener(v -> selectTab(Tab.STATS));
        navSettings.setOnClickListener(v -> selectTab(Tab.SETTINGS));

        if (savedInstanceState == null) {
            showFragment(new TodayFragment());
        }
        updateNavSelection();
    }

    private void selectTab(Tab tab) {
        if (tab == selectedTab) {
            return;
        }
        selectedTab = tab;
        switch (tab) {
            case TODAY:
                showFragment(new TodayFragment());
                break;
            case STATS:
                showFragment(new StatsFragment());
                break;
            case SETTINGS:
                showFragment(new SettingsFragment());
                break;
        }
        updateNavSelection();
    }

    private void updateNavSelection() {
        setNavItemSelected(navToday, selectedTab == Tab.TODAY);
        setNavItemSelected(navStats, selectedTab == Tab.STATS);
        setNavItemSelected(navSettings, selectedTab == Tab.SETTINGS);
    }

    private void setNavItemSelected(View navItem, boolean selected) {
        int color = ContextCompat.getColor(this, selected
                ? R.color.bottom_nav_icon_selected
                : R.color.bottom_nav_icon_unselected);
        ImageView icon = (ImageView) ((android.view.ViewGroup) navItem).getChildAt(0);
        TextView label = (TextView) ((android.view.ViewGroup) navItem).getChildAt(1);
        ImageViewCompat.setImageTintList(icon, android.content.res.ColorStateList.valueOf(color));
        label.setTextColor(color);
        label.setAlpha(selected ? 1f : 0.85f);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Re-schedules every reminder-enabled habit on each app open. This is what makes a
     * permission grant (the user coming back from "Alarms & reminders" settings) actually
     * take effect without requiring them to re-open and re-save each habit individually.
     */
    private void resyncReminders() {
        if (!ReminderScheduler.canScheduleExactAlarms(this)) {
            return;
        }
        HabitRepository.getInstance(this).getHabitsWithReminders(habits -> {
            for (var habit : habits) {
                ReminderScheduler.schedule(this, habit);
            }
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
