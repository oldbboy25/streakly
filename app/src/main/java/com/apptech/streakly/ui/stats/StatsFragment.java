package com.apptech.streakly.ui.stats;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;
import com.apptech.streakly.ui.views.DonutChartView;
import com.apptech.streakly.ui.views.SimpleBarChartView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StatsViewModel viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        TextView totalHabitsText = view.findViewById(R.id.stats_total_habits);
        TextView totalCompletionsText = view.findViewById(R.id.stats_total_completions);
        TextView bestStreakText = view.findViewById(R.id.stats_best_streak);
        SimpleBarChartView chart = view.findViewById(R.id.stats_chart);
        DonutChartView donut = view.findViewById(R.id.stats_donut);
        TextView legendCompleted = view.findViewById(R.id.legend_text_completed);
        TextView legendMissed = view.findViewById(R.id.legend_text_missed);
        RecyclerView recyclerView = view.findViewById(R.id.stats_habit_recycler);
        ChipGroup rangeChipGroup = view.findViewById(R.id.stats_range_chip_group);

        int completedColor = ContextCompat.getColor(requireContext(), R.color.swatch_green);
        int missedColor = ContextCompat.getColor(requireContext(), R.color.md_primary);
        view.findViewById(R.id.legend_dot_completed).setBackgroundTintList(ColorStateList.valueOf(completedColor));
        view.findViewById(R.id.legend_dot_missed).setBackgroundTintList(ColorStateList.valueOf(missedColor));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        HabitStatAdapter adapter = new HabitStatAdapter();
        recyclerView.setAdapter(adapter);

        rangeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chip_range_week) {
                viewModel.setRange(StatsViewModel.Range.WEEK);
            } else if (id == R.id.chip_range_month) {
                viewModel.setRange(StatsViewModel.Range.MONTH);
            } else if (id == R.id.chip_range_year) {
                viewModel.setRange(StatsViewModel.Range.YEAR);
            } else if (id == R.id.chip_range_all_time) {
                viewModel.setRange(StatsViewModel.Range.ALL_TIME);
            }
        });

        viewModel.getOverview().observe(getViewLifecycleOwner(), overview -> {
            totalHabitsText.setText(getString(R.string.stats_total_habits_format, overview.totalHabits));
            totalCompletionsText.setText(getString(R.string.stats_total_completions_format, overview.totalCompletions));
            bestStreakText.setText(getString(R.string.stats_best_streak_format, overview.bestStreak));

            chart.setValues(overview.chartValues, overview.chartLabels);

            donut.setSegments(
                    Arrays.asList(overview.distributionCompleted, overview.distributionMissed),
                    Arrays.asList(completedColor, missedColor));
            legendCompleted.setText(getString(R.string.stats_completed_label, Math.round(overview.distributionCompleted * 100)));
            legendMissed.setText(getString(R.string.stats_missed_label, Math.round(overview.distributionMissed * 100)));

            adapter.submitRows(overview.habitRows);
        });
    }

    private static class HabitStatAdapter extends RecyclerView.Adapter<HabitStatAdapter.RowHolder> {
        private List<StatsViewModel.HabitStatRow> rows = new ArrayList<>();

        void submitRows(List<StatsViewModel.HabitStatRow> newRows) {
            rows = newRows;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_stat, parent, false);
            return new RowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            StatsViewModel.HabitStatRow row = rows.get(position);
            holder.name.setText(row.name);
            holder.percent.setText(row.percent + "%");
            holder.progress.setProgress(row.percent, true);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        static class RowHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView percent;
            final ProgressBar progress;

            RowHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.stat_habit_name);
                percent = itemView.findViewById(R.id.stat_habit_percent);
                progress = itemView.findViewById(R.id.stat_habit_progress);
            }
        }
    }
}
