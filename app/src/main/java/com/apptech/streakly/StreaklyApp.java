package com.apptech.streakly;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.apptech.streakly.backup.BackupManager;
import com.apptech.streakly.repository.HabitRepository;

public class StreaklyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Dynamic color (Material You) is intentionally NOT applied here: it pulls
        // tones from the device wallpaper, and some OEM skins (e.g. MIUI) apply it
        // inconsistently across components, producing mismatched, muddy-looking
        // surfaces. The app's own static palette (see themes.xml) is designed to be
        // cohesive on every device, so we use that everywhere instead.

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
