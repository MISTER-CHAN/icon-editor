package com.misterchan.iconeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.misterchan.iconeditor.databinding.ItemColorBinding;

import java.util.List;

class ColorAdapter extends ItemMovableAdapter<ColorAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, Long color);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, Long color);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemColorBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemColorBinding.bind(itemView);
        }
    }

    private final List<Long> colors;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public ColorAdapter(List<Long> colors) {
        this.colors = colors;
    }

    @Override
    protected List<Long> getData() {
        return colors;
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Long color = colors.get(position);
        holder.binding.vColor.setBackgroundColor(Color.toArgb(color));
        holder.binding.vColor.setOnClickListener(v -> onItemClickListener.onItemClick(v, color));
        holder.binding.vColor.setOnLongClickListener(v -> onItemLongClickListener.onItemLongClick(v, color));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false);
        return new ViewHolder(item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }
}
