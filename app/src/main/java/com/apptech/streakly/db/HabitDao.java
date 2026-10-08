package com.apptech.streakly.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;

import java.util.List;

@Dao
public interface HabitDao {

    // ---- Habits ----

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    LiveData<List<Habit>> observeActiveHabits();

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    List<Habit> getActiveHabitsSync();

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, createdAt ASC")
    List<Habit> getAllHabitsSync();

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    LiveData<Habit> observeHabit(long habitId);

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    Habit getHabitSync(long habitId);

    @Query("SELECT * FROM habits WHERE reminderEnabled = 1 AND archived = 0")
    List<Habit> getHabitsWithReminders();

    @Insert
    long insertHabit(Habit habit);

    @Update
    void updateHabit(Habit habit);

    @Delete
    void deleteHabit(Habit habit);

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    void deleteCompletionsForHabit(long habitId);

    // ---- Completions ----

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    LiveData<List<HabitCompletion>> observeCompletionsForHabit(long habitId);

    @Query("SELECT * FROM habit_completions")
    LiveData<List<HabitCompletion>> observeAllCompletions();

    @Query("SELECT * FROM habit_completions")
    List<HabitCompletion> getAllCompletionsSync();

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
    HabitCompletion getCompletionForDay(long habitId, long epochDay);

    @Insert
    long insertCompletion(HabitCompletion completion);

    @Update
    void updateCompletion(HabitCompletion completion);

    @Delete
    void deleteCompletion(HabitCompletion completion);

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND epochDay = :epochDay")
    void deleteCompletionForDay(long habitId, long epochDay);

    // ---- Backup / restore ----

    @Query("DELETE FROM habits")
    void deleteAllHabits();

    @Query("DELETE FROM habit_completions")
    void deleteAllCompletions();

    @Insert
    void insertHabits(List<Habit> habits);

    @Insert
    void insertCompletions(List<HabitCompletion> completions);

    @Transaction
    default void replaceAll(List<Habit> habits, List<HabitCompletion> completions) {
        deleteAllCompletions();
        deleteAllHabits();
        if (!habits.isEmpty()) {
            insertHabits(habits);
        }
        if (!completions.isEmpty()) {
            insertCompletions(completions);
        }
    }

    @Transaction
    default void deleteHabitWithHistory(Habit habit) {
        deleteCompletionsForHabit(habit.id);
        deleteHabit(habit);
    }
}
