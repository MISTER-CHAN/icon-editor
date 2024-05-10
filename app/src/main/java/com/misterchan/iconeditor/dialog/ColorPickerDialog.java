package com.misterchan.iconeditor.dialog;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.colorpicker.CmykColorPicker;
import com.misterchan.iconeditor.colorpicker.ColorPicker;
import com.misterchan.iconeditor.colorpicker.HslColorPicker;
import com.misterchan.iconeditor.colorpicker.HsvColorPicker;
import com.misterchan.iconeditor.colorpicker.LabColorPicker;
import com.misterchan.iconeditor.colorpicker.RgbColorPicker;
import com.misterchan.iconeditor.colorpicker.XyYColorPicker;
import com.misterchan.iconeditor.colorpicker.XyzColorPicker;
import com.misterchan.iconeditor.colorpicker.YuvColorPicker;
import com.misterchan.iconeditor.databinding.ColorPickerHsvBinding;
import com.misterchan.iconeditor.databinding.ColorPickerRgbBinding;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorPickerDialog {
    private static final KeyListener KEY_LISTENER_HEX = DigitsKeyListener.getInstance("0123456789ABCDEFabcdef");

    public interface OnColorPickListener {
        void onPick(Long oldColor, Long newColor);
    }

    private static class RgbHexActionModeCallback implements ActionMode.Callback {
        private static final Pattern PATTERN_ARGB_4444 =
                Pattern.compile("(?<a>[0-9A-Fa-f])?(?<r>[0-9A-Fa-f])(?<g>[0-9A-Fa-f])(?<b>[0-9A-Fa-f])$");

        private static final Pattern PATTERN_ARGB_8888 =
                Pattern.compile("(?<a>[0-9A-Fa-f]{2})?(?<r>[0-9A-Fa-f]{2})(?<g>[0-9A-Fa-f]{2})(?<b>[0-9A-Fa-f]{2})$");

        private final boolean hasAlpha;
        private final ClipboardManager clipboard;
        private final ColorPickerDialog dialog;
        private String hexFromViews;
        private String[] hexFromClip;

        public RgbHexActionModeCallback(ColorPickerDialog dialog, boolean hasAlpha) {
            this.dialog = dialog;
            clipboard = (ClipboardManager) dialog.context.getSystemService(Context.CLIPBOARD_SERVICE);
            this.hasAlpha = hasAlpha;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (!(dialog.colorPicker instanceof RgbColorPicker) || dialog.colorPicker.prop().compBase10()) {
                return true;
            }

            copy:
            {
                StringBuilder sb = new StringBuilder();
                boolean hasAlpha = this.hasAlpha && dialog.tietAlpha.getText().length() == 2;
                if (hasAlpha) sb.append(dialog.tietAlpha.getText());
                if (dialog.tietComp0.getText().length() != 2) break copy;
                sb.append(dialog.tietComp0.getText());
                if (dialog.tietComp1.getText().length() != 2) break copy;
                sb.append(dialog.tietComp1.getText());
                if (dialog.tietComp2.getText().length() != 2) break copy;
                sb.append(dialog.tietComp2.getText());
                hexFromViews = sb.toString();

                int itemId = hasAlpha ? R.id.i_copy_argb : R.id.i_copy_rgb;
                @StringRes int titleRes = hasAlpha ? R.string.copy_argb : R.string.copy_rgb;
                menu.add(Menu.NONE, itemId, Menu.NONE, titleRes);
            }

            paste:
            {
                if (!clipboard.hasPrimaryClip()) break paste;
                CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
                if (text == null) break paste;
                Matcher m = null;
                int countToRep = 0;
                if (text.length() >= 6) {
                    m = PATTERN_ARGB_8888.matcher(text);
                    if (m.find()) countToRep = 1;
                }
                if (countToRep == 0 && text.length() >= 3) {
                    m = PATTERN_ARGB_4444.matcher(text);
                    if (m.find()) countToRep = 2;
                }
                if (countToRep == 0) break paste;
                String a = hasAlpha ? m.group("a") : null;
                boolean hasAlpha = a != null;
                hexFromClip = new String[]{
                        a != null ? a.repeat(countToRep) : null,
                        m.group("r").repeat(countToRep), m.group("g").repeat(countToRep), m.group("b").repeat(countToRep)
                };

                int itemId = hasAlpha ? R.id.i_paste_argb : R.id.i_paste_rgb;
                @StringRes int titleRes = hasAlpha ? R.string.paste_argb : R.string.paste_rgb;
                menu.add(Menu.NONE, itemId, Menu.NONE, titleRes);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        @SuppressLint("NonConstantResourceId")
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                default:
                    return false;

                case R.id.i_copy_argb:
                    if (!(dialog.colorPicker instanceof RgbColorPicker)) break;

                case R.id.i_copy_rgb:
                    clipboard.setPrimaryClip(ClipData.newPlainText("RGB Hexadecimal", hexFromViews));
                    break;

                case R.id.i_paste_argb:
                    if (!(dialog.colorPicker instanceof RgbColorPicker)) break;
                    dialog.tietAlpha.setText(hexFromClip[0]);

                case R.id.i_paste_rgb:
                    if (!(dialog.colorPicker instanceof RgbColorPicker)) break;
                    dialog.tietComp0.setText(hexFromClip[1]);
                    dialog.tietComp1.setText(hexFromClip[2]);
                    dialog.tietComp2.setText(hexFromClip[3]);
                    break;
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }

    private final AlertDialog dialog;
    private boolean enabled;
    private ColorPicker colorPicker;
    private final Context context;
    private final LayoutInflater layoutInflater;
    private LinearLayout llExtraViews;
    private final long oldColor;
    private Slider sAlpha, sComp0, sComp1, sComp2, sComp3;
    private TextInputEditText tietAlpha, tietComp0, tietComp1, tietComp2, tietComp3;
    private TextInputLayout tilComp0, tilComp1, tilComp2, tilComp3;
    private View vPreviewColor;

    private final TabLayout.OnTabSelectedListener onColorSpaceTLTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int pos = tab.getPosition();
            if (colorPicker != null) {
                Settings.INST.pref().edit().putInt(Settings.KEY_CP, pos).apply();
                Settings.INST.update(Settings.KEY_CP);
            }
            changeColorPicker(pos);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    public ColorPickerDialog(Context context, @StringRes int titleId,
                             OnColorPickListener onColorPickListener, @ColorLong Long oldColor) {
        this(context, titleId, onColorPickListener, oldColor, 0);
    }

    public ColorPickerDialog(Context context, @StringRes int titleId,
                             OnColorPickListener onColorPickListener,
                             @ColorLong Long oldColor, @StringRes int neutralFunction) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, colorPicker.color()))
                .setTitle(titleId)
                .setView(R.layout.color_picker);

        if (oldColor != null) {
            this.oldColor = oldColor;
            if (neutralFunction != 0) {
                dialogBuilder.setNeutralButton(neutralFunction, (dialog, which) -> {
                    switch (neutralFunction) {
                        case R.string.swap -> onColorPickListener.onPick(null, colorPicker.color());
                        case R.string.delete -> onColorPickListener.onPick(oldColor, null);
                    }
                });
            }
        } else {
            this.oldColor = Color.BLACK;
        }

        dialog = dialogBuilder.create();
    }

    private void changeColorPicker(int pos) {
        changeColorPicker(pos, true);
    }

    private void changeColorPicker(int pos, boolean changeViews) {
        enabled = false;
        if (changeViews) {
            if (colorPicker != null) colorPicker.dismiss();
            llExtraViews.removeAllViews();
        }

        long color = colorPicker != null ? colorPicker.color() : oldColor;
        colorPicker = switch (pos) {
            default -> {
                ColorPickerRgbBinding binding = null;
                if (changeViews) {
                    binding = ColorPickerRgbBinding.inflate(layoutInflater, llExtraViews, false);
                    llExtraViews.addView(binding.getRoot());
                } else if (colorPicker instanceof RgbColorPicker oldCp) {
                    binding = oldCp.binding;
                }
                yield new RgbColorPicker(color, binding, changeViews ? () -> {
                    onColorRepChanged();
                    changeColorPicker(0, false);
                    float a = Color.alpha(colorPicker.color());
                    tietAlpha.setText(alphaToString(Settings.INST.colorRep() ? a : a * 0xFF));
                } : null);
            }
            case 1 -> {
                var binding = ColorPickerHsvBinding.inflate(layoutInflater, llExtraViews, false);
                llExtraViews.addView(binding.getRoot());
                yield new HsvColorPicker(color, binding, h -> tietComp0.setText(String.valueOf(h)), (s, v) -> {
                    tietComp1.setText(String.valueOf(s));
                    tietComp2.setText(String.valueOf(v));
                });
            }
            case 2 -> new HslColorPicker(color);
            case 3 -> new CmykColorPicker(color);
            case 4 -> new YuvColorPicker(color);
            case 5 -> new LabColorPicker(color);
            case 6 -> new XyzColorPicker(color);
            case 7 -> new XyYColorPicker(color);
        };

        tilComp0.setHint(colorPicker.prop().c0());
        tilComp1.setHint(colorPicker.prop().c1());
        tilComp2.setHint(colorPicker.prop().c2());
        tilComp0.setSuffixText(colorPicker.prop().c0Suffix());
        tilComp1.setSuffixText(colorPicker.prop().c1Suffix());
        tilComp2.setSuffixText(colorPicker.prop().c2Suffix());

        sComp0.setValueFrom(colorPicker.prop().c0Min());
        sComp0.setValueTo(colorPicker.prop().c0Max());
        sComp1.setValueFrom(colorPicker.prop().c1Min());
        sComp1.setValueTo(colorPicker.prop().c1Max());
        sComp2.setValueFrom(colorPicker.prop().c2Min());
        sComp2.setValueTo(colorPicker.prop().c2Max());

        sComp0.setStepSize(colorPicker.prop().colorRep() ? 0.0f : 1.0f);
        sComp1.setStepSize(colorPicker.prop().colorRep() ? 0.0f : 1.0f);
        sComp2.setStepSize(colorPicker.prop().colorRep() ? 0.0f : 1.0f);
        if (colorPicker.prop().compBase10()) {
            tietComp0.setInputType(colorPicker.prop().c0InputType());
            tietComp1.setInputType(colorPicker.prop().c1InputType());
            tietComp2.setInputType(colorPicker.prop().c2InputType());
        } else {
            tietComp0.setKeyListener(KEY_LISTENER_HEX);
            tietComp1.setKeyListener(KEY_LISTENER_HEX);
            tietComp2.setKeyListener(KEY_LISTENER_HEX);
        }

        float c0v = colorPicker.getComponent(0), c1v = colorPicker.getComponent(1), c2v = colorPicker.getComponent(2);
        sComp0.setValue(c0v);
        sComp1.setValue(c1v);
        sComp2.setValue(c2v);
        tietComp0.setText(compToString(c0v));
        tietComp1.setText(compToString(c1v));
        tietComp2.setText(compToString(c2v));
        sComp3.setVisibility(colorPicker.prop().compCount() ? View.VISIBLE : View.GONE);
        tilComp3.setVisibility(colorPicker.prop().compCount() ? View.VISIBLE : View.GONE);
        if (colorPicker.prop().compCount()) {
            float c3v = colorPicker.getComponent(3);
            sComp3.setValue(c3v);
            tietComp3.setText(String.valueOf(c3v));
        }

        enabled = true;
    }

    private CharSequence alphaToString(float f) {
        return Settings.INST.colorRep() ? String.valueOf(f) : String.format(Settings.INST.colorIntCompFormat(), (int) f);
    }

    private CharSequence compToString(float f) {
        return colorPicker.prop().colorRep() ? String.valueOf(f) : String.format(Settings.INST.colorIntCompFormat(), (int) f);
    }

    private void onAlphaChanged(String s) {
        float f;
        try {
            f = Settings.INST.colorRep() ? Float.parseFloat(s) : Integer.parseUnsignedInt(s, Settings.INST.colorIntCompRadix());
        } catch (NumberFormatException e) {
            return;
        }
        if (!(sAlpha.getValueFrom() <= f && f <= sAlpha.getValueTo())) return;
        sAlpha.setValue(f);
        colorPicker.setAlpha(f);
        vPreviewColor.setBackgroundColor(colorPicker.colorInt());
    }

    private void onComponentChanged(int index, String s, Slider slider) {
        if (!enabled) return;

        float f;
        try {
            f = colorPicker.prop().colorRep() ? Float.parseFloat(s) : Integer.parseUnsignedInt(s, Settings.INST.colorIntCompRadix());
        } catch (NumberFormatException e) {
            return;
        }
        if (index == 0 && colorPicker.prop().c0Circular()) {
            f %= colorPicker.prop().c0Max();
        } else {
            if (!(slider.getValueFrom() <= f && f <= slider.getValueTo())) return;
        }
        slider.setValue(f);
        colorPicker.setComponent(index, f);
        vPreviewColor.setBackgroundColor(colorPicker.colorInt());
    }

    private void onColorRepChanged() {
        boolean colorRep = Settings.INST.colorRep();

        sAlpha.setStepSize(colorRep ? 0.0f : 1.0f);
        sAlpha.setValueFrom(colorRep ? 0.0f : 0x00);
        sAlpha.setValueTo(colorRep ? 1.0f : 0xFF);

        if (colorRep) {
            tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC);
        } else if (Settings.INST.colorIntCompRadix() == 10) {
            tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM);
        } else {
            tietAlpha.setKeyListener(KEY_LISTENER_HEX);
        }
    }

    public void show() {
        dialog.show();

        int oldColorInt = Color.toArgb(oldColor);
        llExtraViews = dialog.findViewById(R.id.ll_extra_views);
        sAlpha = dialog.findViewById(R.id.s_alpha);
        sComp0 = dialog.findViewById(R.id.s_comp_0);
        sComp1 = dialog.findViewById(R.id.s_comp_1);
        sComp2 = dialog.findViewById(R.id.s_comp_2);
        sComp3 = dialog.findViewById(R.id.s_comp_3);
        TabLayout tlColorPickers = dialog.findViewById(R.id.color_pickers);
        tietAlpha = dialog.findViewById(R.id.tiet_alpha);
        tilComp0 = dialog.findViewById(R.id.til_comp_0);
        tilComp1 = dialog.findViewById(R.id.til_comp_1);
        tilComp2 = dialog.findViewById(R.id.til_comp_2);
        tilComp3 = dialog.findViewById(R.id.til_comp_3);
        tietComp0 = (TextInputEditText) tilComp0.getEditText();
        tietComp1 = (TextInputEditText) tilComp1.getEditText();
        tietComp2 = (TextInputEditText) tilComp2.getEditText();
        tietComp3 = (TextInputEditText) tilComp3.getEditText();
        View vCurrent = dialog.findViewById(R.id.item_color_current);
        View vCurrentColor = vCurrent.findViewById(R.id.v_color);
        vPreviewColor = dialog.findViewById(R.id.item_color).findViewById(R.id.v_color);

        View.OnClickListener onPreviewClickListener = v -> {
            final PopupMenu popupMenu = new PopupMenu(dialog.getContext(), v);
            final Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.color_picker, menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                boolean checked = !item.isChecked();
                vCurrent.setVisibility(checked ? View.VISIBLE : View.GONE);
                Settings.INST.pref().edit().putBoolean(Settings.KEY_SCC, checked).apply();
                Settings.INST.update(Settings.KEY_SCC);
                return true;
            });
            popupMenu.show();
            menu.findItem(R.id.i_show_current).setChecked(Settings.INST.showCurrentColor());
        };

        vCurrent.setVisibility(Settings.INST.showCurrentColor() ? View.VISIBLE : View.GONE);
        vCurrentColor.setBackgroundColor(oldColorInt);
        vPreviewColor.setBackgroundColor(oldColorInt);

        onColorRepChanged();
        {
            float a = Color.alpha(oldColor), av = Settings.INST.colorRep() ? a : a * 0xFF;
            sAlpha.setValue(av);
            tietAlpha.setText(alphaToString(av));
        }

        sAlpha.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietAlpha.setText(alphaToString(value)));
        sComp0.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietComp0.setText(compToString(value)));
        sComp1.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietComp1.setText(compToString(value)));
        sComp2.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietComp2.setText(compToString(value)));
        sComp3.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietComp3.setText(String.valueOf(value)));

        tietAlpha.addTextChangedListener((AfterTextChangedListener) this::onAlphaChanged);
        tietComp0.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sComp0));
        tietComp1.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sComp1));
        tietComp2.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sComp2));
        tietComp3.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(3, s, sComp3));

        ActionMode.Callback argbHexActionModeCallback = new RgbHexActionModeCallback(this, true);
        ActionMode.Callback rgbHexActionModeCallback = new RgbHexActionModeCallback(this, false);
        tietAlpha.setCustomInsertionActionModeCallback(argbHexActionModeCallback);
        tietAlpha.setCustomSelectionActionModeCallback(argbHexActionModeCallback);
        tietComp0.setCustomInsertionActionModeCallback(rgbHexActionModeCallback);
        tietComp0.setCustomSelectionActionModeCallback(rgbHexActionModeCallback);
        tietComp1.setCustomInsertionActionModeCallback(rgbHexActionModeCallback);
        tietComp1.setCustomSelectionActionModeCallback(rgbHexActionModeCallback);
        tietComp2.setCustomInsertionActionModeCallback(rgbHexActionModeCallback);
        tietComp2.setCustomSelectionActionModeCallback(rgbHexActionModeCallback);

        tlColorPickers.addOnTabSelectedListener(onColorSpaceTLTabSelectedListener);
        {
            int colorPickerPos = Settings.INST.colorPicker();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    && (colorPickerPos != 0 && colorPickerPos != 1)) {
                llExtraViews.setVisibility(View.GONE);
            }
            if (tlColorPickers.getSelectedTabPosition() != colorPickerPos) {
                tlColorPickers.getTabAt(colorPickerPos).select();
            } else {
                onColorSpaceTLTabSelectedListener.onTabSelected(tlColorPickers.getTabAt(colorPickerPos));
            }
        }

        vCurrentColor.setOnClickListener(onPreviewClickListener);
        vPreviewColor.setOnClickListener(onPreviewClickListener);
    }
}
