package com.apptech.streakly.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.apptech.streakly.widget.TodayWidgetProvider;

import java.util.concurrent.TimeUnit;

/**
 * Reliability net for the home-screen widget: {@link TodayWidgetProvider} already refreshes
 * on every check-off and via its own AppWidgetProvider update cycle, but that cycle can be
 * delayed by the OS (Doze, battery optimization) or missed around day rollover. WorkManager's
 * periodic queue survives process death and reschedules itself, so this just nudges the
 * widget to redraw against "today" every so often even if nothing else triggered it.
 */
public class WidgetRefreshWorker extends Worker {

    private static final String UNIQUE_WORK_NAME = "widget_refresh";

    public WidgetRefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void schedulePeriodic(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WidgetRefreshWorker.class, 30, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        TodayWidgetProvider.requestUpdate(getApplicationContext());
        return Result.success();
    }
}
