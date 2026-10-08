package com.apptech.streakly.ui.settings;

import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apptech.streakly.BuildConfig;
import com.apptech.streakly.R;
import com.apptech.streakly.backup.BackupData;
import com.apptech.streakly.backup.BackupManager;
import com.apptech.streakly.growth.ShareHelper;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private final BackupManager backupManager = new BackupManager();
    private HabitRepository repository;
    private TextView lastBackupText;

    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri != null) {
                    performExport(uri);
                }
            });

    private final ActivityResultLauncher<String[]> importLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    performImport(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = HabitRepository.getInstance(requireContext());

        lastBackupText = view.findViewById(R.id.last_backup_text);
        refreshLastBackupText();

        TextView versionText = view.findViewById(R.id.about_version_text);
        versionText.setText(getString(R.string.settings_version_format, BuildConfig.VERSION_NAME));

        view.findViewById(R.id.btn_export).setOnClickListener(v -> {
            String fileName = "streakly_backup_"
                    + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".json";
            exportLauncher.launch(fileName);
        });

        view.findViewById(R.id.btn_import).setOnClickListener(v ->
                importLauncher.launch(new String[]{"application/json", "text/plain", "*/*"}));

        view.findViewById(R.id.btn_rate).setOnClickListener(v ->
                ShareHelper.openPlayStoreListing(requireContext()));
        view.findViewById(R.id.btn_share).setOnClickListener(v ->
                ShareHelper.shareApp(requireContext()));
    }

    private void refreshLastBackupText() {
        long timestamp = backupManager.getLastBackupTimestamp(requireContext());
        if (timestamp <= 0) {
            lastBackupText.setText(R.string.settings_last_backup_never);
        } else {
            CharSequence relative = DateUtils.getRelativeTimeSpanString(timestamp);
            lastBackupText.setText(getString(R.string.settings_last_backup_format, relative));
        }
    }

    private void performExport(Uri uri) {
        repository.getAllHabitsForBackup(habits ->
                repository.getAllCompletionsForBackup(completions ->
                        repository.runOnBackground(() -> {
                            try {
                                backupManager.exportToUri(requireContext(), uri, habits, completions);
                                postToUi(() -> {
                                    refreshLastBackupText();
                                    toast(R.string.settings_export_success);
                                });
                            } catch (IOException e) {
                                postToUi(() -> toast(R.string.settings_export_failure));
                            }
                        })));
    }

    private void performImport(Uri uri) {
        repository.runOnBackground(() -> {
            try {
                BackupData data = backupManager.importFromUri(requireContext(), uri);
                repository.replaceAllData(data.habits, data.completions, () -> {
                    if (data.habits != null) {
                        for (Habit habit : data.habits) {
                            if (habit.reminderEnabled) {
                                ReminderScheduler.schedule(requireContext(), habit);
                            }
                        }
                    }
                    postToUi(() -> toast(R.string.settings_import_success));
                });
            } catch (IOException e) {
                postToUi(() -> toast(R.string.settings_import_failure));
            }
        });
    }

    private void postToUi(Runnable runnable) {
        if (isAdded()) {
            requireActivity().runOnUiThread(runnable);
        }
    }

    private void toast(int resId) {
        if (getContext() != null) {
            Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
        }
    }
}
