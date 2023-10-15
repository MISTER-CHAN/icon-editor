package com.misterchan.iconeditor.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class ItemMovableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final int DRAG_DIRS = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

    public interface OnItemMoveListener {
        void onItemMove(int fromPos, int toPos);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(View view, int position);
    }

    public static ItemTouchHelper createItemMoveHelper(OnItemMoveListener onItemMoveListener) {
        final ItemTouchHelper.SimpleCallback onItemMoveCallback = new ItemTouchHelper.SimpleCallback(DRAG_DIRS, 0) {
            private int dragFrom = -1, dragTo = -1;

            @Override
            public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
                final int currentPosition = current.getAdapterPosition();
                final int targetPosition = target.getAdapterPosition();
                return 0 <= Math.min(currentPosition, targetPosition) && Math.max(currentPosition, targetPosition) < recyclerView.getAdapter().getItemCount();
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (onItemMoveListener != null && dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    onItemMoveListener.onItemMove(dragFrom, dragTo);
                }
                dragFrom = dragTo = -1;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (viewHolder.getItemViewType() != target.getItemViewType()) {
                    return false;
                }
                final int position = viewHolder.getAdapterPosition();
                final int targetPosition = target.getAdapterPosition();
                if (dragFrom == -1) {
                    dragFrom = position;
                }
                dragTo = targetPosition;
                final RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                final List<?> data = ((ItemMovableAdapter<?>) adapter).getData();
                for (int i = Math.min(position, targetPosition); i < Math.max(position, targetPosition); ++i) {
                    Collections.swap(data, i, i + 1);
                }
                adapter.notifyItemMoved(position, targetPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };
        return new ItemTouchHelper(onItemMoveCallback);
    }

    protected abstract List<?> getData();
}
