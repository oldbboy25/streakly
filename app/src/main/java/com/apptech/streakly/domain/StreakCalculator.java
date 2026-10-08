package com.apptech.streakly.domain;

import com.apptech.streakly.model.FrequencyType;
import com.apptech.streakly.model.Habit;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pure, offline streak math. No I/O, no Android dependencies, easy to reason about
 * and unit test independently of the database.
 */
public final class StreakCalculator {

    private StreakCalculator() {
    }

    public static int currentStreakDays(Habit habit, Set<Long> completedDays, long todayEpochDay) {
        if (habit.frequencyType == FrequencyType.TIMES_PER_WEEK) {
            return currentWeeklyStreak(habit, completedDays, todayEpochDay);
        }
        int streak = 0;
        long day = todayEpochDay;
        boolean isToday = true;
        while (todayEpochDay - day < 3650) {
            LocalDate date = LocalDate.ofEpochDay(day);
            boolean due = habit.isScheduledOn(date.getDayOfWeek().getValue());
            boolean done = completedDays.contains(day);

            if (isToday && due && !done) {
                // today's window isn't over yet; skip without breaking the streak
                isToday = false;
                day--;
                continue;
            }
            isToday = false;

            if (!due) {
                day--;
                continue;
            }
            if (done) {
                streak++;
                day--;
            } else {
                break;
            }
        }
        return streak;
    }

    public static int longestStreakDays(Habit habit, Set<Long> completedDays) {
        if (completedDays.isEmpty()) {
            return 0;
        }
        if (habit.frequencyType == FrequencyType.TIMES_PER_WEEK) {
            return longestWeeklyStreak(habit, completedDays);
        }
        long minDay = Collections.min(completedDays);
        long maxDay = Collections.max(completedDays);
        int longest = 0;
        int run = 0;
        for (long day = minDay; day <= maxDay; day++) {
            LocalDate date = LocalDate.ofEpochDay(day);
            boolean due = habit.isScheduledOn(date.getDayOfWeek().getValue());
            if (!due) {
                continue;
            }
            if (completedDays.contains(day)) {
                run++;
                longest = Math.max(longest, run);
            } else {
                run = 0;
            }
        }
        return longest;
    }

    private static int currentWeeklyStreak(Habit habit, Set<Long> completedDays, long todayEpochDay) {
        if (completedDays.isEmpty()) {
            return 0;
        }
        Map<Long, Integer> weekCounts = buildWeekCounts(completedDays);
        int target = Math.max(1, habit.timesPerWeekTarget);

        LocalDate cursor = LocalDate.ofEpochDay(todayEpochDay);
        long currentWeek = weekKey(cursor);
        if (weekCounts.getOrDefault(currentWeek, 0) < target) {
            // this week is still in progress and hasn't hit target yet; don't count it
            // as a break, just start evaluating from last week.
            cursor = cursor.minusWeeks(1);
        }

        int streak = 0;
        while (true) {
            long wk = weekKey(cursor);
            int count = weekCounts.getOrDefault(wk, 0);
            if (count >= target) {
                streak++;
                cursor = cursor.minusWeeks(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private static int longestWeeklyStreak(Habit habit, Set<Long> completedDays) {
        Map<Long, Integer> weekCounts = buildWeekCounts(completedDays);
        int target = Math.max(1, habit.timesPerWeekTarget);

        long minDay = Collections.min(completedDays);
        long maxDay = Collections.max(completedDays);
        LocalDate cursor = LocalDate.ofEpochDay(minDay);
        LocalDate end = LocalDate.ofEpochDay(maxDay);

        int longest = 0;
        int run = 0;
        while (!cursor.isAfter(end)) {
            long wk = weekKey(cursor);
            int count = weekCounts.getOrDefault(wk, 0);
            if (count >= target) {
                run++;
                longest = Math.max(longest, run);
            } else {
                run = 0;
            }
            cursor = cursor.plusWeeks(1);
        }
        return longest;
    }

    private static Map<Long, Integer> buildWeekCounts(Set<Long> completedDays) {
        Map<Long, Integer> map = new HashMap<>();
        for (long d : completedDays) {
            long wk = weekKey(LocalDate.ofEpochDay(d));
            map.merge(wk, 1, Integer::sum);
        }
        return map;
    }

    private static long weekKey(LocalDate date) {
        int weekYear = date.get(IsoFields.WEEK_BASED_YEAR);
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return weekYear * 100L + week;
    }
}
