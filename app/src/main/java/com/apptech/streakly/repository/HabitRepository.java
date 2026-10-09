package com.apptech.streakly.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.apptech.streakly.db.AppDatabase;
import com.apptech.streakly.db.HabitDao;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;

import java.util.List;

public class HabitRepository {

    private static volatile HabitRepository instance;

    private final HabitDao dao;

    private HabitRepository(Context context) {
        dao = AppDatabase.getInstance(context).habitDao();
    }

    public static HabitRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (HabitRepository.class) {
                if (instance == null) {
                    instance = new HabitRepository(context);
                }
            }
        }
        return instance;
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    // ---- Reads (LiveData, main-thread safe) ----

    public LiveData<List<Habit>> observeActiveHabits() {
        return dao.observeActiveHabits();
    }

    public LiveData<Habit> observeHabit(long habitId) {
        return dao.observeHabit(habitId);
    }

    public LiveData<List<HabitCompletion>> observeCompletionsForHabit(long habitId) {
        return dao.observeCompletionsForHabit(habitId);
    }

    public LiveData<List<HabitCompletion>> observeAllCompletions() {
        return dao.observeAllCompletions();
    }

    // ---- Writes (background thread) ----

    public void insertHabit(Habit habit, Callback<Long> onInserted) {
        AppExecutors.diskIO().execute(() -> {
            long id = dao.insertHabit(habit);
            if (onInserted != null) {
                onInserted.onResult(id);
            }
        });
    }

    public void updateHabit(Habit habit, Runnable onDone) {
        AppExecutors.diskIO().execute(() -> {
            dao.updateHabit(habit);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void deleteHabit(Habit habit, Runnable onDone) {
        AppExecutors.diskIO().execute(() -> {
            dao.deleteHabitWithHistory(habit);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void toggleCompletion(long habitId, long epochDay) {
        AppExecutors.diskIO().execute(() -> {
            HabitCompletion existing = dao.getCompletionForDay(habitId, epochDay);
            if (existing != null) {
                dao.deleteCompletion(existing);
            } else {
                dao.insertCompletion(new HabitCompletion(habitId, epochDay, 1));
            }
        });
    }

    public void setCompletionState(long habitId, long epochDay, boolean completed) {
        AppExecutors.diskIO().execute(() -> {
            HabitCompletion existing = dao.getCompletionForDay(habitId, epochDay);
            if (completed && existing == null) {
                dao.insertCompletion(new HabitCompletion(habitId, epochDay, 1));
            } else if (!completed && existing != null) {
                dao.deleteCompletion(existing);
            }
        });
    }

    // ---- Synchronous helpers for background callers (widgets, boot receiver, backup) ----

    public void getHabitSync(long habitId, Callback<Habit> callback) {
        AppExecutors.diskIO().execute(() -> callback.onResult(dao.getHabitSync(habitId)));
    }

    public void getActiveHabitsSyncList(Callback<List<Habit>> callback) {
        AppExecutors.diskIO().execute(() -> callback.onResult(dao.getActiveHabitsSync()));
    }

    public void getHabitsWithReminders(Callback<List<Habit>> callback) {
        AppExecutors.diskIO().execute(() -> callback.onResult(dao.getHabitsWithReminders()));
    }

    public void getAllHabitsForBackup(Callback<List<Habit>> callback) {
        AppExecutors.diskIO().execute(() -> callback.onResult(dao.getAllHabitsSync()));
    }

    public void getAllCompletionsForBackup(Callback<List<HabitCompletion>> callback) {
        AppExecutors.diskIO().execute(() -> callback.onResult(dao.getAllCompletionsSync()));
    }

    public void replaceAllData(List<Habit> habits, List<HabitCompletion> completions, Runnable onDone) {
        AppExecutors.diskIO().execute(() -> {
            dao.replaceAll(habits, completions);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void runOnBackground(Runnable task) {
        AppExecutors.diskIO().execute(task);
    }

    /**
     * Synchronous variants for callers that are already running on the repository's
     * background executor (widget receivers) and need operations to happen strictly
     * in order rather than queued as a separate async task.
     */
    public void toggleCompletionBlocking(long habitId, long epochDay) {
        HabitCompletion existing = dao.getCompletionForDay(habitId, epochDay);
        if (existing != null) {
            dao.deleteCompletion(existing);
        } else {
            dao.insertCompletion(new HabitCompletion(habitId, epochDay, 1));
        }
    }

    public boolean isCompletedSync(long habitId, long epochDay) {
        return dao.getCompletionForDay(habitId, epochDay) != null;
    }

    public void updateSortOrders(List<Habit> habitsInNewOrder) {
        for (int i = 0; i < habitsInNewOrder.size(); i++) {
            habitsInNewOrder.get(i).sortOrder = i;
        }
        AppExecutors.diskIO().execute(() -> dao.updateSortOrders(habitsInNewOrder));
    }
}
