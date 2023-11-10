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
        LEVEL, NAME, SELECTED
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

    {
        context = Settings.INST.mainActivity;
        final Resources resources = context.getResources();
        dim64Dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64.0f, resources.getDisplayMetrics());
        final Resources.Theme theme = context.getTheme();
        final TypedValue colorPrimaryTV = new TypedValue(), textColorPrimaryTV = new TypedValue();
        theme.resolveAttribute(android.R.attr.colorPrimary, colorPrimaryTV, true);
        theme.resolveAttribute(android.R.attr.textColorPrimary, textColorPrimaryTV, true);
        colorPrimary = resources.getColor(colorPrimaryTV.resourceId, theme);
        textColorPrimary = resources.getColor(textColorPrimaryTV.resourceId, theme);
    }

    public LayerAdapter(Frame frame) {
        this.frame = frame;
    }

    private void displayOperators(ViewHolder holder, int operators) {
        holder.binding.vLowerLevel.setVisibility(operators >> 30 > 0 ? View.VISIBLE : View.GONE);
        holder.binding.llParentBg.removeAllViews();
        for (int i = 0; i < (operators >> 20 & 0x3FF); ++i) {
            final View v = LayoutInflater.from(context).inflate(R.layout.bracket, holder.binding.llParentBg, false);
            v.setBackground(AppCompatResources.getDrawable(context, R.drawable.bracket_vert_open_np));
            holder.binding.llParentBg.addView(v);
        }
        holder.binding.llFgLeaf.removeAllViews();
        for (int i = 0; i < (operators >> 10 & 0x3FF); ++i) {
            final View v = LayoutInflater.from(context).inflate(R.layout.bracket, holder.binding.llFgLeaf, false);
            v.setBackground(AppCompatResources.getDrawable(context, R.drawable.bracket_vert_close_np));
            holder.binding.llFgLeaf.addView(v);
        }
        holder.binding.llRoot.removeAllViews();
        for (int i = 0; i < (operators & 0x3FF); ++i) {
            final View v = LayoutInflater.from(context).inflate(R.layout.bracket, holder.binding.llRoot, false);
            v.setBackground(AppCompatResources.getDrawable(context, R.drawable.bracket_vert_open_np));
            holder.binding.llRoot.addView(v);
        }
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        for (final Object o : payloads) {
            if (!(o instanceof final Payload payload)) {
                continue;
            }
            final Layer layer = frame.layers.get(position);
            switch (payload) {
                case LEVEL -> displayOperators(holder, layer.displayingOperators);
                case NAME -> holder.binding.tvName.setText(layer.name);
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
        displayOperators(holder, layer.displayingOperators);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemLayerBinding binding = ItemLayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LayerAdapter.ViewHolder(binding);
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
