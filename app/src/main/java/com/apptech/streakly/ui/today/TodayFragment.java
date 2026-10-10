package com.apptech.streakly.ui.today;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;
import com.apptech.streakly.growth.ReviewPrompter;
import com.apptech.streakly.model.Habit;
import com.apptech.streakly.notifications.ReminderScheduler;
import com.apptech.streakly.repository.HabitRepository;
import com.apptech.streakly.ui.addedit.TemplatePickerActivity;
import com.apptech.streakly.ui.habitdetail.HabitDetailActivity;
import com.apptech.streakly.ui.views.CircularProgressView;
import com.apptech.streakly.ui.views.ConfettiView;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TodayFragment extends Fragment {

    private static final Set<Integer> CELEBRATION_MILESTONES = new HashSet<>(Arrays.asList(7, 14, 30, 100, 365));

    private HabitAdapter adapter;
    private TodayViewModel viewModel;
    private HabitRepository repository;
    private ConfettiView confettiView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TodayViewModel.class);
        repository = HabitRepository.getInstance(requireContext());
        confettiView = view.findViewById(R.id.confetti_view);

        RecyclerView recyclerView = view.findViewById(R.id.habit_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HabitAdapter(new HabitAdapter.Listener() {
            @Override
            public void onToggle(HabitUiItem item) {
                performToggle(item);
            }

            @Override
            public void onOpenDetail(HabitUiItem item) {
                Intent intent = new Intent(requireContext(), HabitDetailActivity.class);
                intent.putExtra(HabitDetailActivity.EXTRA_HABIT_ID, item.habit.id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new HabitSwipeCallback(new HabitSwipeCallback.Listener() {
            @Override
            public void onSwipeComplete(int position) {
                HabitUiItem item = adapter.getCurrentList().get(position);
                performToggle(item);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onSwipeArchive(int position) {
                HabitUiItem item = adapter.getCurrentList().get(position);
                archiveHabit(item.habit);
            }

            @Override
            public void onMove(int fromPosition, int toPosition) {
                adapter.moveItem(fromPosition, toPosition);
            }

            @Override
            public void onDragFinished() {
                List<Habit> habitsInOrder = new ArrayList<>();
                for (HabitUiItem item : adapter.getCurrentList()) {
                    habitsInOrder.add(item.habit);
                }
                repository.updateSortOrders(habitsInOrder);
            }
        })).attachToRecyclerView(recyclerView);

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
                startActivity(new Intent(requireContext(), TemplatePickerActivity.class)));
    }

    private void performToggle(HabitUiItem item) {
        boolean wasCompleted = item.completedToday;
        viewModel.toggleHabit(item.habit.id);

        if (!wasCompleted) {
            int newStreak = item.currentStreak + 1;
            if (CELEBRATION_MILESTONES.contains(newStreak)) {
                celebrate(newStreak);
            }
        }
    }

    private void celebrate(int streak) {
        confettiView.burst();
        View root = getView();
        if (root != null) {
            root.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            Snackbar.make(root, getString(R.string.milestone_celebration_format, streak), Snackbar.LENGTH_LONG).show();
        }
        if (getActivity() != null) {
            ReviewPrompter.maybePromptAfterMilestone(getActivity(), streak);
        }
    }

    private void archiveHabit(Habit habit) {
        habit.archived = true;
        ReminderScheduler.cancel(requireContext(), habit.id);
        repository.updateHabit(habit, null);

        View root = getView();
        if (root == null) {
            return;
        }
        Snackbar.make(root, R.string.archive_undo_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, v -> {
                    habit.archived = false;
                    repository.updateHabit(habit, () -> {
                        if (habit.reminderEnabled && getContext() != null) {
                            ReminderScheduler.schedule(requireContext(), habit);
                        }
                    });
                })
                .show();
    }
}
