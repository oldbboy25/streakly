package com.apptech.streakly.ui.today;

import com.apptech.streakly.model.Habit;

public class HabitUiItem {
    public final Habit habit;
    public final boolean completedToday;
    public final int currentStreak;

    public HabitUiItem(Habit habit, boolean completedToday, int currentStreak) {
        this.habit = habit;
        this.completedToday = completedToday;
        this.currentStreak = currentStreak;
    }
}
