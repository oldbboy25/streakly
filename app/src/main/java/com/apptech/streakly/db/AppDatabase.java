package com.apptech.streakly.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.TypeConverters;

import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;

@Database(entities = {Habit.class, HabitCompletion.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends androidx.room.RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract HabitDao habitDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "streakly.db")
                            .build();
                }
            }
        }
        return instance;
    }
}
