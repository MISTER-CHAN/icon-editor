package com.misterchan.iconeditor;

import android.content.Context;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

public class Preferences {

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    private int argbChannelsRadix = 16;
    private String argbChannelsFormat = FORMAT_02X;

    public int getArgbChannelsRadix() {
        return argbChannelsRadix;
    }

    public String getArgbChannelsFormat() {
        return argbChannelsFormat;
    }

    private void setArgbChannelsRadix(boolean isChecked, int radix) {
        if (isChecked) {
            argbChannelsRadix = radix;
            argbChannelsFormat = radix == 16 ? FORMAT_02X : FORMAT_D;
        }
    }

    public void show(Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setView(R.layout.preferences)
                .setTitle(R.string.preferences)
                .show();

        RadioButton rbDec = dialog.findViewById(R.id.rb_dec);
        RadioButton rbHex = dialog.findViewById(R.id.rb_hex);

        rbDec.setChecked(argbChannelsRadix == 10);
        rbDec.setOnCheckedChangeListener((buttonView, isChecked) -> setArgbChannelsRadix(isChecked, 10));
        rbHex.setChecked(argbChannelsRadix == 16);
        rbHex.setOnCheckedChangeListener((buttonView, isChecked) -> setArgbChannelsRadix(isChecked, 16));
    }
}
