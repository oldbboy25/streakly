package com.apptech.streakly.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.apptech.streakly.R;

/** Small flame badge that gently pulses while a streak is alive, and sits dim/static at zero. */
public class StreakFlameView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path flamePath = new Path();

    private boolean active = false;
    private float pulseScale = 1f;
    private ValueAnimator pulseAnimator;

    public StreakFlameView(Context context) {
        super(context);
    }

    public StreakFlameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        if (active) {
            startPulse();
        } else {
            stopPulse();
        }
        invalidate();
    }

    private void startPulse() {
        stopPulse();
        pulseAnimator = ValueAnimator.ofFloat(0.88f, 1.12f);
        pulseAnimator.setDuration(900);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(a -> {
            pulseScale = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    private void stopPulse() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        pulseScale = 1f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPulse();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(ContextCompat.getColor(getContext(),
                active ? R.color.flame_active : R.color.flame_inactive));

        float w = getWidth();
        float h = getHeight();
        canvas.save();
        canvas.scale(pulseScale, pulseScale, w / 2f, h / 2f);

        flamePath.reset();
        flamePath.moveTo(w * 0.5f, h * 0.05f);
        flamePath.cubicTo(w * 0.85f, h * 0.35f, w * 0.75f, h * 0.55f, w * 0.6f, h * 0.55f);
        flamePath.cubicTo(w * 0.7f, h * 0.3f, w * 0.55f, h * 0.2f, w * 0.5f, h * 0.35f);
        flamePath.cubicTo(w * 0.45f, h * 0.2f, w * 0.3f, h * 0.3f, w * 0.4f, h * 0.55f);
        flamePath.cubicTo(w * 0.25f, h * 0.55f, w * 0.15f, h * 0.35f, w * 0.5f, h * 0.05f);
        flamePath.close();

        canvas.drawPath(flamePath, paint);
        canvas.restore();
    }
}
