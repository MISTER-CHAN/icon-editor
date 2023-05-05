package com.misterchan.iconeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class LayerAdapter extends ItemMovableAdapter<LayerAdapter.ViewHolder> {

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbVisible;
        private final ImageView ivThumbnail;
        private final TextView tvName;
        private final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            cbVisible = itemView.findViewById(R.id.cb_visible);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    private final Frame frame;
    private View.OnClickListener onItemClickListener;
    private View.OnLongClickListener onItemLongClickListener;

    public LayerAdapter(Frame frame) {
        this.frame = frame;
    }

    @Override
    protected List<Layer> getData() {
        return frame.layers;
    }

    @Override
    public int getItemCount() {
        return frame.layers.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Layer layer = frame.layers.get(position);
        holder.itemView.setOnClickListener(onItemClickListener);
        holder.itemView.setOnLongClickListener(onItemLongClickListener);
        holder.ivThumbnail.setImageBitmap(layer.bitmap);
        holder.tvName.setText(layer.name);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layer, parent, false);
        return new ViewHolder(item);
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
        onItemLongClickListener = listener;
    }

}
