package com.apptech.streakly.ui.today;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;
import com.apptech.streakly.ui.addedit.AddEditHabitActivity;
import com.apptech.streakly.ui.habitdetail.HabitDetailActivity;
import com.apptech.streakly.ui.views.CircularProgressView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private HabitAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TodayViewModel viewModel = new ViewModelProvider(this).get(TodayViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.habit_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HabitAdapter(new HabitAdapter.Listener() {
            @Override
            public void onToggle(HabitUiItem item) {
                viewModel.toggleHabit(item.habit.id);
            }

            @Override
            public void onOpenDetail(HabitUiItem item) {
                Intent intent = new Intent(requireContext(), HabitDetailActivity.class);
                intent.putExtra(HabitDetailActivity.EXTRA_HABIT_ID, item.habit.id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        View emptyState = view.findViewById(R.id.empty_state);
        CircularProgressView progressRing = view.findViewById(R.id.today_progress_ring);
        TextView completionText = view.findViewById(R.id.today_completion_text);
        TextView dateText = view.findViewById(R.id.today_date_text);

        dateText.setText(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
                .format(LocalDate.now()));

        viewModel.getUiItems().observe(getViewLifecycleOwner(), (List<HabitUiItem> items) -> {
            adapter.submitList(items);

            boolean empty = items.isEmpty();
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

            int total = items.size();
            int done = 0;
            for (HabitUiItem item : items) {
                if (item.completedToday) {
                    done++;
                }
            }
            float fraction = total == 0 ? 0f : (float) done / total;
            progressRing.setProgress(fraction);
            completionText.setText(getString(R.string.today_completion_format, Math.round(fraction * 100)));
        });

        view.findViewById(R.id.fab_add_habit).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditHabitActivity.class)));
    }
}
