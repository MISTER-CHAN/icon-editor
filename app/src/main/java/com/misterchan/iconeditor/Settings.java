package com.misterchan.iconeditor;

import android.content.SharedPreferences;

import com.misterchan.iconeditor.ui.MainActivity;

public class Settings {
    public static final Settings INST = new Settings();

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    private static final String KEY_ASHA = "asha"; // Automatically Set Has Alpha
    public static final String KEY_CFU = "cfu"; // Check for Updates
    public static final String KEY_CIR = "cir"; // Component Radix of Color Int
    public static final String KEY_CP = "cp"; // Color Picker
    public static final String KEY_CR = "cr"; // Color Representation
    private static final String KEY_FB = "fb"; // Filter Bitmap
    public static final String KEY_FL = "fl"; // Frame List
    public static final String KEY_HMS = "hms"; // Max Size of History
    public static final String KEY_LOC = "loc"; // Locale
    private static final String KEY_MT = "mt"; // Multithreaded
    private static final String KEY_NLL = "nll"; // Level of New Layer
    public static final String KEY_THEME = "theme"; // Theme

    private boolean autoSetHasAlpha = false;
    private boolean colorRep = false;
    private int historyMaxSize = 50;
    private boolean newLayerLevel = false;
    private int colorIntCompRadix = 16;
    private int colorPicker = 0;
    public MainActivity mainActivity;
    private SharedPreferences preferences;
    private String colorIntCompFormat = FORMAT_02X;

    private Settings() {
    }

    public boolean autoSetHasAlpha() {
        return autoSetHasAlpha;
    }

    public String colorIntCompFormat() {
        return colorIntCompFormat;
    }

    public int colorIntCompRadix() {
        return colorIntCompRadix;
    }

    public int colorPicker() {
        return colorPicker;
    }

    public boolean colorRep() {
        return colorRep;
    }

    public int historyMaxSize() {
        return historyMaxSize;
    }

    public boolean newLayerLevel() {
        return newLayerLevel;
    }

    public SharedPreferences pref() {
        return preferences;
    }

    public void update(SharedPreferences preferences) {
        this.preferences = preferences;
        update(KEY_ASHA);
        update(KEY_CIR);
        update(KEY_CP);
        update(KEY_CR);
        update(KEY_FB);
        update(KEY_HMS);
        update(KEY_MT);
        update(KEY_NLL);
    }

    public void update(String key) {
        switch (key) {
            case KEY_ASHA -> autoSetHasAlpha = preferences.getBoolean(KEY_ASHA, false);
            case KEY_CIR -> {
                try {
                    colorIntCompRadix = preferences.getInt(KEY_CIR, 16);
                } catch (NumberFormatException e) {
                    colorIntCompRadix = 16;
                }
                colorIntCompFormat = colorIntCompRadix == 16 ? FORMAT_02X : FORMAT_D;
            }
            case KEY_CP -> colorPicker = preferences.getInt(KEY_CP, 0);
            case KEY_CR -> {
                colorRep = preferences.getBoolean(KEY_CR, false);
                mainActivity.setColorRep();
            }
            case KEY_FB -> mainActivity.setFilterBitmap(preferences.getBoolean(KEY_FB, false));
            case KEY_FL ->
                    mainActivity.setFrameListMenuItemVisible(preferences.getBoolean(KEY_FL, false));
            case KEY_HMS -> {
                try {
                    historyMaxSize = Integer.parseUnsignedInt(preferences.getString(KEY_HMS, "50"));
                } catch (NumberFormatException e) {
                    historyMaxSize = 50;
                }
            }
            case KEY_MT -> mainActivity.setRunnableRunner(preferences.getBoolean(KEY_MT, true));
            case KEY_NLL -> newLayerLevel = "s".equals(preferences.getString(KEY_NLL, "t"));
        }
    }
}
