package com.apptech.streakly.ui.today;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;
import com.apptech.streakly.ui.views.StreakFlameView;

import java.util.Objects;

public class HabitAdapter extends ListAdapter<HabitUiItem, HabitAdapter.ViewHolder> {

    public interface Listener {
        void onToggle(HabitUiItem item);

        void onOpenDetail(HabitUiItem item);
    }

    private final Listener listener;

    private static final DiffUtil.ItemCallback<HabitUiItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<HabitUiItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull HabitUiItem oldItem, @NonNull HabitUiItem newItem) {
            return oldItem.habit.id == newItem.habit.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull HabitUiItem oldItem, @NonNull HabitUiItem newItem) {
            return oldItem.completedToday == newItem.completedToday
                    && oldItem.currentStreak == newItem.currentStreak
                    && Objects.equals(oldItem.habit.name, newItem.habit.name)
                    && Objects.equals(oldItem.habit.emoji, newItem.habit.emoji);
        }
    };

    public HabitAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView emoji;
        private final TextView name;
        private final TextView streakText;
        private final StreakFlameView flame;
        private final ImageButton checkButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.habit_emoji);
            name = itemView.findViewById(R.id.habit_name);
            streakText = itemView.findViewById(R.id.habit_streak_text);
            flame = itemView.findViewById(R.id.streak_flame);
            checkButton = itemView.findViewById(R.id.habit_check_button);
        }

        void bind(HabitUiItem item, Listener listener) {
            emoji.setText(item.habit.emoji);
            name.setText(item.habit.name);
            flame.setActive(item.currentStreak > 0);

            Resources res = itemView.getResources();
            streakText.setText(res.getString(R.string.streak_current_format, item.currentStreak));

            if (item.completedToday) {
                checkButton.setBackgroundResource(R.drawable.bg_circle_filled_primary);
                checkButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                checkButton.setBackgroundResource(R.drawable.bg_circle_outline);
                checkButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.md_outline));
            }

            checkButton.setOnClickListener(v -> {
                animateCheck(checkButton);
                if (listener != null) {
                    listener.onToggle(item);
                }
            });
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenDetail(item);
                }
            });
        }

        private void animateCheck(View view) {
            view.animate().cancel();
            view.setScaleX(0.7f);
            view.setScaleY(0.7f);
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }
}
