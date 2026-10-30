package com.apptech.streakly.ui.streaks;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.apptech.streakly.R;
import com.google.android.material.appbar.MaterialToolbar;

public class StreaksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaks);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView currentValue = findViewById(R.id.streaks_current_value);
        TextView bestValue = findViewById(R.id.streaks_best_value);

        View badge7 = findViewById(R.id.badge_7);
        View badge30 = findViewById(R.id.badge_30);
        View badge100 = findViewById(R.id.badge_100);
        View badge365 = findViewById(R.id.badge_365);

        StreaksViewModel viewModel = new ViewModelProvider(this).get(StreaksViewModel.class);
        viewModel.getSummary().observe(this, summary -> {
            currentValue.setText(getString(R.string.streaks_days_format, summary.currentStreak));
            bestValue.setText(getString(R.string.streaks_days_format, summary.bestStreak));

            setUnlocked(badge7, summary.bestStreak >= 7);
            setUnlocked(badge30, summary.bestStreak >= 30);
            setUnlocked(badge100, summary.bestStreak >= 100);
            setUnlocked(badge365, summary.bestStreak >= 365);
        });
    }

    private void setUnlocked(View badge, boolean unlocked) {
        badge.setAlpha(unlocked ? 1f : 0.35f);
    }
}
