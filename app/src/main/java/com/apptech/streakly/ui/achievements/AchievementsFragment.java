package com.apptech.streakly.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AchievementsViewModel viewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        TextView subtitle = view.findViewById(R.id.achievements_subtitle);
        RecyclerView recyclerView = view.findViewById(R.id.achievements_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        AchievementAdapter adapter = new AchievementAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getAchievements().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            int unlocked = 0;
            for (Achievement a : list) {
                if (a.unlocked) {
                    unlocked++;
                }
            }
            subtitle.setText(getString(R.string.achievements_subtitle_format, unlocked, list.size()));
        });
    }

    private static class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
        private List<Achievement> items = new ArrayList<>();

        void submit(List<Achievement> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Achievement item = items.get(position);
            holder.emoji.setText(item.emoji);
            holder.title.setText(item.titleRes);
            holder.desc.setText(item.descRes);

            float alpha = item.unlocked ? 1f : 0.35f;
            holder.iconFrame.setAlpha(alpha);
            holder.emoji.setAlpha(alpha);
            holder.title.setAlpha(item.unlocked ? 1f : 0.6f);
            holder.desc.setAlpha(item.unlocked ? 1f : 0.6f);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final FrameLayout iconFrame;
            final TextView emoji;
            final TextView title;
            final TextView desc;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconFrame = itemView.findViewById(R.id.achievement_icon_frame);
                emoji = itemView.findViewById(R.id.achievement_emoji);
                title = itemView.findViewById(R.id.achievement_title);
                desc = itemView.findViewById(R.id.achievement_desc);
            }
        }
    }
}
