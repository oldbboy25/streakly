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
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.apptech.streakly.R;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.onboarding.OnboardingActivity;
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

        FragmentContainerView navHostView = findViewById(R.id.nav_host_fragment);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(navHostView.getId());
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
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
