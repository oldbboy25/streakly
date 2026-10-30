package com.apptech.streakly.ui.calendar;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;
import com.apptech.streakly.ui.habitdetail.HabitDetailActivity;
import com.apptech.streakly.ui.views.MonthCalendarView;
import com.apptech.streakly.ui.views.StreakFlameView;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class CalendarFragment extends Fragment {

    private CalendarViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        MonthCalendarView calendarView = view.findViewById(R.id.calendar_view);
        TextView countText = view.findViewById(R.id.calendar_habit_count);
        RecyclerView recyclerView = view.findViewById(R.id.calendar_habit_recycler);
        View emptyText = view.findViewById(R.id.calendar_empty_text);

        long todayEpochDay = LocalDate.now().toEpochDay();
        calendarView.setToday(todayEpochDay);
        calendarView.setOnDayClickListener(viewModel::selectDay);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        DayHabitAdapter adapter = new DayHabitAdapter(viewModel::toggleHabitOnSelectedDay, habitId -> {
            Intent intent = new Intent(requireContext(), HabitDetailActivity.class);
            intent.putExtra(HabitDetailActivity.EXTRA_HABIT_ID, habitId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            calendarView.setCompletedDays(state.markedDays);
            countText.setText(getString(R.string.calendar_count_format, state.doneCount, state.totalCount));
            adapter.submitList(state.dayItems);

            boolean empty = state.dayItems.isEmpty();
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    private static class DayHabitAdapter extends ListAdapter<CalendarViewModel.DayHabitItem, DayHabitAdapter.ViewHolder> {

        interface ToggleListener {
            void onToggle(long habitId);
        }

        interface DetailListener {
            void onOpenDetail(long habitId);
        }

        private final ToggleListener toggleListener;
        private final DetailListener detailListener;

        private static final DiffUtil.ItemCallback<CalendarViewModel.DayHabitItem> DIFF =
                new DiffUtil.ItemCallback<CalendarViewModel.DayHabitItem>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull CalendarViewModel.DayHabitItem oldItem,
                                                    @NonNull CalendarViewModel.DayHabitItem newItem) {
                        return oldItem.habit.id == newItem.habit.id;
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull CalendarViewModel.DayHabitItem oldItem,
                                                        @NonNull CalendarViewModel.DayHabitItem newItem) {
                        return oldItem.completedOnDay == newItem.completedOnDay
                                && Objects.equals(oldItem.habit.name, newItem.habit.name);
                    }
                };

        DayHabitAdapter(ToggleListener toggleListener, DetailListener detailListener) {
            super(DIFF);
            this.toggleListener = toggleListener;
            this.detailListener = detailListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(getItem(position), toggleListener, detailListener);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final FrameLayout iconFrame;
            final TextView emoji;
            final TextView name;
            final TextView streakText;
            final StreakFlameView flame;
            final ImageButton checkButton;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconFrame = itemView.findViewById(R.id.habit_icon_frame);
                emoji = itemView.findViewById(R.id.habit_emoji);
                name = itemView.findViewById(R.id.habit_name);
                streakText = itemView.findViewById(R.id.habit_streak_text);
                flame = itemView.findViewById(R.id.streak_flame);
                checkButton = itemView.findViewById(R.id.habit_check_button);
            }

            void bind(CalendarViewModel.DayHabitItem item, ToggleListener toggleListener, DetailListener detailListener) {
                emoji.setText(item.habit.emoji);
                name.setText(item.habit.name);
                flame.setActive(item.completedOnDay);
                tintIconFrame(iconFrame, item.habit.colorHex);

                Resources res = itemView.getResources();
                streakText.setText(item.completedOnDay ? res.getString(R.string.calendar_done_label) : "");

                if (item.completedOnDay) {
                    checkButton.setBackgroundResource(R.drawable.bg_circle_filled_primary);
                    checkButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.white));
                } else {
                    checkButton.setBackgroundResource(R.drawable.bg_circle_outline);
                    checkButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.md_outline));
                }

                checkButton.setOnClickListener(v -> toggleListener.onToggle(item.habit.id));
                itemView.setOnClickListener(v -> detailListener.onOpenDetail(item.habit.id));
            }

            /** Tints the icon circle with a soft wash of the habit's own color, matching the
             * Today screen's habit cards (see HabitAdapter.tintIconFrame). */
            private void tintIconFrame(FrameLayout frame, String colorHex) {
                int color;
                try {
                    color = colorHex == null ? Color.TRANSPARENT : Color.parseColor(colorHex);
                } catch (IllegalArgumentException e) {
                    color = Color.TRANSPARENT;
                }
                if (color == Color.TRANSPARENT) {
                    frame.setBackgroundResource(R.drawable.bg_emoji_circle);
                    return;
                }
                int wash = Color.argb(38, Color.red(color), Color.green(color), Color.blue(color));
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(wash);
                frame.setBackground(drawable);
            }
        }
    }
}
