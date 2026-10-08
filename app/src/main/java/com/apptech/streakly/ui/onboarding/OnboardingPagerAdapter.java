package com.apptech.streakly.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apptech.streakly.R;

import java.util.List;

class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder> {

    private final List<OnboardingPage> pages;

    OnboardingPagerAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);
        holder.emoji.setText(page.emoji);
        holder.title.setText(page.titleRes);
        holder.subtitle.setText(page.subtitleRes);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        final TextView emoji;
        final TextView title;
        final TextView subtitle;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.page_emoji);
            title = itemView.findViewById(R.id.page_title);
            subtitle = itemView.findViewById(R.id.page_subtitle);
        }
    }
}
