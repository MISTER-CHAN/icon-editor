package com.misterchan.iconeditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.OneShotPreDrawListener;
import androidx.recyclerview.widget.RecyclerView;

import com.misterchan.iconeditor.listener.OnCBCheckedListener;

import java.util.List;

class FrameAdapter extends ItemMovableAdapter<FrameAdapter.ViewHolder> {

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout flThumbnail;
        private final ImageView ivThumbnail;
        private final RadioButton rb;
        private final TextView tvThumbnail;
        private final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            flThumbnail = itemView.findViewById(R.id.fl_thumbnail);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvThumbnail = itemView.findViewById(R.id.tv_thumbnail);
            rb = itemView.findViewById(R.id.rb);
        }
    }

    public boolean isOnVisibleChangedListenerEnabled = false;
    private final Context context;
    private final Project project;
    private final int dim64Dip;
    private OnItemSelectedListener onItemSelectedListener;
    private OnItemSelectedListener onItemReselectedListener;
    private RecyclerView recyclerView;

    {
        context = Settings.INST.mainActivity;
        final Resources resources = context.getResources();
        dim64Dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64.0f, resources.getDisplayMetrics());
    }

    public FrameAdapter(Project project) {
        this.project = project;
    }

    @Override
    protected List<Frame> getData() {
        return project.frames;
    }

    @Override
    public int getItemCount() {
        return project.frames.size();
    }

    public void notifyFrameSelectedChanged(int position, boolean selected) {
        final ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            return;
        }
        holder.rb.setOnCheckedChangeListener(null);
        holder.rb.setChecked(selected);
        holder.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Frame frame = project.frames.get(position);
        final boolean selected = position == project.selectedFrameIndex;

        holder.itemView.setOnClickListener(v -> {
            if (project.selectedFrameIndex != position) {
                onItemSelectedListener.onItemSelected(v, position);
            } else {
                onItemReselectedListener.onItemSelected(v, position);
            }
        });
        OneShotPreDrawListener.add(holder.flThumbnail, () -> {
            final Bitmap background = frame.getBackgroundLayer().bitmap;
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.flThumbnail.getLayoutParams();
            final int w = background.getWidth(), h = background.getHeight();
            lp.width = w >= h ? dim64Dip : dim64Dip * w / h;
            lp.height = w >= h ? dim64Dip * h / w : dim64Dip;
            holder.flThumbnail.setLayoutParams(lp);
        });
        holder.ivThumbnail.setImageBitmap(frame.getBackgroundLayer().bitmap);
        holder.rb.setOnCheckedChangeListener(null);
        holder.rb.setChecked(selected);
        holder.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
        holder.rb.setText(context.getString(R.string.milliseconds, frame.delay));
        holder.tvThumbnail.setText(String.valueOf(position));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener, OnItemSelectedListener onItemReselectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        this.onItemReselectedListener = onItemReselectedListener;
    }
}
