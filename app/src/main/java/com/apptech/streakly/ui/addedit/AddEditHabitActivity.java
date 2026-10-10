package com.apptech.streakly.ui.addedit;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.apptech.streakly.R;
import com.apptech.streakly.model.FrequencyType;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class AddEditHabitActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_TEMPLATE_NAME = "template_name";
    public static final String EXTRA_TEMPLATE_EMOJI = "template_emoji";
    public static final String EXTRA_TEMPLATE_COLOR = "template_color";

    private static final String[] EMOJIS = {
            "💪", "🏃", "📚", "💧", "🧘",
            "🥗", "😴", "✍️", "🎯", "🧹",
            "💰", "🎨", "🎵", "☀️", "🧠",
            "❤️", "🚴", "🛐", "🙏", "🌱",
            "⏰", "🧼", "🍽️", "📵"
    };

    private static final String[] COLOR_HEXES = {
            "#13874F", "#1E6FA8", "#7B4FB0", "#D9731F",
            "#C23B30", "#1A8F86", "#C44E82", "#53615A"
    };

    private HabitRepository repository;
    private long habitId = -1;
    private Habit editingHabit;

    private TextInputEditText nameInput;
    private LinearLayout emojiContainer;
    private LinearLayout colorContainer;
    private ChipGroup daysChipGroup;
    private View timesPerWeekRow;
    private TextView targetCountText;
    private MaterialSwitch reminderSwitch;
    private MaterialButton reminderTimeButton;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;

    private String selectedEmoji = EMOJIS[0];
    private String selectedColorHex = COLOR_HEXES[0];
    private int timesPerWeekTarget = 3;
    private int reminderHour = -1;
    private int reminderMinute = -1;

    private View selectedEmojiView;
    private View selectedColorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);

        repository = HabitRepository.getInstance(this);
        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, -1);

        bindViews();
        buildEmojiPicker();
        buildColorPicker();
        setupFrequencyUi();
        setupReminderUi();

        saveButton.setOnClickListener(v -> onSaveClicked());

        if (habitId >= 0) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.edit_habit_title);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> onDeleteClicked());
            loadExistingHabit();
        } else {
            applyTemplateIfPresent();
        }
    }

    private void applyTemplateIfPresent() {
        String templateName = getIntent().getStringExtra(EXTRA_TEMPLATE_NAME);
        if (templateName == null) {
            return;
        }
        nameInput.setText(templateName);

        String templateEmoji = getIntent().getStringExtra(EXTRA_TEMPLATE_EMOJI);
        if (templateEmoji != null) {
            for (int i = 0; i < EMOJIS.length; i++) {
                if (EMOJIS[i].equals(templateEmoji)) {
                    selectEmoji(templateEmoji, emojiContainer.getChildAt(i));
                    break;
                }
            }
        }

        String templateColor = getIntent().getStringExtra(EXTRA_TEMPLATE_COLOR);
        if (templateColor != null) {
            for (int i = 0; i < COLOR_HEXES.length; i++) {
                if (COLOR_HEXES[i].equalsIgnoreCase(templateColor)) {
                    selectColor(templateColor, colorContainer.getChildAt(i));
                    break;
                }
            }
        }
    }

    private void bindViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        nameInput = findViewById(R.id.name_input);
        emojiContainer = findViewById(R.id.emoji_container);
        colorContainer = findViewById(R.id.color_container);
        daysChipGroup = findViewById(R.id.days_chip_group);
        timesPerWeekRow = findViewById(R.id.times_per_week_row);
        targetCountText = findViewById(R.id.target_count_text);
        reminderSwitch = findViewById(R.id.reminder_switch);
        reminderTimeButton = findViewById(R.id.reminder_time_button);
        saveButton = findViewById(R.id.btn_save);
        deleteButton = findViewById(R.id.btn_delete);

        findViewById(R.id.btn_target_minus).setOnClickListener(v -> {
            timesPerWeekTarget = Math.max(1, timesPerWeekTarget - 1);
            targetCountText.setText(String.valueOf(timesPerWeekTarget));
        });
        findViewById(R.id.btn_target_plus).setOnClickListener(v -> {
            timesPerWeekTarget = Math.min(7, timesPerWeekTarget + 1);
            targetCountText.setText(String.valueOf(timesPerWeekTarget));
        });

        ChipGroup frequencyChipGroup = findViewById(R.id.frequency_chip_group);
        frequencyChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> updateFrequencyVisibility());
    }

    private void buildEmojiPicker() {
        for (String emoji : EMOJIS) {
            FrameLayout frame = new FrameLayout(this);
            int size = dp(48);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(dp(10));
            frame.setLayoutParams(params);
            frame.setBackgroundResource(R.drawable.bg_emoji_circle);

            TextView text = new TextView(this);
            text.setText(emoji);
            text.setTextSize(15);
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.gravity = Gravity.CENTER;
            frame.addView(text, textParams);

            frame.setOnClickListener(v -> selectEmoji(emoji, frame));
            emojiContainer.addView(frame);

            if (emoji.equals(selectedEmoji)) {
                selectEmoji(emoji, frame);
            }
        }
    }

    private void selectEmoji(String emoji, View view) {
        selectedEmoji = emoji;
        if (selectedEmojiView != null) {
            selectedEmojiView.setScaleX(1f);
            selectedEmojiView.setScaleY(1f);
        }
        view.setScaleX(1.15f);
        view.setScaleY(1.15f);
        selectedEmojiView = view;
    }

    private void buildColorPicker() {
        for (String hex : COLOR_HEXES) {
            View swatch = new View(this);
            int size = dp(36);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(dp(10));
            swatch.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(hex));
            swatch.setBackground(drawable);

            swatch.setOnClickListener(v -> selectColor(hex, swatch));
            colorContainer.addView(swatch);

            if (hex.equals(selectedColorHex)) {
                selectColor(hex, swatch);
            }
        }
    }

    private void selectColor(String hex, View view) {
        selectedColorHex = hex;
        if (selectedColorView != null) {
            selectedColorView.setScaleX(1f);
            selectedColorView.setScaleY(1f);
        }
        view.setScaleX(1.2f);
        view.setScaleY(1.2f);
        selectedColorView = view;
    }

    private void setupFrequencyUi() {
        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String label : dayLabels) {
            Chip chip = new Chip(this);
            chip.setText(label);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            daysChipGroup.addView(chip);
        }

        targetCountText.setText(String.valueOf(timesPerWeekTarget));
        updateFrequencyVisibility();
    }

    private void updateFrequencyVisibility() {
        boolean specificDays = ((Chip) findViewById(R.id.chip_specific_days)).isChecked();
        boolean timesPerWeek = ((Chip) findViewById(R.id.chip_times_per_week)).isChecked();
        daysChipGroup.setVisibility(specificDays ? View.VISIBLE : View.GONE);
        timesPerWeekRow.setVisibility(timesPerWeek ? View.VISIBLE : View.GONE);
    }

    private void setupReminderUi() {
        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminderTimeButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                if (reminderHour < 0) {
                    showTimePicker();
                }
                if (!ReminderScheduler.canScheduleExactAlarms(this)) {
                    promptForExactAlarmPermission();
                }
            }
        });
        reminderTimeButton.setOnClickListener(v -> showTimePicker());
    }

    private void promptForExactAlarmPermission() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exact_alarm_dialog_title)
                .setMessage(R.string.exact_alarm_dialog_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.exact_alarm_dialog_action, (dialog, which) ->
                        ReminderScheduler.openExactAlarmSettings(this))
                .show();
    }

    private void showTimePicker() {
        int initialHour = reminderHour >= 0 ? reminderHour : 9;
        int initialMinute = reminderMinute >= 0 ? reminderMinute : 0;
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            reminderHour = hourOfDay;
            reminderMinute = minute;
            reminderTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, initialHour, initialMinute, true).show();
    }

    private void loadExistingHabit() {
        repository.getHabitSync(habitId, habit -> runOnUiThread(() -> {
            if (habit == null) {
                return;
            }
            editingHabit = habit;
            populateFromHabit(habit);
        }));
    }

    private void populateFromHabit(Habit habit) {
        nameInput.setText(habit.name);

        for (int i = 0; i < EMOJIS.length; i++) {
            if (EMOJIS[i].equals(habit.emoji)) {
                selectEmoji(habit.emoji, emojiContainer.getChildAt(i));
                break;
            }
        }
        for (int i = 0; i < COLOR_HEXES.length; i++) {
            if (COLOR_HEXES[i].equalsIgnoreCase(habit.colorHex)) {
                selectColor(habit.colorHex, colorContainer.getChildAt(i));
                break;
            }
        }

        if (habit.frequencyType == FrequencyType.SPECIFIC_DAYS) {
            ((Chip) findViewById(R.id.chip_specific_days)).setChecked(true);
            for (int i = 0; i < 7; i++) {
                boolean on = (habit.specificDaysMask & (1 << i)) != 0;
                ((Chip) daysChipGroup.getChildAt(i)).setChecked(on);
            }
        } else if (habit.frequencyType == FrequencyType.TIMES_PER_WEEK) {
            ((Chip) findViewById(R.id.chip_times_per_week)).setChecked(true);
            timesPerWeekTarget = Math.max(1, habit.timesPerWeekTarget);
            targetCountText.setText(String.valueOf(timesPerWeekTarget));
        } else {
            ((Chip) findViewById(R.id.chip_daily)).setChecked(true);
        }
        updateFrequencyVisibility();

        if (habit.reminderEnabled && habit.reminderHour >= 0) {
            reminderSwitch.setChecked(true);
            reminderHour = habit.reminderHour;
            reminderMinute = habit.reminderMinute;
            reminderTimeButton.setVisibility(View.VISIBLE);
            reminderTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute));
        }
    }

    private void onSaveClicked() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.error_name_required));
            return;
        }

        FrequencyType frequencyType;
        int specificDaysMask = 0;
        if (((Chip) findViewById(R.id.chip_specific_days)).isChecked()) {
            frequencyType = FrequencyType.SPECIFIC_DAYS;
            for (int i = 0; i < 7; i++) {
                if (((Chip) daysChipGroup.getChildAt(i)).isChecked()) {
                    specificDaysMask |= (1 << i);
                }
            }
        } else if (((Chip) findViewById(R.id.chip_times_per_week)).isChecked()) {
            frequencyType = FrequencyType.TIMES_PER_WEEK;
        } else {
            frequencyType = FrequencyType.DAILY;
        }

        boolean reminderEnabled = reminderSwitch.isChecked() && reminderHour >= 0;

        Habit habit = editingHabit != null ? editingHabit : new Habit();
        habit.name = name;
        habit.emoji = selectedEmoji;
        habit.colorHex = selectedColorHex;
        habit.frequencyType = frequencyType;
        habit.specificDaysMask = specificDaysMask;
        habit.timesPerWeekTarget = timesPerWeekTarget;
        habit.reminderEnabled = reminderEnabled;
        habit.reminderHour = reminderEnabled ? reminderHour : -1;
        habit.reminderMinute = reminderEnabled ? reminderMinute : -1;

        if (editingHabit != null) {
            repository.updateHabit(habit, () -> runOnUiThread(() -> {
                applyReminder(habit);
                finish();
            }));
        } else {
            habit.createdAt = System.currentTimeMillis();
            repository.insertHabit(habit, newId -> runOnUiThread(() -> {
                habit.id = newId;
                applyReminder(habit);
                finish();
            }));
        }
    }

    private void applyReminder(Habit habit) {
        if (habit.reminderEnabled) {
            ReminderScheduler.schedule(this, habit);
        } else {
            ReminderScheduler.cancel(this, habit.id);
        }
    }

    private void onDeleteClicked() {
        if (editingHabit == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    ReminderScheduler.cancel(this, editingHabit.id);
                    repository.deleteHabit(editingHabit, () -> runOnUiThread(this::finish));
                })
                .show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
