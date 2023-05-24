package com.misterchan.iconeditor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.OneShotPreDrawListener;
import androidx.recyclerview.widget.RecyclerView;

import com.misterchan.iconeditor.listener.OnCBCheckedListener;

import java.util.List;

class LayerAdapter extends ItemMovableAdapter<LayerAdapter.ViewHolder> {

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbVisible;
        private final FrameLayout flThumbnail;
        private final ImageView ivThumbnail;
        private final LinearLayout llRoot, llParentBg, llFgLeaf;
        private final RadioButton rb;
        private final TextView tvName;
        private final View itemView;
        private final View vLowerLevel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            cbVisible = itemView.findViewById(R.id.cb_visible);
            flThumbnail = itemView.findViewById(R.id.fl_thumbnail);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            rb = itemView.findViewById(R.id.rb);
            tvName = itemView.findViewById(R.id.tv_name);
            llFgLeaf = itemView.findViewById(R.id.ll_fg_leaf);
            vLowerLevel = itemView.findViewById(R.id.v_lower_level);
            llParentBg = itemView.findViewById(R.id.ll_parent_bg);
            llRoot = itemView.findViewById(R.id.ll_root);
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

    public void notifyLayerSelectedChanged(int position, boolean selected) {
        if (recyclerView == null) {
            return;
        }
        final ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            return;
        }
        holder.rb.setOnCheckedChangeListener(null);
        holder.rb.setChecked(selected);
        holder.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
        holder.tvName.setTextColor(selected ? colorPrimary : textColorPrimary);
        holder.tvName.setTypeface(Typeface.defaultFromStyle(selected ? Typeface.BOLD : Typeface.NORMAL));
    }

    public void notifyLevelChanged() {
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

            holder.vLowerLevel.setVisibility(layer.getLevel() > 0 ? View.VISIBLE : View.GONE);
            holder.llRoot.removeAllViews();
            holder.llParentBg.removeAllViews();
            holder.llFgLeaf.removeAllViews();
            if (lastLayer == null) {
                for (int l = 0; l < layer.getLevel() - 1; ++l) {
                    final View v = LayoutInflater.from(context).inflate(R.layout.bracket, null);
                    v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_open));
                    holder.llRoot.addView(v);
                }
            } else {
                final int levelDiff = layer.getLevel() - lastLayer.getLevel();
                if (levelDiff > 0) {
                    for (int l = 0; l < (lastLayer.getLevel() > 0 ? levelDiff : levelDiff - 1); ++l) {
                        final View v = LayoutInflater.from(context).inflate(R.layout.bracket, null);
                        v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_open));
                        lastHolder.llParentBg.addView(v);
                    }
                } else if (levelDiff < 0) {
                    for (int l = 0; l < (layer.getLevel() > 0 ? -levelDiff : -levelDiff - 1); ++l) {
                        final View v = LayoutInflater.from(context).inflate(R.layout.bracket, null);
                        v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_close));
                        lastHolder.llFgLeaf.addView(v);
                    }
                }
            }
            lastLayer = layer;
            lastHolder = holder;
        }
        if (lastLayer != null) {
            for (int l = 0; l < lastLayer.getLevel() - 1; ++l) {
                final View v = LayoutInflater.from(context).inflate(R.layout.bracket, null);
                v.setBackground(AppCompatResources.getDrawable(context, R.drawable.np_bracket_close));
                lastHolder.llFgLeaf.addView(v);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
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
        holder.cbVisible.setOnCheckedChangeListener(null);
        holder.cbVisible.setChecked(layer.visible);
        holder.cbVisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layer.visible = isChecked;
            if (isOnVisibleChangedListenerEnabled) {
                ovcbccListener.onCheckedChanged(buttonView, isChecked);
            }
        });
        OneShotPreDrawListener.add(holder.flThumbnail, () -> {
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.flThumbnail.getLayoutParams();
            final int w = layer.bitmap.getWidth(), h = layer.bitmap.getHeight();
            lp.width = w >= h ? dim64Dip : dim64Dip * w / h;
            lp.height = w >= h ? dim64Dip * h / w : dim64Dip;
            holder.flThumbnail.setLayoutParams(lp);
        });
        holder.ivThumbnail.setImageBitmap(layer.bitmap);
        holder.rb.setOnCheckedChangeListener(null);
        holder.rb.setChecked(selected);
        holder.rb.setOnCheckedChangeListener((OnCBCheckedListener) buttonView ->
                onItemSelectedListener.onItemSelected(holder.itemView, position));
        holder.tvName.setText(layer.name);
        holder.tvName.setTextColor(selected ? colorPrimary : textColorPrimary);
        holder.tvName.setTypeface(Typeface.defaultFromStyle(selected ? Typeface.BOLD : Typeface.NORMAL));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layer, parent, false);
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

    /**
     * @param listener the callback to call on checked state of {@link ViewHolder#cbVisible} change
     */
    public void setOnLayerVisibleChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        ovcbccListener = listener;
        isOnVisibleChangedListenerEnabled = true;
    }
}
