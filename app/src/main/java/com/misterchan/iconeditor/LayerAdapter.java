package com.misterchan.iconeditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.misterchan.iconeditor.listener.OnCBCheckedListener;

import java.util.List;

class LayerAdapter extends ItemMovableAdapter<LayerAdapter.ViewHolder> {

    public interface OnItemSelectedListener {
        void onItemSelected(View view, int position);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbVisible;
        private final ConstraintLayout cl;
        private final ImageView ivThumbnail;
        private final RadioButton rb;
        private final TextView tvName;
        private final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            cbVisible = itemView.findViewById(R.id.cb_visible);
            cl = itemView.findViewById(R.id.cl);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            rb = itemView.findViewById(R.id.rb);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    {
        final Context context = Settings.INST.mainActivity;
        final Resources.Theme theme = context.getTheme();
        final TypedValue colorPrimaryTv = new TypedValue(), textColorPrimaryTv = new TypedValue();
        theme.resolveAttribute(android.R.attr.colorPrimary, colorPrimaryTv, true);
        theme.resolveAttribute(android.R.attr.textColorPrimary, textColorPrimaryTv, true);
        final Resources resources = context.getResources();
        colorPrimary = resources.getColor(colorPrimaryTv.resourceId, theme);
        textColorPrimary = resources.getColor(textColorPrimaryTv.resourceId, theme);
    }

    public boolean isOnVisibleChangedListenerEnabled = false;
    private final Frame frame;
    private final int colorPrimary, textColorPrimary;
    private CompoundButton.OnCheckedChangeListener ovcbccListener;
    private OnItemSelectedListener onItemSelectedListener;
    private OnItemSelectedListener onItemReselectedListener;

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
        final boolean selected = position == frame.selectedLayerIndex;

        holder.itemView.setOnClickListener(v -> {
            if (frame.selectedLayerIndex != position) {
                onItemSelectedListener.onItemSelected(v, position);
            } else {
                onItemReselectedListener.onItemSelected(v, position);
            }
        });

        holder.cbVisible.setChecked(layer.visible);
        holder.cbVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layer.visible = isChecked;
            if (isOnVisibleChangedListenerEnabled) {
                ovcbccListener.onCheckedChanged(buttonView, isChecked);
            }
        });

        holder.ivThumbnail.setImageBitmap(layer.bitmap);
        holder.rb.setChecked(selected);
        holder.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
        holder.tvName.setText(layer.name);
        holder.tvName.setTextColor(selected ? colorPrimary : textColorPrimary);
        holder.tvName.setTypeface(Typeface.defaultFromStyle(selected ? Typeface.BOLD : Typeface.NORMAL));

//        miHasAlpha.setChecked(bitmap.hasAlpha());
//        miLayerColorMatrix.setChecked(layer.filter == COLOR_MATRIX);
//        miLayerCurves.setChecked(layer.filter == Layer.Filter.CURVES);
//        miLayerDrawBelow.setChecked(layer.drawBelow);
//        miLayerFilterSet.setEnabled(layer.filter != null);
//        miLayerHsv.setChecked(layer.filter == Layer.Filter.HSV);
//        miLayerLevelUp.setEnabled(layer.getLevel() > 0);
//        miLayerReference.setChecked(layer.reference);
//        checkLayerBlendModeMenuItem(layer.paint.getBlendMode());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layer, parent, false);
        return new ViewHolder(item);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener, OnItemSelectedListener onItemReselectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        this.onItemReselectedListener = onItemReselectedListener;
    }

    /**
     * @param listener the callback to call on checked state of {@link ViewHolder#cbVisible} change
     */
    public void setOnLayerVisibleChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        ovcbccListener = listener;
        isOnVisibleChangedListenerEnabled = true;
    }
}
