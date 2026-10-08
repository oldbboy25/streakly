package com.apptech.streakly.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.apptech.streakly.R;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "habit_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        long habitId = intent.getLongExtra(ReminderScheduler.EXTRA_HABIT_ID, -1);
        String habitName = intent.getStringExtra(ReminderScheduler.EXTRA_HABIT_NAME);
        if (habitId < 0) {
            return;
        }

        ensureChannel(context);
        showNotification(context, habitId, habitName);

        // Alarms are one-shot; re-read the habit (in case its reminder time changed)
        // and reschedule tomorrow's alarm before letting the receiver die.
        PendingResult pendingResult = goAsync();
        HabitRepository repository = HabitRepository.getInstance(context);
        repository.getHabitSync(habitId, habit -> {
            if (habit != null) {
                ReminderScheduler.schedule(context, habit);
            }
            pendingResult.finish();
        });
    }

    private void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context, long habitId, String habitName) {
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, (int) habitId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = context.getString(R.string.notification_title_format,
                habitName == null ? "" : habitName);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_text))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        try {
            NotificationManagerCompat.from(context).notify((int) habitId, notification);
        } catch (SecurityException ignored) {
            // POST_NOTIFICATIONS was denied by the user; the habit still works, just silently.
        }
    }
}
