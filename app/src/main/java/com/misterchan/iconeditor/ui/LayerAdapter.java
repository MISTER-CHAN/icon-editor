package com.misterchan.iconeditor.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.OneShotPreDrawListener;
import androidx.recyclerview.widget.RecyclerView;

import com.misterchan.iconeditor.Frame;
import com.misterchan.iconeditor.Layer;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.databinding.ItemLayerBinding;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;

import java.util.List;

public class LayerAdapter extends ItemMovableAdapter<LayerAdapter.ViewHolder> {

    public enum Payload {
        NAME, SELECTED
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLayerBinding binding;

        public ViewHolder(@NonNull ItemLayerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public boolean isOnVisibleChangedListenerEnabled = false;
    private final Context context;
    private final Frame frame;
    private final int colorPrimary, textColorPrimary;
    private final int dim64Dip;
    private CompoundButton.OnCheckedChangeListener ovcbccListener;
    private OnItemSelectedListener onItemSelectedListener;
    private OnItemSelectedListener onItemReselectedListener;
    private RecyclerView recyclerView;

    {
        context = Settings.INST.mainActivity;
        final Resources resources = context.getResources();
        dim64Dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64.0f, resources.getDisplayMetrics());
        final Resources.Theme theme = context.getTheme();
        final TypedValue colorPrimaryTv = new TypedValue(), textColorPrimaryTv = new TypedValue();
        theme.resolveAttribute(android.R.attr.colorPrimary, colorPrimaryTv, true);
        theme.resolveAttribute(android.R.attr.textColorPrimary, textColorPrimaryTv, true);
        colorPrimary = resources.getColor(colorPrimaryTv.resourceId, theme);
        textColorPrimary = resources.getColor(textColorPrimaryTv.resourceId, theme);
    }

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

    public void notifyLayerTreeChanged() {
        if (recyclerView == null) {
            return;
        }
        Layer lastLayer = null;
        ViewHolder lastHolder = null;
        for (int i = frame.layers.size() - 1; i >= 0; --i) {
            final Layer layer = frame.layers.get(i);
            final ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder == null) {
                return;
            }

            holder.binding.vLowerLevel.setVisibility(layer.getLevel() > 0 ? View.VISIBLE : View.GONE);
            holder.binding.llRoot.removeAllViews();
            holder.binding.llParentBg.removeAllViews();
            holder.binding.llFgLeaf.removeAllViews();
            if (lastLayer == null) {
                for (int l = 0; l < layer.getLevel() - 1; ++l) {
                    final View v = LayoutInflater.from(context).inflate(R.layout.bracket, holder.binding.llRoot, false);
                    v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_vert_open));
                    holder.binding.llRoot.addView(v);
                }
            } else {
                final int levelDiff = layer.getLevel() - lastLayer.getLevel();
                if (levelDiff > 0) {
                    for (int l = 0; l < (lastLayer.getLevel() > 0 ? levelDiff : levelDiff - 1); ++l) {
                        final View v = LayoutInflater.from(context).inflate(R.layout.bracket, lastHolder.binding.llParentBg, false);
                        v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_vert_open));
                        lastHolder.binding.llParentBg.addView(v);
                    }
                } else if (levelDiff < 0) {
                    for (int l = 0; l < (layer.getLevel() > 0 ? -levelDiff : -levelDiff - 1); ++l) {
                        final View v = LayoutInflater.from(context).inflate(R.layout.bracket, lastHolder.binding.llFgLeaf, false);
                        v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_vert_close));
                        lastHolder.binding.llFgLeaf.addView(v);
                    }
                }
            }
            lastLayer = layer;
            lastHolder = holder;
        }
        if (lastLayer != null) {
            for (int l = 0; l < lastLayer.getLevel() - 1; ++l) {
                final View v = LayoutInflater.from(context).inflate(R.layout.bracket, lastHolder.binding.llFgLeaf, false);
                v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_vert_close));
                lastHolder.binding.llFgLeaf.addView(v);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
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
                case NAME -> {
                    final Layer layer = frame.layers.get(position);
                    holder.binding.tvName.setText(layer.name);
                }
                case SELECTED -> setLayerSelected(holder, position);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Layer layer = frame.layers.get(position);

        holder.itemView.setOnClickListener(v -> {
            if (frame.selectedLayerIndex != position) {
                onItemSelectedListener.onItemSelected(v, position);
            } else {
                onItemReselectedListener.onItemSelected(v, position);
            }
        });
        holder.binding.cbVisible.setOnCheckedChangeListener(null);
        holder.binding.cbVisible.setChecked(layer.visible);
        holder.binding.cbVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layer.visible = isChecked;
            if (isOnVisibleChangedListenerEnabled) {
                ovcbccListener.onCheckedChanged(buttonView, isChecked);
            }
        });
        OneShotPreDrawListener.add(holder.binding.flThumbnail, () -> {
            final ViewGroup.LayoutParams lp = holder.binding.flThumbnail.getLayoutParams();
            final int w = layer.bitmap.getWidth(), h = layer.bitmap.getHeight();
            lp.width = w >= h ? dim64Dip : dim64Dip * w / h;
            lp.height = w >= h ? dim64Dip * h / w : dim64Dip;
            holder.binding.flThumbnail.setLayoutParams(lp);
        });
        holder.binding.ivThumbnail.setImageBitmap(layer.bitmap);
        holder.binding.tvName.setText(layer.name);
        setLayerSelected(holder, position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemLayerBinding binding = ItemLayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LayerAdapter.ViewHolder(binding);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    private void setLayerSelected(ViewHolder holder, int position) {
        final boolean selected = position == frame.selectedLayerIndex;
        holder.binding.rb.setOnCheckedChangeListener(null);
        holder.binding.rb.setChecked(selected);
        holder.binding.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
        holder.binding.tvName.setTextColor(selected ? colorPrimary : textColorPrimary);
        holder.binding.tvName.setTypeface(Typeface.defaultFromStyle(selected ? Typeface.BOLD : Typeface.NORMAL));
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener, OnItemSelectedListener onItemReselectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        this.onItemReselectedListener = onItemReselectedListener;
    }

    /**
     * @param listener the callback to call on checked state of {@link ItemLayerBinding#cbVisible} change
     */
    public void setOnLayerVisibleChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        ovcbccListener = listener;
        isOnVisibleChangedListenerEnabled = true;
    }
}
