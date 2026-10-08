package com.apptech.streakly.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apptech.streakly.repository.HabitRepository;

import java.time.LocalDate;

public class WidgetToggleReceiver extends BroadcastReceiver {

    public static final String EXTRA_HABIT_ID = "widget_habit_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        long habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1);
        if (habitId < 0) {
            return;
        }

        long todayEpochDay = LocalDate.now().toEpochDay();
        HabitRepository repository = HabitRepository.getInstance(context);

        PendingResult pendingResult = goAsync();
        repository.runOnBackground(() -> {
            repository.toggleCompletionBlocking(habitId, todayEpochDay);
            TodayWidgetProvider.requestUpdate(context);
            pendingResult.finish();
        });
    }
}
