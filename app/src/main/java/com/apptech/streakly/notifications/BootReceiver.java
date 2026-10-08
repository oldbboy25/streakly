package com.apptech.streakly.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apptech.streakly.model.Habit;
import com.apptech.streakly.repository.HabitRepository;

import java.util.List;

/** Alarms don't survive a reboot, so every habit with a reminder gets re-scheduled here. */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        PendingResult pendingResult = goAsync();
        HabitRepository repository = HabitRepository.getInstance(context);
        repository.getHabitsWithReminders(habits -> {
            for (Habit habit : habits) {
                ReminderScheduler.schedule(context, habit);
            }
            pendingResult.finish();
        });
    }
}
