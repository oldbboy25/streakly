package com.apptech.streakly.ui.habitdetail;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.apptech.streakly.R;
import com.apptech.streakly.domain.StreakCalculator;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.addedit.AddEditHabitActivity;
import com.apptech.streakly.ui.views.MonthCalendarView;
import com.apptech.streakly.ui.views.SimpleBarChartView;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HabitDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";

    private HabitRepository repository;
    private long habitId;
    private Habit currentHabit;
    private List<HabitCompletion> latestCompletions = new ArrayList<>();

    private TextView nameText;
    private TextView emojiText;
    private TextView currentStreakText;
    private TextView longestStreakText;
    private SimpleBarChartView chart;
    private MonthCalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, -1);
        if (habitId < 0) {
            finish();
            return;
        }
        repository = HabitRepository.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.habit_detail_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                Intent intent = new Intent(this, AddEditHabitActivity.class);
                intent.putExtra(AddEditHabitActivity.EXTRA_HABIT_ID, habitId);
                startActivity(intent);
                return true;
            } else if (id == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        emojiText = findViewById(R.id.detail_emoji);
        nameText = findViewById(R.id.detail_name);
        currentStreakText = findViewById(R.id.detail_current_streak);
        longestStreakText = findViewById(R.id.detail_longest_streak);
        chart = findViewById(R.id.detail_chart);
        calendar = findViewById(R.id.detail_calendar);

        long todayEpochDay = LocalDate.now().toEpochDay();
        calendar.setToday(todayEpochDay);
        calendar.setOnDayClickListener(epochDay -> repository.toggleCompletion(habitId, epochDay));

        repository.observeHabit(habitId).observe(this, habit -> {
            currentHabit = habit;
            if (habit == null) {
                finish();
                return;
            }
            emojiText.setText(habit.emoji);
            nameText.setText(habit.name);
            refreshStreaksAndChart();
        });

        repository.observeCompletionsForHabit(habitId).observe(this, completions -> {
            latestCompletions = completions;
            refreshStreaksAndChart();
        });
    }

    private void refreshStreaksAndChart() {
        if (currentHabit == null) {
            return;
        }
        Set<Long> completedDays = new HashSet<>();
        for (HabitCompletion c : latestCompletions) {
            completedDays.add(c.epochDay);
        }
        long todayEpochDay = LocalDate.now().toEpochDay();

        int current = StreakCalculator.currentStreakDays(currentHabit, completedDays, todayEpochDay);
        int longest = StreakCalculator.longestStreakDays(currentHabit, completedDays);
        currentStreakText.setText(getString(R.string.streak_current_format, current));
        longestStreakText.setText(getString(R.string.streak_longest_format, longest));

        List<Float> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            values.add(completedDays.contains(date.toEpochDay()) ? 1f : 0f);
            labels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1));
        }
        chart.setValues(values, labels);

        calendar.setCompletedDays(completedDays);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (currentHabit != null) {
                        ReminderScheduler.cancel(this, currentHabit.id);
                        repository.deleteHabit(currentHabit, () -> {
                        });
                    }
                    finish();
                })
                .show();
    }
}
