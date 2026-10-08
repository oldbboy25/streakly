package com.apptech.streakly.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.apptech.streakly.model.Habit;

import java.util.Calendar;

public class ReminderScheduler {

    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_HABIT_NAME = "habit_name";

    public static void schedule(Context context, Habit habit) {
        if (!habit.reminderEnabled || habit.reminderHour < 0 || habit.reminderMinute < 0) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        long triggerAt = nextTriggerMillis(habit.reminderHour, habit.reminderMinute);
        PendingIntent pendingIntent = buildPendingIntent(context, habit);

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } catch (SecurityException ignored) {
            // Exact alarm permission revoked by the user; the habit still works without reminders.
        }
    }

    public static void cancel(Context context, long habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = buildPendingIntentForId(context, habitId, "");
        alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent buildPendingIntent(Context context, Habit habit) {
        return buildPendingIntentForId(context, habit.id, habit.name);
    }

    private static PendingIntent buildPendingIntentForId(Context context, long habitId, String name) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_HABIT_ID, habitId);
        intent.putExtra(EXTRA_HABIT_NAME, name);
        int requestCode = (int) habitId;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    private static long nextTriggerMillis(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTimeInMillis();
    }
}
