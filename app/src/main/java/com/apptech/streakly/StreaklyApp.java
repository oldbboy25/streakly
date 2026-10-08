package com.apptech.streakly;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.apptech.streakly.backup.BackupManager;
import com.apptech.streakly.repository.HabitRepository;
import com.google.android.material.color.DynamicColors;

public class StreaklyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Material You dynamic color on Android 12+, graceful static fallback below that.
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Silent rolling local backup every time the app goes to background —
        // this is what makes "uninstalling never loses your data" actually true.
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                autoBackup();
            }
        });
    }

    private void autoBackup() {
        HabitRepository repository = HabitRepository.getInstance(this);
        BackupManager backupManager = new BackupManager();
        repository.getAllHabitsForBackup(habits ->
                repository.getAllCompletionsForBackup(completions ->
                        backupManager.autoBackupToInternal(StreaklyApp.this, habits, completions)));
    }
}
