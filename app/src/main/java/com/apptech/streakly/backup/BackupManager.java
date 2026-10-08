package com.apptech.streakly.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Everything here is plain file I/O against either a user-chosen SAF Uri (manual
 * export/import) or this app's own private files dir (silent rolling auto-backup).
 * Nothing in this class ever touches the network.
 */
public class BackupManager {

    private static final String PREFS_NAME = "streakly_backup_prefs";
    private static final String KEY_LAST_BACKUP_AT = "last_backup_at";
    private static final String AUTO_BACKUP_DIR = "backups";
    private static final String AUTO_BACKUP_PREFIX = "autobackup_";
    private static final int MAX_AUTO_BACKUPS = 5;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void exportToUri(Context context, Uri uri, List<Habit> habits, List<HabitCompletion> completions) throws IOException {
        BackupData data = new BackupData(habits, completions);
        String json = gson.toJson(data);
        try (OutputStream out = context.getContentResolver().openOutputStream(uri, "w")) {
            if (out == null) {
                throw new IOException("Could not open output stream for " + uri);
            }
            try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writer.write(json);
            }
        }
        recordBackupTimestamp(context);
    }

    public BackupData importFromUri(Context context, Uri uri) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Could not open input stream for " + uri);
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                BackupData data = gson.fromJson(reader, BackupData.class);
                if (data == null || data.habits == null || data.completions == null) {
                    throw new IOException("Backup file is empty or malformed");
                }
                return data;
            }
        }
    }

    /** Silent rolling backup kept entirely inside this app's private storage, no permission needed. */
    public void autoBackupToInternal(Context context, List<Habit> habits, List<HabitCompletion> completions) {
        try {
            File dir = new File(context.getFilesDir(), AUTO_BACKUP_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = AUTO_BACKUP_PREFIX + System.currentTimeMillis() + ".json";
            File file = new File(dir, fileName);
            BackupData data = new BackupData(habits, completions);
            String json = gson.toJson(data);
            try (OutputStream out = new FileOutputStream(file)) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            }
            pruneOldAutoBackups(dir);
        } catch (IOException ignored) {
            // Best-effort silent backup; the user-triggered export is the reliable path.
        }
    }

    private void pruneOldAutoBackups(File dir) {
        File[] files = dir.listFiles((d, name) -> name.startsWith(AUTO_BACKUP_PREFIX));
        if (files == null || files.length <= MAX_AUTO_BACKUPS) {
            return;
        }
        List<File> sorted = new ArrayList<>(Arrays.asList(files));
        sorted.sort(Comparator.comparingLong(File::lastModified));
        int toDelete = sorted.size() - MAX_AUTO_BACKUPS;
        for (int i = 0; i < toDelete; i++) {
            sorted.get(i).delete();
        }
    }

    public long getLastBackupTimestamp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_BACKUP_AT, 0L);
    }

    public void recordBackupTimestamp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_BACKUP_AT, System.currentTimeMillis()).apply();
    }
}
