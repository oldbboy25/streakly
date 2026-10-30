package com.apptech.streakly.ui.streaks;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StreaksViewModel extends AndroidViewModel {

    public static class Summary {
        public int currentStreak;
        public int bestStreak;
        public int totalCompletions;
    }

    private final MediatorLiveData<Summary> summary = new MediatorLiveData<>();
    private List<Habit> latestHabits = new ArrayList<>();
    private List<HabitCompletion> latestCompletions = new ArrayList<>();

    public StreaksViewModel(@NonNull Application application) {
        super(application);
        HabitRepository repository = HabitRepository.getInstance(application);

        summary.addSource(repository.observeActiveHabits(), habits -> {
            latestHabits = habits;
            recompute();
        });
        summary.addSource(repository.observeAllCompletions(), completions -> {
            latestCompletions = completions;
            recompute();
        });
    }

    public LiveData<Summary> getSummary() {
        return summary;
    }

    private void recompute() {
        Summary data = new Summary();
        data.totalCompletions = latestCompletions.size();

        Map<Long, Set<Long>> completedDaysByHabit = new HashMap<>();
        for (HabitCompletion c : latestCompletions) {
            completedDaysByHabit.computeIfAbsent(c.habitId, k -> new HashSet<>()).add(c.epochDay);
        }

        long todayEpochDay = LocalDate.now().toEpochDay();
        int bestCurrent = 0;
        int bestLongest = 0;
        for (Habit habit : latestHabits) {
            Set<Long> days = completedDaysByHabit.getOrDefault(habit.id, new HashSet<>());
            bestCurrent = Math.max(bestCurrent, StreakCalculator.currentStreakDays(habit, days, todayEpochDay));
            bestLongest = Math.max(bestLongest, StreakCalculator.longestStreakDays(habit, days));
        }
        data.currentStreak = bestCurrent;
        data.bestStreak = Math.max(bestLongest, bestCurrent);

        summary.setValue(data);
    }
}
