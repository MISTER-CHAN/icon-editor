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

import com.misterchan.iconeditor.databinding.ItemColorBinding;
import com.misterchan.iconeditor.databinding.ItemFrameBinding;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;

import java.util.List;

class FrameAdapter extends ItemMovableAdapter<FrameAdapter.ViewHolder> {

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFrameBinding binding;

        public ViewHolder(@NonNull ItemFrameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

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
        holder.binding.rb.setOnCheckedChangeListener(null);
        holder.binding.rb.setChecked(selected);
        holder.binding.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
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
        OneShotPreDrawListener.add(holder.binding.flThumbnail, () -> {
            final Bitmap background = frame.getBackgroundLayer().bitmap;
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.binding.flThumbnail.getLayoutParams();
            final int w = background.getWidth(), h = background.getHeight();
            lp.width = w >= h ? dim64Dip : dim64Dip * w / h;
            lp.height = w >= h ? dim64Dip * h / w : dim64Dip;
            holder.binding.flThumbnail.setLayoutParams(lp);
        });
        holder.binding.ivThumbnail.setImageBitmap(frame.getBackgroundLayer().bitmap);
        holder.binding.rb.setOnCheckedChangeListener(null);
        holder.binding.rb.setChecked(selected);
        holder.binding.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
        holder.binding.rb.setText(context.getString(R.string.milliseconds, frame.delay));
        holder.binding.tvThumbnail.setText(String.valueOf(position));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemFrameBinding binding = ItemFrameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FrameAdapter.ViewHolder(binding);
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
