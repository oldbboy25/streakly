package com.apptech.streakly.ui.habitdetail;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.apptech.streakly.R;
import com.apptech.streakly.domain.StreakCalculator;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.model.HabitCompletion;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.addedit.AddEditHabitActivity;
import com.apptech.streakly.ui.views.CircularProgressView;
import com.apptech.streakly.ui.views.MonthCalendarView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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
    private static final int CHART_DAYS = 30;

    private HabitRepository repository;
    private long habitId;
    private Habit currentHabit;
    private List<HabitCompletion> latestCompletions = new ArrayList<>();

    private FrameLayout iconFrame;
    private TextView nameText;
    private TextView emojiText;
    private TextView currentStreakText;
    private TextView longestStreakText;
    private CircularProgressView progressRing;
    private BarChart chart;
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

        iconFrame = findViewById(R.id.detail_icon_frame);
        emojiText = findViewById(R.id.detail_emoji);
        nameText = findViewById(R.id.detail_name);
        currentStreakText = findViewById(R.id.detail_current_streak);
        longestStreakText = findViewById(R.id.detail_longest_streak);
        progressRing = findViewById(R.id.detail_progress_ring);
        chart = findViewById(R.id.detail_bar_chart);
        calendar = findViewById(R.id.detail_calendar);

        setUpChart();

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
            tintIconFrame(habit.colorHex);
            refreshStreaksAndChart();
        });

        repository.observeCompletionsForHabit(habitId).observe(this, completions -> {
            latestCompletions = completions;
            refreshStreaksAndChart();
        });
    }

    private void setUpChart() {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.md_on_surface_variant));
        xAxis.setTextSize(9f);
        xAxis.setLabelCount(6);
    }

    private void tintIconFrame(String colorHex) {
        int color;
        try {
            color = colorHex == null ? Color.TRANSPARENT : Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            color = Color.TRANSPARENT;
        }
        if (color == Color.TRANSPARENT) {
            iconFrame.setBackgroundResource(R.drawable.bg_emoji_circle);
            return;
        }
        int wash = Color.argb(48, Color.red(color), Color.green(color), Color.blue(color));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(wash);
        iconFrame.setBackground(drawable);
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

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int due = 0;
        int done = 0;
        for (int i = CHART_DAYS - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long epochDay = date.toEpochDay();
            boolean scheduled = currentHabit.isScheduledOn(date.getDayOfWeek().getValue());
            boolean completed = completedDays.contains(epochDay);
            int index = CHART_DAYS - 1 - i;
            entries.add(new BarEntry(index, completed ? 1f : 0f));
            labels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1));
            if (scheduled) {
                due++;
                if (completed) {
                    done++;
                }
            }
        }

        int barColor = ContextCompat.getColor(this, R.color.chart_bar);
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(barColor);
        dataSet.setDrawValues(false);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.animateY(500);
        chart.invalidate();

        float fraction = due == 0 ? 0f : (float) done / due;
        progressRing.setProgress(fraction);

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
