package com.apptech.streakly.ui.addedit;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.apptech.streakly.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

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
        health.add(new HabitTemplate("Drink water", "💧", "#2E8BE6"));
        health.add(new HabitTemplate("Exercise", "🏃", "#FF9F43"));
        health.add(new HabitTemplate("Sleep 8 hours", "😴", "#6C4FEB"));
        health.add(new HabitTemplate("Eat a healthy meal", "🥗", "#1FAE6B"));
        map.put(R.string.template_category_health, health);

        List<HabitTemplate> mind = new ArrayList<>();
        mind.add(new HabitTemplate("Meditate", "🧠", "#1FB6B0"));
        mind.add(new HabitTemplate("Journal", "✍️", "#F060A8"));
        mind.add(new HabitTemplate("Read", "📚", "#6B6880"));
        mind.add(new HabitTemplate("Practice gratitude", "🙏", "#FF5470"));
        map.put(R.string.template_category_mind, mind);

        List<HabitTemplate> productivity = new ArrayList<>();
        productivity.add(new HabitTemplate("Plan tomorrow", "🎯", "#1FAE6B"));
        productivity.add(new HabitTemplate("No phone before bed", "📵", "#2E8BE6"));
        productivity.add(new HabitTemplate("Tidy up", "🧹", "#FF9F43"));
        productivity.add(new HabitTemplate("Wake up early", "⏰", "#6C4FEB"));
        map.put(R.string.template_category_productivity, productivity);

        List<HabitTemplate> money = new ArrayList<>();
        money.add(new HabitTemplate("Track spending", "💰", "#1FB6B0"));
        money.add(new HabitTemplate("Save money", "🌱", "#F060A8"));
        map.put(R.string.template_category_money, money);

        return map;
    }

    private void addCategory(LinearLayout container, int categoryTitleRes, List<HabitTemplate> templates) {
        TextView header = new TextView(this);
        header.setText(categoryTitleRes);
        header.setTextSize(13);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextColor(ContextCompat.getColor(this, R.color.md_on_surface_variant));
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.topMargin = dp(24);
        headerParams.leftMargin = dp(4);
        header.setLayoutParams(headerParams);
        container.addView(header);

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(10);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.md_surface));

        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        card.addView(rows);

        for (int i = 0; i < templates.size(); i++) {
            HabitTemplate template = templates.get(i);
            rows.addView(buildRow(template));
            if (i < templates.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                dividerParams.leftMargin = dp(68);
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.md_outline));
                rows.addView(divider);
            }
        }
        container.addView(card);
    }

    private View buildRow(HabitTemplate template) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue rippleValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleValue, true);
        row.setBackgroundResource(rippleValue.resourceId);
        row.setOnClickListener(v -> openCustom(template));

        FrameLayout iconFrame = new FrameLayout(this);
        int iconSize = dp(40);
        iconFrame.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
        iconFrame.setBackground(tintedCircle(template.colorHex));

        TextView emoji = new TextView(this);
        emoji.setText(template.emoji);
        emoji.setTextSize(16);
        FrameLayout.LayoutParams emojiParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.gravity = Gravity.CENTER;
        iconFrame.addView(emoji, emojiParams);
        row.addView(iconFrame);

        TextView name = new TextView(this);
        name.setText(template.name);
        name.setTextSize(15);
        name.setTextColor(ContextCompat.getColor(this, R.color.md_on_surface));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameParams.leftMargin = dp(14);
        row.addView(name, nameParams);

        ImageView chevron = new ImageView(this);
        chevron.setImageResource(R.drawable.ic_chevron_right);
        chevron.setColorFilter(ContextCompat.getColor(this, R.color.md_on_surface_variant));
        row.addView(chevron, dp(20), dp(20));

        return row;
    }

    private GradientDrawable tintedCircle(String colorHex) {
        int color;
        try {
            color = Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            color = ContextCompat.getColor(this, R.color.md_primary);
        }
        int wash = Color.argb(38, Color.red(color), Color.green(color), Color.blue(color));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(wash);
        return drawable;
    }

    private int ViewGroupLayoutParamsWrap() {
        return FrameLayout.LayoutParams.WRAP_CONTENT;
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
