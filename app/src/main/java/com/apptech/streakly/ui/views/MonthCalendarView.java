package com.apptech.streakly.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.apptech.streakly.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Tap a past or current day to toggle it; tap the header arrows to change month. */
public class MonthCalendarView extends View {

    public interface OnDayClickListener {
        void onDayClicked(long epochDay);
    }

    private YearMonth currentMonth = YearMonth.now();
    private long todayEpochDay = LocalDate.now().toEpochDay();
    private Set<Long> completedDays = new HashSet<>();
    private OnDayClickListener listener;

    private final Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint weekdayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint todayRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint completedTextPaint;

    private float headerHeight;
    private float weekdayRowHeight;
    private float cellSize;
    private float gridTop;

    private int leadingBlanksCache;
    private int daysInMonthCache;

    public MonthCalendarView(Context context) {
        super(context);
        init(context);
    }

    public MonthCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setClickable(true);

        headerPaint.setColor(ContextCompat.getColor(context, R.color.md_on_surface));
        headerPaint.setTextSize(dp(15));
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);

        arrowPaint.setColor(ContextCompat.getColor(context, R.color.md_on_surface_variant));
        arrowPaint.setTextSize(dp(22));
        arrowPaint.setTextAlign(Paint.Align.CENTER);

        weekdayPaint.setColor(ContextCompat.getColor(context, R.color.md_on_surface_variant));
        weekdayPaint.setTextSize(dp(11));
        weekdayPaint.setTextAlign(Paint.Align.CENTER);

        dayTextPaint.setColor(ContextCompat.getColor(context, R.color.md_on_surface));
        dayTextPaint.setTextSize(dp(13));
        dayTextPaint.setTextAlign(Paint.Align.CENTER);

        todayRingPaint.setStyle(Paint.Style.STROKE);
        todayRingPaint.setStrokeWidth(dp(1.5f));
        todayRingPaint.setColor(ContextCompat.getColor(context, R.color.md_outline));

        completedPaint.setColor(ContextCompat.getColor(context, R.color.chart_bar));

        completedTextPaint = new Paint(dayTextPaint);
        completedTextPaint.setColor(ContextCompat.getColor(context, R.color.white));

        headerHeight = dp(40);
        weekdayRowHeight = dp(24);
    }

    public void setToday(long todayEpochDay) {
        this.todayEpochDay = todayEpochDay;
        this.currentMonth = YearMonth.from(LocalDate.ofEpochDay(todayEpochDay));
        invalidate();
    }

    public void setCompletedDays(Set<Long> completedDays) {
        this.completedDays = completedDays == null ? new HashSet<>() : completedDays;
        invalidate();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = w / 7f;
        gridTop = headerHeight + weekdayRowHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();

        String monthLabel = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear();
        float headerTextY = headerHeight / 2f - ((headerPaint.descent() + headerPaint.ascent()) / 2f);
        canvas.drawText(monthLabel, width / 2f, headerTextY, headerPaint);
        canvas.drawText("‹", dp(24), headerTextY, arrowPaint);
        canvas.drawText("›", width - dp(24), headerTextY, arrowPaint);

        DayOfWeek[] order = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        float weekdayTextY = headerHeight + weekdayRowHeight / 2f
                - ((weekdayPaint.descent() + weekdayPaint.ascent()) / 2f);
        for (int i = 0; i < 7; i++) {
            String label = order[i].getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 1);
            canvas.drawText(label, cellSize * i + cellSize / 2f, weekdayTextY, weekdayPaint);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() - 1;
        int daysInMonth = currentMonth.lengthOfMonth();
        leadingBlanksCache = leadingBlanks;
        daysInMonthCache = daysInMonth;

        for (int day = 1; day <= daysInMonth; day++) {
            int cellIndex = leadingBlanks + day - 1;
            int col = cellIndex % 7;
            int row = cellIndex / 7;

            float cx = col * cellSize + cellSize / 2f;
            float cy = gridTop + row * cellSize + cellSize / 2f;
            float radius = cellSize * 0.36f;

            long epochDay = currentMonth.atDay(day).toEpochDay();
            boolean isToday = epochDay == todayEpochDay;
            boolean isCompleted = completedDays.contains(epochDay);

            if (isCompleted) {
                canvas.drawCircle(cx, cy, radius, completedPaint);
            }
            if (isToday) {
                canvas.drawCircle(cx, cy, radius, todayRingPaint);
            }

            Paint textPaint = isCompleted ? completedTextPaint : dayTextPaint;
            float dayTextY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f);
            canvas.drawText(String.valueOf(day), cx, dayTextY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                handleTap(event.getX(), event.getY());
                performClick();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void handleTap(float x, float y) {
        if (y <= headerHeight) {
            currentMonth = x < getWidth() / 2f ? currentMonth.minusMonths(1) : currentMonth.plusMonths(1);
            invalidate();
            return;
        }
        if (y >= gridTop) {
            int col = (int) (x / cellSize);
            int row = (int) ((y - gridTop) / cellSize);
            int cellIndex = row * 7 + col;
            int day = cellIndex - leadingBlanksCache + 1;
            if (day >= 1 && day <= daysInMonthCache) {
                long epochDay = currentMonth.atDay(day).toEpochDay();
                if (epochDay <= todayEpochDay && listener != null) {
                    listener.onDayClicked(epochDay);
                }
            }
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
