package com.apptech.streakly.growth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Asks for a Play Store rating exactly once, right after a genuine "happy moment"
 * (a streak milestone) — never on a cold start, never more than once per install,
 * and always through the policy-safe in-app review API rather than a direct link.
 */
public class ReviewPrompter {

    private static final String PREFS_NAME = "streakly_growth_prefs";
    private static final String KEY_HAS_PROMPTED = "has_prompted_review";
    private static final List<Integer> TRIGGER_STREAKS = Arrays.asList(7, 30);

    private ReviewPrompter() {
    }

    public static void maybePromptAfterMilestone(Activity activity, int newStreak) {
        if (!TRIGGER_STREAKS.contains(newStreak)) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_HAS_PROMPTED, false)) {
            return;
        }

        ReviewManager manager = ReviewManagerFactory.create(activity);
        manager.requestReviewFlow().addOnCompleteListener(request -> {
            if (!request.isSuccessful()) {
                return;
            }
            ReviewInfo reviewInfo = request.getResult();
            manager.launchReviewFlow(activity, reviewInfo);
            prefs.edit().putBoolean(KEY_HAS_PROMPTED, true).apply();
        });
    }
}
