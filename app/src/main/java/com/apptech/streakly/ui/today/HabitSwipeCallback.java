package com.apptech.streakly.ui.today;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/** Swipe right to complete (item snaps back in place), swipe left to archive (item leaves the list). */
public class HabitSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface Listener {
        void onSwipeComplete(int position);

        void onSwipeArchive(int position);
    }

    private final Listener listener;
    private final Paint completePaint = new Paint();
    private final Paint archivePaint = new Paint();

    public HabitSwipeCallback(Listener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
        completePaint.setColor(Color.parseColor("#4F6F52"));
        archivePaint.setColor(Color.parseColor("#B0453E"));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                           @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        if (direction == ItemTouchHelper.RIGHT) {
            listener.onSwipeComplete(position);
        } else {
            listener.onSwipeArchive(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                             @NonNull RecyclerView.ViewHolder viewHolder,
                             float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        if (dX > 0) {
            RectF rect = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom());
            c.drawRoundRect(rect, 20f, 20f, completePaint);
        } else if (dX < 0) {
            RectF rect = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            c.drawRoundRect(rect, 20f, 20f, archivePaint);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
