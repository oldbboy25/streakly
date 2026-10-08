package com.apptech.streakly.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.apptech.streakly.R;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.MainActivity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TodayWidgetProvider extends AppWidgetProvider {

    private static final int MAX_ROWS = 4;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        refreshAll(context, appWidgetManager, appWidgetIds);
    }

    /** Called by WidgetToggleReceiver after a check-off so the widget reflects it immediately. */
    public static void requestUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TodayWidgetProvider.class));
        if (ids.length > 0) {
            refreshAll(context, manager, ids);
        }
    }

    private static void refreshAll(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            return;
        }
        HabitRepository repository = HabitRepository.getInstance(context);
        long todayEpochDay = LocalDate.now().toEpochDay();

        repository.getActiveHabitsSyncList(habits -> {
            // This callback already runs on the repository's background executor.
            List<Habit> remaining = new ArrayList<>();
            for (Habit habit : habits) {
                if (!repository.isCompletedSync(habit.id, todayEpochDay)) {
                    remaining.add(habit);
                    if (remaining.size() >= MAX_ROWS) {
                        break;
                    }
                }
            }
            RemoteViews views = buildViews(context, remaining, habits.isEmpty());
            for (int widgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        });
    }

    private static RemoteViews buildViews(Context context, List<Habit> rows, boolean noHabitsAtAll) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPending = PendingIntent.getActivity(context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_title, openAppPending);

        views.removeAllViews(R.id.widget_rows_container);

        if (rows.isEmpty()) {
            RemoteViews emptyRow = new RemoteViews(context.getPackageName(), R.layout.widget_today_row);
            emptyRow.setTextViewText(R.id.widget_row_name, noHabitsAtAll
                    ? context.getString(R.string.today_empty_title)
                    : "All done for today ✨");
            emptyRow.setViewVisibility(R.id.widget_row_check, View.GONE);
            views.addView(R.id.widget_rows_container, emptyRow);
            return views;
        }

        for (Habit habit : rows) {
            RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_today_row);
            String label = (habit.emoji == null ? "" : habit.emoji + "  ") + habit.name;
            row.setTextViewText(R.id.widget_row_name, label);

            Intent toggleIntent = new Intent(context, WidgetToggleReceiver.class);
            toggleIntent.putExtra(WidgetToggleReceiver.EXTRA_HABIT_ID, habit.id);
            PendingIntent togglePending = PendingIntent.getBroadcast(
                    context, (int) habit.id, toggleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            row.setOnClickPendingIntent(R.id.widget_row_check, togglePending);

            views.addView(R.id.widget_rows_container, row);
        }
        return views;
    }
}
