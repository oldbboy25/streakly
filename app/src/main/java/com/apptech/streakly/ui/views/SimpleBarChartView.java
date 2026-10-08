package com.apptech.streakly.ui.views;

import android.content.Context;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.apptech.streakly.R;

import java.util.Collections;
import java.util.List;

/**
 * Lightweight self-contained bar chart (no external chart library). Bars animate in from
 * zero height whenever new data is set, which is what makes Stats/Habit Detail feel alive
 * rather than static.
 */
public class SimpleBarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private List<Float> values = Collections.emptyList();
    private List<String> labels = Collections.emptyList();

    private float revealFraction = 1f;
    private ValueAnimator animator;

    public SimpleBarChartView(Context context) {
        super(context);
        init(context);
    }

    public SimpleBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        barPaint.setColor(ContextCompat.getColor(context, R.color.chart_bar));
        trackPaint.setColor(ContextCompat.getColor(context, R.color.chart_bar_track));
        labelPaint.setColor(ContextCompat.getColor(context, R.color.md_on_surface_variant));
        labelPaint.setTextSize(dp(11));
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setValues(List<Float> newValues, List<String> newLabels) {
        this.values = newValues == null ? Collections.emptyList() : newValues;
        this.labels = newLabels == null ? Collections.emptyList() : newLabels;
        animateReveal();
    }

    private void animateReveal() {
        if (animator != null) {
            animator.cancel();
        }
        revealFraction = 0f;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            revealFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.isEmpty()) {
            return;
        }
        int count = values.size();
        float width = getWidth();
        float height = getHeight();
        float labelSpace = labels.isEmpty() ? 0 : dp(16);
        float chartHeight = height - labelSpace;

        float gap = dp(8);
        float totalGap = gap * (count + 1);
        float barWidth = (width - totalGap) / count;
        float radius = Math.min(barWidth / 3f, dp(6));

        for (int i = 0; i < count; i++) {
            float left = gap + i * (barWidth + gap);
            float right = left + barWidth;

            rect.set(left, 0, right, chartHeight);
            canvas.drawRoundRect(rect, radius, radius, trackPaint);

            float value = Math.max(0f, Math.min(1f, values.get(i)));
            float barTop = chartHeight - (chartHeight * value * revealFraction);
            rect.set(left, barTop, right, chartHeight);
            canvas.drawRoundRect(rect, radius, radius, barPaint);

            if (!labels.isEmpty() && i < labels.size()) {
                canvas.drawText(labels.get(i), (left + right) / 2f, height - dp(2), labelPaint);
            }
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
