package com.apptech.streakly.ui.stats;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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
        TextView successRateText = view.findViewById(R.id.stats_success_rate_text);
        TextView successRateDeltaText = view.findViewById(R.id.stats_success_rate_delta);
        TextView insightText = view.findViewById(R.id.stats_insight_text);
        LineChart lineChart = view.findViewById(R.id.stats_line_chart);
        PieChart pieChart = view.findViewById(R.id.stats_pie_chart);
        TextView legendCompleted = view.findViewById(R.id.legend_text_completed);
        TextView legendMissed = view.findViewById(R.id.legend_text_missed);
        RecyclerView recyclerView = view.findViewById(R.id.stats_habit_recycler);
        ChipGroup rangeChipGroup = view.findViewById(R.id.stats_range_chip_group);

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.chart_bar);
        int completedColor = ContextCompat.getColor(requireContext(), R.color.accent_success);
        int missedColor = ContextCompat.getColor(requireContext(), R.color.md_outline);
        view.findViewById(R.id.legend_dot_completed).setBackgroundTintList(ColorStateList.valueOf(completedColor));
        view.findViewById(R.id.legend_dot_missed).setBackgroundTintList(ColorStateList.valueOf(missedColor));

        setUpLineChart(lineChart, primaryColor);
        setUpPieChart(pieChart);

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

            int successPercent = Math.round(overview.distributionCompleted * 100);
            successRateText.setText(successPercent + "%");

            int delta = overview.successRateDeltaPercent;
            String arrow = delta >= 0 ? "↑" : "↓";
            successRateDeltaText.setText(String.format("%s %s%d%%", arrow, delta >= 0 ? "+" : "", delta));
            successRateDeltaText.setTextColor(delta >= 0 ? completedColor
                    : ContextCompat.getColor(requireContext(), R.color.accent_error));

            updateLineChart(lineChart, overview.chartValues, overview.chartLabels, primaryColor);
            updatePieChart(pieChart, overview.distributionCompleted, overview.distributionMissed,
                    completedColor, missedColor);

            legendCompleted.setText(getString(R.string.stats_completed_label, Math.round(overview.distributionCompleted * 100)));
            legendMissed.setText(getString(R.string.stats_missed_label, Math.round(overview.distributionMissed * 100)));
            insightText.setText(getString(R.string.stats_overview_insight_format, successPercent));

            adapter.submitRows(overview.habitRows);
        });
    }

    private void setUpLineChart(LineChart chart, int primaryColor) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setExtraOffsets(0f, 4f, 0f, 4f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_on_surface_variant));
        xAxis.setTextSize(10f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
    }

    private void updateLineChart(LineChart chart, List<Float> values, List<String> labels, int primaryColor) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i) * 100f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "success");
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(primaryColor);
        dataSet.setCircleRadius(3.5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(primaryColor);
        dataSet.setFillAlpha(40);

        chart.setData(new LineData(dataSet));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(Math.min(labels.size(), 7));
        chart.animateY(500);
        chart.invalidate();
    }

    private void setUpPieChart(PieChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.setHoleRadius(72f);
        chart.setTransparentCircleRadius(0f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setRotationEnabled(false);
        chart.setTouchEnabled(false);
    }

    private void updatePieChart(PieChart chart, float completed, float missed, int completedColor, int missedColor) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(Math.max(completed, 0.001f)));
        entries.add(new PieEntry(Math.max(missed, 0.001f)));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(completedColor, missedColor);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(2f);

        chart.setCenterText(Math.round(completed * 100) + "%");
        chart.setCenterTextSize(18f);
        chart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.md_on_surface));
        chart.setData(new PieData(dataSet));
        chart.animateY(600);
        chart.invalidate();
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
