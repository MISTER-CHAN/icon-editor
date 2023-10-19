package com.misterchan.iconeditor.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.OneShotPreDrawListener;
import androidx.recyclerview.widget.RecyclerView;

import com.misterchan.iconeditor.Frame;
import com.misterchan.iconeditor.Project;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.databinding.ItemFrameBinding;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;

import java.util.List;

public class FrameAdapter extends ItemMovableAdapter<FrameAdapter.ViewHolder> {

    public enum Payload {
        DELAY, SELECTED
    }

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        for (final Object o : payloads) {
            if (!(o instanceof final Payload payload)) {
                continue;
            }
            switch (payload) {
                case DELAY -> {
                    final Frame frame = project.frames.get(position);
                    holder.binding.tvDelay.setText(context.getString(R.string.milliseconds, frame.delay));
                }
                case SELECTED -> setFrameSelected(holder, position);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Frame frame = project.frames.get(position);

        holder.itemView.setOnClickListener(v -> {
            if (project.selectedFrameIndex != position) {
                onItemSelectedListener.onItemSelected(v, position);
            } else {
                onItemReselectedListener.onItemSelected(v, position);
            }
        });
        OneShotPreDrawListener.add(holder.binding.flThumbnail, () -> {
            final Bitmap thumbnail = frame.getThumbnail();
            final ViewGroup.LayoutParams lp = holder.binding.flThumbnail.getLayoutParams();
            final int w = thumbnail.getWidth(), h = thumbnail.getHeight();
            lp.width = w >= h ? dim64Dip : dim64Dip * w / h;
            lp.height = w >= h ? dim64Dip * h / w : dim64Dip;
            holder.binding.flThumbnail.setLayoutParams(lp);
        });
        holder.binding.ivThumbnail.setImageBitmap(frame.getThumbnail());
        holder.binding.rb.setText(String.valueOf(position));
        holder.binding.tvDelay.setText(context.getString(R.string.milliseconds, frame.delay));
        setFrameSelected(holder, position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemFrameBinding binding = ItemFrameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FrameAdapter.ViewHolder(binding);
    }

    private void setFrameSelected(ViewHolder holder, int position) {
        final boolean selected = position == project.selectedFrameIndex;
        holder.binding.rb.setOnCheckedChangeListener(null);
        holder.binding.rb.setChecked(selected);
        holder.binding.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener, OnItemSelectedListener onItemReselectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        this.onItemReselectedListener = onItemReselectedListener;
    }
}
