package com.apptech.streakly.ui.achievements;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.apptech.streakly.R;
import com.apptech.streakly.domain.StreakCalculator;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;
import com.apptech.streakly.repository.HabitRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AchievementsViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<Achievement>> achievements = new MediatorLiveData<>();
    private List<Habit> latestHabits = new ArrayList<>();
    private List<HabitCompletion> latestCompletions = new ArrayList<>();

    public AchievementsViewModel(@NonNull Application application) {
        super(application);
        HabitRepository repository = HabitRepository.getInstance(application);

        achievements.addSource(repository.observeActiveHabits(), habits -> {
            latestHabits = habits;
            recompute();
        });
        achievements.addSource(repository.observeAllCompletions(), completions -> {
            latestCompletions = completions;
            recompute();
        });
    }

    public LiveData<List<Achievement>> getAchievements() {
        return achievements;
    }

    private void recompute() {
        Map<Long, Set<Long>> completedDaysByHabit = new HashMap<>();
        for (HabitCompletion c : latestCompletions) {
            completedDaysByHabit.computeIfAbsent(c.habitId, k -> new HashSet<>()).add(c.epochDay);
        }

        int bestStreak = 0;
        for (Habit habit : latestHabits) {
            Set<Long> days = completedDaysByHabit.getOrDefault(habit.id, new HashSet<>());
            bestStreak = Math.max(bestStreak, StreakCalculator.longestStreakDays(habit, days));
        }

        boolean hadPerfectDay = false;
        if (!latestHabits.isEmpty()) {
            Map<Long, Integer> habitsDoneOnDay = new HashMap<>();
            for (Set<Long> days : completedDaysByHabit.values()) {
                for (Long day : days) {
                    habitsDoneOnDay.merge(day, 1, Integer::sum);
                }
            }
            for (int count : habitsDoneOnDay.values()) {
                if (count >= latestHabits.size()) {
                    hadPerfectDay = true;
                    break;
                }
            }
        }

        int totalHabits = latestHabits.size();
        int totalCompletions = latestCompletions.size();

        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("🌱", R.string.achievement_first_habit_title,
                R.string.achievement_first_habit_desc, totalHabits >= 1));
        list.add(new Achievement("✅", R.string.achievement_first_checkin_title,
                R.string.achievement_first_checkin_desc, totalCompletions >= 1));
        list.add(new Achievement("🔥", R.string.achievement_streak_7_title,
                R.string.achievement_streak_7_desc, bestStreak >= 7));
        list.add(new Achievement("🏆", R.string.achievement_streak_30_title,
                R.string.achievement_streak_30_desc, bestStreak >= 30));
        list.add(new Achievement("👑", R.string.achievement_streak_100_title,
                R.string.achievement_streak_100_desc, bestStreak >= 100));
        list.add(new Achievement("📚", R.string.achievement_collector_title,
                R.string.achievement_collector_desc, totalHabits >= 5));
        list.add(new Achievement("🌟", R.string.achievement_perfect_day_title,
                R.string.achievement_perfect_day_desc, hadPerfectDay));
        list.add(new Achievement("💪", R.string.achievement_consistent_title,
                R.string.achievement_consistent_desc, totalCompletions >= 50));
        list.add(new Achievement("⚡", R.string.achievement_dedicated_title,
                R.string.achievement_dedicated_desc, totalCompletions >= 200));

        achievements.setValue(list);
    }
}
