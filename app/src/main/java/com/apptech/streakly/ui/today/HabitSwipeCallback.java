package com.apptech.streakly.ui.today;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Long-press and drag to reorder; swipe right to complete (item snaps back in place);
 * swipe left to archive (item leaves the list).
 */
public class HabitSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface Listener {
        void onSwipeComplete(int position);

        void onSwipeArchive(int position);

        void onMove(int fromPosition, int toPosition);

        void onDragFinished();
    }

    private final Listener listener;
    private final Paint completePaint = new Paint();
    private final Paint archivePaint = new Paint();

    public HabitSwipeCallback(Listener listener) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
        completePaint.setColor(Color.parseColor("#13874F"));
        archivePaint.setColor(Color.parseColor("#C23B30"));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                           @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
            return false;
        }
        listener.onMove(from, to);
        return true;
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
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        listener.onDragFinished();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                             @NonNull RecyclerView.ViewHolder viewHolder,
                             float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            if (dX > 0) {
                RectF rect = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom());
                c.drawRoundRect(rect, 20f, 20f, completePaint);
            } else if (dX < 0) {
                RectF rect = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                c.drawRoundRect(rect, 20f, 20f, archivePaint);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
