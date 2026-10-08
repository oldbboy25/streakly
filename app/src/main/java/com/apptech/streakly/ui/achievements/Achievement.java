package com.apptech.streakly.ui.achievements;

public class Achievement {
    public final String emoji;
    public final int titleRes;
    public final int descRes;
    public final boolean unlocked;

    public Achievement(String emoji, int titleRes, int descRes, boolean unlocked) {
        this.emoji = emoji;
        this.titleRes = titleRes;
        this.descRes = descRes;
        this.unlocked = unlocked;
    }
}
