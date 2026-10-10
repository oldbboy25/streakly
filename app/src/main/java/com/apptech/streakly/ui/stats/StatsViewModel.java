package com.apptech.streakly.ui.stats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.apptech.streakly.domain.StreakCalculator;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;
import com.apptech.streakly.repository.HabitRepository;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StatsViewModel extends AndroidViewModel {

    public enum Range { WEEK, MONTH, YEAR, ALL_TIME }

    public static class Overview {
        public int totalHabits;
        public int totalCompletions;
        public int bestStreak;
        public List<Float> chartValues = new ArrayList<>();
        public List<String> chartLabels = new ArrayList<>();
        public float distributionCompleted;
        public float distributionMissed;
        public List<HabitStatRow> habitRows = new ArrayList<>();
    }

    public static class HabitStatRow {
        public final String name;
        public final int percent;

        public HabitStatRow(String name, int percent) {
            this.name = name;
            this.percent = percent;
        }
    }

    private final MediatorLiveData<Overview> overview = new MediatorLiveData<>();
    private List<Habit> latestHabits = new ArrayList<>();
    private List<HabitCompletion> latestCompletions = new ArrayList<>();
    private Range currentRange = Range.WEEK;

    public StatsViewModel(@NonNull Application application) {
        super(application);
        HabitRepository repository = HabitRepository.getInstance(application);

        overview.addSource(repository.observeActiveHabits(), habits -> {
            latestHabits = habits;
            recompute();
        });
        overview.addSource(repository.observeAllCompletions(), completions -> {
            latestCompletions = completions;
            recompute();
        });
    }

    public LiveData<Overview> getOverview() {
        return overview;
    }

    public Range getCurrentRange() {
        return currentRange;
    }

    public void setRange(Range range) {
        if (currentRange == range) {
            return;
        }
        currentRange = range;
        recompute();
    }

    private void recompute() {
        Overview data = new Overview();
        data.totalHabits = latestHabits.size();
        data.totalCompletions = latestCompletions.size();

        Map<Long, Set<Long>> completedDaysByHabit = new HashMap<>();
        for (HabitCompletion c : latestCompletions) {
            completedDaysByHabit.computeIfAbsent(c.habitId, k -> new HashSet<>()).add(c.epochDay);
        }

        int bestStreak = 0;
        for (Habit habit : latestHabits) {
            Set<Long> days = completedDaysByHabit.getOrDefault(habit.id, new HashSet<>());
            bestStreak = Math.max(bestStreak, StreakCalculator.longestStreakDays(habit, days));
        }
        data.bestStreak = bestStreak;

        long todayEpochDay = LocalDate.now().toEpochDay();
        long rangeStart = rangeStartEpochDay(todayEpochDay);

        long totalDue = 0;
        long totalDone = 0;
        for (Habit habit : latestHabits) {
            Set<Long> days = completedDaysByHabit.getOrDefault(habit.id, new HashSet<>());
            long dueCount = 0;
            long doneCount = 0;
            for (long day = rangeStart; day <= todayEpochDay; day++) {
                LocalDate date = LocalDate.ofEpochDay(day);
                if (!habit.isScheduledOn(date.getDayOfWeek().getValue())) {
                    continue;
                }
                dueCount++;
                if (days.contains(day)) {
                    doneCount++;
                }
            }
            int percent = dueCount == 0 ? 0 : (int) Math.round(100.0 * doneCount / dueCount);
            data.habitRows.add(new HabitStatRow(habit.name, percent));
            totalDue += dueCount;
            totalDone += doneCount;
        }

        if (totalDue > 0) {
            data.distributionCompleted = (float) totalDone / totalDue;
            data.distributionMissed = 1f - data.distributionCompleted;
        }

        buildChart(data, rangeStart, todayEpochDay, completedDaysByHabit);

        overview.setValue(data);
    }

    private long rangeStartEpochDay(long todayEpochDay) {
        switch (currentRange) {
            case MONTH:
                return todayEpochDay - 29;
            case YEAR:
                return todayEpochDay - 364;
            case ALL_TIME:
                long earliest = todayEpochDay;
                for (Habit habit : latestHabits) {
                    if (habit.createdAt > 0) {
                        long createdEpochDay = habit.createdAt / (24L * 60 * 60 * 1000);
                        earliest = Math.min(earliest, createdEpochDay);
                    }
                }
                return earliest;
            case WEEK:
            default:
                return todayEpochDay - 6;
        }
    }

    private void buildChart(Overview data, long rangeStart, long todayEpochDay,
                             Map<Long, Set<Long>> completedDaysByHabit) {
        int habitCount = Math.max(1, latestHabits.size());

        if (currentRange == Range.WEEK) {
            for (long day = rangeStart; day <= todayEpochDay; day++) {
                data.chartValues.add((float) countCompletedOnDay(day, completedDaysByHabit) / habitCount);
                data.chartLabels.add(dayInitial(day));
            }
            return;
        }

        if (currentRange == Range.MONTH) {
            for (int week = 0; week < 5; week++) {
                long bucketStart = rangeStart + (week * 6L);
                if (bucketStart > todayEpochDay) {
                    break;
                }
                long bucketEnd = Math.min(todayEpochDay, bucketStart + 6);
                data.chartValues.add(averageFraction(bucketStart, bucketEnd, completedDaysByHabit, habitCount));
                data.chartLabels.add("W" + (week + 1));
            }
            return;
        }

        buildMonthlyChart(data, rangeStart, todayEpochDay, completedDaysByHabit, habitCount);
    }

    private void buildMonthlyChart(Overview data, long rangeStart, long todayEpochDay,
                                    Map<Long, Set<Long>> completedDaysByHabit, int habitCount) {
        LocalDate startMonth = LocalDate.ofEpochDay(rangeStart).withDayOfMonth(1);
        LocalDate endDate = LocalDate.ofEpochDay(todayEpochDay);

        List<LocalDate> months = new ArrayList<>();
        LocalDate cursor = startMonth;
        while (!cursor.isAfter(endDate)) {
            months.add(cursor);
            cursor = cursor.plusMonths(1);
        }

        int startIndex = Math.max(0, months.size() - 24);
        for (int i = startIndex; i < months.size(); i++) {
            LocalDate monthStart = months.get(i);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            long bucketStart = monthStart.toEpochDay();
            long bucketEnd = Math.min(todayEpochDay, monthEnd.toEpochDay());
            data.chartValues.add(averageFraction(bucketStart, bucketEnd, completedDaysByHabit, habitCount));
            data.chartLabels.add(monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1));
        }
    }

    private float averageFraction(long startDay, long endDay, Map<Long, Set<Long>> completedDaysByHabit, int habitCount) {
        if (endDay < startDay) {
            return 0f;
        }
        float sum = 0f;
        int count = 0;
        for (long day = startDay; day <= endDay; day++) {
            sum += (float) countCompletedOnDay(day, completedDaysByHabit) / habitCount;
            count++;
        }
        return count == 0 ? 0f : sum / count;
    }

    private int countCompletedOnDay(long epochDay, Map<Long, Set<Long>> completedDaysByHabit) {
        int done = 0;
        for (Set<Long> days : completedDaysByHabit.values()) {
            if (days.contains(epochDay)) {
                done++;
            }
        }
        return done;
    }

    private String dayInitial(long epochDay) {
        LocalDate date = LocalDate.ofEpochDay(epochDay);
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1);
    }
}
