package com.apptech.streakly.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Simple burst-and-fall confetti overlay for streak milestones. No external library needed. */
public class ConfettiView extends View {

    private static final int[] PALETTE = {
            Color.parseColor("#4F6F52"), Color.parseColor("#3D6B8A"),
            Color.parseColor("#7A5C9E"), Color.parseColor("#C9742F"),
            Color.parseColor("#B0453E"), Color.parseColor("#2E8C84"),
            Color.parseColor("#B14E7A")
    };

    private static class Particle {
        float x0, y0;
        float vx;
        float size;
        float rotationSpeed;
        float startRotation;
        int color;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private ValueAnimator animator;
    private float progress = 0f;

    public ConfettiView(Context context) {
        super(context);
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void burst() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        particles.clear();
        int count = 36;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x0 = random.nextFloat() * width;
            p.y0 = height * (0.05f + random.nextFloat() * 0.15f);
            p.vx = (random.nextFloat() - 0.5f) * width * 0.6f;
            p.size = dp(6 + random.nextInt(6));
            p.rotationSpeed = (random.nextFloat() - 0.5f) * 720f;
            p.startRotation = random.nextFloat() * 360f;
            p.color = PALETTE[random.nextInt(PALETTE.length)];
            particles.add(p);
        }

        setVisibility(VISIBLE);
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1300);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                particles.clear();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) {
            return;
        }
        float height = getHeight();
        float fallDistance = height * 1.1f;

        for (Particle p : particles) {
            float t = progress;
            float y = p.y0 + fallDistance * (t * t);
            float x = p.x0 + p.vx * t;
            float alpha = t < 0.75f ? 1f : (1f - (t - 0.75f) / 0.25f);

            paint.setColor(p.color);
            paint.setAlpha((int) (Math.max(0f, alpha) * 255));

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(p.startRotation + p.rotationSpeed * t);
            canvas.drawRoundRect(-p.size / 2f, -p.size / 4f, p.size / 2f, p.size / 4f, 2f, 2f, paint);
            canvas.restore();
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
