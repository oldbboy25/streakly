package com.apptech.streakly.ui.today;

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

public class TodayViewModel extends AndroidViewModel {

    private final HabitRepository repository;
    private final MediatorLiveData<List<HabitUiItem>> uiItems = new MediatorLiveData<>();

    private List<Habit> latestHabits = new ArrayList<>();
    private List<HabitCompletion> latestCompletions = new ArrayList<>();

    public TodayViewModel(@NonNull Application application) {
        super(application);
        repository = HabitRepository.getInstance(application);

        uiItems.addSource(repository.observeActiveHabits(), habits -> {
            latestHabits = habits;
            recompute();
        });
        uiItems.addSource(repository.observeAllCompletions(), completions -> {
            latestCompletions = completions;
            recompute();
        });
    }

    public LiveData<List<HabitUiItem>> getUiItems() {
        return uiItems;
    }

    private void recompute() {
        long todayEpochDay = LocalDate.now().toEpochDay();

        Map<Long, Set<Long>> completedDaysByHabit = new HashMap<>();
        for (HabitCompletion completion : latestCompletions) {
            completedDaysByHabit
                    .computeIfAbsent(completion.habitId, k -> new HashSet<>())
                    .add(completion.epochDay);
        }

        List<HabitUiItem> items = new ArrayList<>();
        for (Habit habit : latestHabits) {
            Set<Long> days = completedDaysByHabit.getOrDefault(habit.id, new HashSet<>());
            boolean completedToday = days.contains(todayEpochDay);
            int streak = StreakCalculator.currentStreakDays(habit, days, todayEpochDay);
            items.add(new HabitUiItem(habit, completedToday, streak));
        }
        uiItems.setValue(items);
    }

    public void toggleHabit(long habitId) {
        long todayEpochDay = LocalDate.now().toEpochDay();
        repository.toggleCompletion(habitId, todayEpochDay);
    }
}
