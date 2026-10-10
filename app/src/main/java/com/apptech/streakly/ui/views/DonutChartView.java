package com.apptech.streakly.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
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

/** Simple animated ring chart. Segment fractions should sum to at most 1; the remainder
 * shows as the neutral track color. */
public class DonutChartView extends View {

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();

    private List<Float> fractions = Collections.emptyList();
    private List<Integer> colors = Collections.emptyList();
    private float revealFraction = 1f;
    private ValueAnimator animator;

    public DonutChartView(Context context) {
        super(context);
        init(context);
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.md_surface_variant));
    }

    public void setSegments(List<Float> newFractions, List<Integer> newColors) {
        this.fractions = newFractions == null ? Collections.emptyList() : newFractions;
        this.colors = newColors == null ? Collections.emptyList() : newColors;
        animateReveal();
    }

    private void animateReveal() {
        if (animator != null) {
            animator.cancel();
        }
        revealFraction = 0f;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(700);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            revealFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float stroke = dp(16);
        segmentPaint.setStrokeWidth(stroke);
        trackPaint.setStrokeWidth(stroke);
        float inset = stroke / 2f;
        bounds.set(inset, inset, w - inset, h - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(bounds, 0, 360, false, trackPaint);

        float startAngle = -90f;
        for (int i = 0; i < fractions.size(); i++) {
            float fullSweep = fractions.get(i) * 360f;
            float animatedSweep = fullSweep * revealFraction;
            if (i < colors.size()) {
                segmentPaint.setColor(colors.get(i));
                canvas.drawArc(bounds, startAngle, animatedSweep, false, segmentPaint);
            }
            startAngle += fullSweep;
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
