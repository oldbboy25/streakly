package com.apptech.streakly.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.apptech.streakly.R;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.achievements.AchievementsFragment;
import com.apptech.streakly.ui.onboarding.OnboardingActivity;
import com.apptech.streakly.ui.settings.SettingsFragment;
import com.apptech.streakly.ui.stats.StatsFragment;
import com.apptech.streakly.ui.today.TodayFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (savedInstanceState == null) {
            showFragment(new TodayFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today) {
                showFragment(new TodayFragment());
                return true;
            } else if (id == R.id.nav_stats) {
                showFragment(new StatsFragment());
                return true;
            } else if (id == R.id.nav_achievements) {
                showFragment(new AchievementsFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                showFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
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
