package com.apptech.streakly.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public String emoji;

    public String colorHex;

    public FrequencyType frequencyType;

    /** Bitmask, bit 0 = Monday ... bit 6 = Sunday. Only used when frequencyType == SPECIFIC_DAYS. */
    public int specificDaysMask;

    /** Only used when frequencyType == TIMES_PER_WEEK. */
    public int timesPerWeekTarget;

    public boolean reminderEnabled;

    /** -1 when no reminder time is set. */
    public int reminderHour = -1;
    public int reminderMinute = -1;

    public long createdAt;

    public int sortOrder;

    public boolean archived;

    public Habit() {
    }

    public boolean isScheduledOn(int isoDayOfWeek) {
        // isoDayOfWeek: 1 = Monday ... 7 = Sunday (java.time.DayOfWeek.getValue())
        switch (frequencyType) {
            case DAILY:
                return true;
            case SPECIFIC_DAYS:
                return (specificDaysMask & (1 << (isoDayOfWeek - 1))) != 0;
            case TIMES_PER_WEEK:
                return true;
            default:
                return true;
        }
    }
}
