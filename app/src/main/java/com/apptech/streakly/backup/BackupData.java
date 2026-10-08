package com.apptech.streakly.backup;

import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;

import java.util.List;

/** Plain POJO serialized to/from JSON via Gson. Field names are the on-disk schema — don't rename lightly. */
public class BackupData {

    public int schemaVersion = 1;
    public long exportedAt;
    public List<Habit> habits;
    public List<HabitCompletion> completions;

    public BackupData() {
    }

    public BackupData(List<Habit> habits, List<HabitCompletion> completions) {
        this.exportedAt = System.currentTimeMillis();
        this.habits = habits;
        this.completions = completions;
    }
}
