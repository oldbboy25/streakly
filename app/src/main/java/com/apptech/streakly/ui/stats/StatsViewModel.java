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

    public static class Overview {
        public int totalHabits;
        public int totalCompletions;
        public int bestStreak;
        public List<Float> last7Days = new ArrayList<>();
        public List<String> last7DayLabels = new ArrayList<>();
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

            long daysSinceCreated = habit.createdAt > 0
                    ? Math.max(1, (System.currentTimeMillis() - habit.createdAt) / (24L * 60 * 60 * 1000) + 1)
                    : 1;
            int percent = (int) Math.round(100.0 * days.size() / daysSinceCreated);
            percent = Math.max(0, Math.min(100, percent));
            data.habitRows.add(new HabitStatRow(habit.name, percent));
        }
        data.bestStreak = bestStreak;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long epochDay = date.toEpochDay();
            int habitsDoneThatDay = 0;
            for (Set<Long> days : completedDaysByHabit.values()) {
                if (days.contains(epochDay)) {
                    habitsDoneThatDay++;
                }
            }
            float fraction = latestHabits.isEmpty() ? 0f : (float) habitsDoneThatDay / latestHabits.size();
            data.last7Days.add(fraction);
            data.last7DayLabels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1));
        }

        overview.setValue(data);
    }
}
