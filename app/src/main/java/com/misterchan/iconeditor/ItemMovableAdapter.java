package com.misterchan.iconeditor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class ItemMovableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final int DRAG_DIRS = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

    public void attachItemMoveHelperToRecycleView(RecyclerView recyclerView) {
        final ItemTouchHelper.SimpleCallback itemMoveCallback = new ItemTouchHelper.SimpleCallback(DRAG_DIRS, 0) {
            @Override
            public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
                final int currentPosition = current.getAdapterPosition();
                final int targetPosition = target.getAdapterPosition();
                return 0 <= Math.min(currentPosition, targetPosition) && Math.max(currentPosition, targetPosition) < getItemCount();
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (viewHolder.getItemViewType() != target.getItemViewType()) {
                    return false;
                }
                final int position = viewHolder.getAdapterPosition();
                final int targetPosition = target.getAdapterPosition();
                final List<?> data = getData();
                for (int i = Math.min(position, targetPosition); i < Math.max(position, targetPosition); ++i) {
                    Collections.swap(data, i, i + 1);
                }
                notifyItemMoved(position, targetPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };
        final ItemTouchHelper itemMoveHelper = new ItemTouchHelper(itemMoveCallback);
        itemMoveHelper.attachToRecyclerView(recyclerView);
    }

    protected abstract List<?> getData();
}
