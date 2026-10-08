package com.apptech.streakly.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_completions")
public class HabitCompletion {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long habitId;

    /** java.time.LocalDate#toEpochDay() */
    public long epochDay;

    /** How many times the habit was done that day (counter-style habits). */
    public int count = 1;

    public HabitCompletion() {
    }

    @Ignore
    public HabitCompletion(long habitId, long epochDay, int count) {
        this.habitId = habitId;
        this.epochDay = epochDay;
        this.count = count;
    }
}
