package com.apptech.streakly.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

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

public class CalendarViewModel extends AndroidViewModel {

    public static class DayHabitItem {
        public final Habit habit;
        public final boolean completedOnDay;

        public DayHabitItem(Habit habit, boolean completedOnDay) {
            this.habit = habit;
            this.completedOnDay = completedOnDay;
        }
    }

    public static class State {
        public Set<Long> markedDays = new HashSet<>();
        public List<DayHabitItem> dayItems = new ArrayList<>();
        public int doneCount;
        public int totalCount;
    }

    private final HabitRepository repository;
    private final MediatorLiveData<State> state = new MediatorLiveData<>();

    private List<Habit> latestHabits = new ArrayList<>();
    private List<HabitCompletion> latestCompletions = new ArrayList<>();
    private long selectedEpochDay = LocalDate.now().toEpochDay();

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = HabitRepository.getInstance(application);

        state.addSource(repository.observeActiveHabits(), habits -> {
            latestHabits = habits;
            recompute();
        });
        state.addSource(repository.observeAllCompletions(), completions -> {
            latestCompletions = completions;
            recompute();
        });
    }

    public LiveData<State> getState() {
        return state;
    }

    public void selectDay(long epochDay) {
        if (selectedEpochDay == epochDay) {
            return;
        }
        selectedEpochDay = epochDay;
        recompute();
    }

    public void toggleHabitOnSelectedDay(long habitId) {
        repository.toggleCompletion(habitId, selectedEpochDay);
    }

    private void recompute() {
        State data = new State();

        Map<Long, Set<Long>> completedDaysByHabit = new HashMap<>();
        for (HabitCompletion c : latestCompletions) {
            completedDaysByHabit.computeIfAbsent(c.habitId, k -> new HashSet<>()).add(c.epochDay);
            data.markedDays.add(c.epochDay);
        }

        LocalDate selectedDate = LocalDate.ofEpochDay(selectedEpochDay);
        int isoDay = selectedDate.getDayOfWeek().getValue();

        for (Habit habit : latestHabits) {
            if (!habit.isScheduledOn(isoDay)) {
                continue;
            }
            Set<Long> days = completedDaysByHabit.getOrDefault(habit.id, new HashSet<>());
            boolean completed = days.contains(selectedEpochDay);
            data.dayItems.add(new DayHabitItem(habit, completed));
            data.totalCount++;
            if (completed) {
                data.doneCount++;
            }
        }

        state.setValue(data);
    }
}
