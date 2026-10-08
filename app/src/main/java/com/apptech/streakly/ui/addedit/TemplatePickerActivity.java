package com.apptech.streakly.ui.addedit;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.apptech.streakly.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplatePickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_picker);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_start_scratch).setOnClickListener(v -> openCustom(null));

        LinearLayout container = findViewById(R.id.categories_container);
        for (Map.Entry<Integer, List<HabitTemplate>> entry : buildCategories().entrySet()) {
            addCategory(container, entry.getKey(), entry.getValue());
        }
    }

    private Map<Integer, List<HabitTemplate>> buildCategories() {
        Map<Integer, List<HabitTemplate>> map = new LinkedHashMap<>();

        List<HabitTemplate> health = new ArrayList<>();
        health.add(new HabitTemplate("Drink water", "💧", "#3D6B8A"));
        health.add(new HabitTemplate("Exercise", "🏃", "#C9742F"));
        health.add(new HabitTemplate("Sleep 8 hours", "😴", "#7A5C9E"));
        health.add(new HabitTemplate("Eat a healthy meal", "🥗", "#4F6F52"));
        map.put(R.string.template_category_health, health);

        List<HabitTemplate> mind = new ArrayList<>();
        mind.add(new HabitTemplate("Meditate", "🧠", "#2E8C84"));
        mind.add(new HabitTemplate("Journal", "✍️", "#B14E7A"));
        mind.add(new HabitTemplate("Read", "📚", "#5B5F56"));
        mind.add(new HabitTemplate("Practice gratitude", "🙏", "#B0453E"));
        map.put(R.string.template_category_mind, mind);

        List<HabitTemplate> productivity = new ArrayList<>();
        productivity.add(new HabitTemplate("Plan tomorrow", "🎯", "#4F6F52"));
        productivity.add(new HabitTemplate("No phone before bed", "📵", "#3D6B8A"));
        productivity.add(new HabitTemplate("Tidy up", "🧹", "#C9742F"));
        productivity.add(new HabitTemplate("Wake up early", "⏰", "#7A5C9E"));
        map.put(R.string.template_category_productivity, productivity);

        List<HabitTemplate> money = new ArrayList<>();
        money.add(new HabitTemplate("Track spending", "💰", "#2E8C84"));
        money.add(new HabitTemplate("Save money", "🌱", "#B14E7A"));
        map.put(R.string.template_category_money, money);

        return map;
    }

    private void addCategory(LinearLayout container, int categoryTitleRes, List<HabitTemplate> templates) {
        TextView header = new TextView(this);
        header.setText(categoryTitleRes);
        header.setTextSize(14);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextColor(ContextCompat.getColor(this, R.color.md_on_surface));
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.topMargin = dp(16);
        header.setLayoutParams(headerParams);
        container.addView(header);

        ChipGroup chipGroup = new ChipGroup(this);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        groupParams.topMargin = dp(8);
        chipGroup.setLayoutParams(groupParams);

        for (HabitTemplate template : templates) {
            Chip chip = new Chip(this);
            chip.setText(template.emoji + "  " + template.name);
            chip.setOnClickListener(v -> openCustom(template));
            chipGroup.addView(chip);
        }
        container.addView(chipGroup);
    }

    private void openCustom(HabitTemplate template) {
        Intent intent = new Intent(this, AddEditHabitActivity.class);
        if (template != null) {
            intent.putExtra(AddEditHabitActivity.EXTRA_TEMPLATE_NAME, template.name);
            intent.putExtra(AddEditHabitActivity.EXTRA_TEMPLATE_EMOJI, template.emoji);
            intent.putExtra(AddEditHabitActivity.EXTRA_TEMPLATE_COLOR, template.colorHex);
        }
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
