package com.misterchan.iconeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class ColorAdapter extends ItemMovableAdapter<ColorAdapter.ViewHolder> {

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.v_color);
        }
    }

    private final List<Long> colors;
    private View.OnClickListener onItemClickListener;
    private View.OnLongClickListener onItemLongClickListener;

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
        holder.view.setBackgroundColor(Color.toArgb(color));
        holder.view.setOnClickListener(onItemClickListener);
        holder.view.setOnLongClickListener(onItemLongClickListener);
        holder.view.setTag(color);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false);
        return new ViewHolder(item);
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
        onItemLongClickListener = listener;
    }
}
