package com.misterchan.iconeditor;

import android.content.SharedPreferences;

import com.misterchan.iconeditor.ui.MainActivity;

public class Settings {
    public static final Settings INST = new Settings();

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    private static final String KEY_ASHA = "asha"; // Automatically Set Has Alpha
    public static final String KEY_CFU = "cfu"; // Check for Updates
    public static final String KEY_CIR = "cir"; // Color Int Component Radix
    public static final String KEY_CR = "cr"; // Color Representation
    private static final String KEY_FB = "fb"; // Filter Bitmap
    public static final String KEY_FL = "fl"; // Frame List
    public static final String KEY_HMS = "hms"; // History Max Size
    public static final String KEY_LOC = "loc"; // Locale
    private static final String KEY_MT = "mt"; // Multithreaded
    private static final String KEY_NLL = "nll"; // New Layer Level
    public static final String KEY_PIH = "pih"; // Pick in HSV

    private boolean autoSetHasAlpha = false;
    private boolean colorRep = false;
    private int historyMaxSize = 50;
    private boolean newLayerLevel = false;
    private boolean pickInHsv = false;
    private int colorIntCompRadix = 16;
    public MainActivity mainActivity;
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

    public boolean colorRep() {
        return colorRep;
    }

    public int historyMaxSize() {
        return historyMaxSize;
    }

    public boolean newLayerLevel() {
        return newLayerLevel;
    }

    public boolean pickInHsv() {
        return pickInHsv;
    }

    public void update(SharedPreferences preferences) {
        update(preferences, KEY_CIR);
        update(preferences, KEY_CR);
        update(preferences, KEY_ASHA);
        update(preferences, KEY_FB);
        update(preferences, KEY_HMS);
        update(preferences, KEY_MT);
        update(preferences, KEY_NLL);
        update(preferences, KEY_PIH);
    }

    public void update(SharedPreferences preferences, String key) {
        switch (key) {
            case KEY_ASHA -> autoSetHasAlpha = preferences.getBoolean(KEY_ASHA, false);
            case KEY_CIR -> {
                try {
                    colorIntCompRadix = Integer.parseUnsignedInt(preferences.getString(KEY_CIR, "16"));
                } catch (NumberFormatException e) {
                    colorIntCompRadix = 16;
                }
                colorIntCompFormat = colorIntCompRadix == 16 ? FORMAT_02X : FORMAT_D;
            }
            case KEY_CR -> {
                colorRep = "l".equals(preferences.getString(KEY_CR, "i"));
                mainActivity.setArgbColorType();
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
            case KEY_NLL -> newLayerLevel = "sel".equals(preferences.getString(KEY_NLL, "top"));
            case KEY_PIH -> pickInHsv = preferences.getBoolean(KEY_PIH, false);
        }
    }
}
