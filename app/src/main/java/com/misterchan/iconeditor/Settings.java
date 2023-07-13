package com.misterchan.iconeditor;

import android.content.SharedPreferences;

public class Settings {
    public static final Settings INST = new Settings();

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    static final String KEY_ACR = "acr"; // ARGB Color Int Component Radix
    static final String KEY_ACT = "act"; // ARGB Color Type
    private static final String KEY_ASHA = "asha"; // Automatically Set Has Alpha
    static final String KEY_CFU = "cfu"; // Check for Updates
    private static final String KEY_FB = "fb"; // Filter Bitmap
    static final String KEY_FL = "fl"; // Frame List
    static final String KEY_HMS = "hms"; // History Max Size
    static final String KEY_LOC = "loc"; // Locale
    private static final String KEY_MT = "mt"; // Multithreaded
    private static final String KEY_NLL = "nll"; // New Layer Level
    static final String KEY_PIH = "pih"; // Pick in HSV

    private boolean argbColorType = false;
    private boolean autoSetHasAlpha = false;
    private int historyMaxSize = 50;
    private boolean newLayerLevel = false;
    private boolean pickInHsv = false;
    private int argbCompRadix = 16;
    public MainActivity mainActivity;
    private String argbCompFormat = FORMAT_02X;

    private Settings() {
    }

    public boolean argbColorType() {
        return argbColorType;
    }

    public String argbCompFormat() {
        return argbCompFormat;
    }

    public int argbCompRadix() {
        return argbCompRadix;
    }

    public boolean autoSetHasAlpha() {
        return autoSetHasAlpha;
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
        update(preferences, KEY_ACR);
        update(preferences, KEY_ACT);
        update(preferences, KEY_ASHA);
        update(preferences, KEY_FB);
        update(preferences, KEY_HMS);
        update(preferences, KEY_MT);
        update(preferences, KEY_NLL);
        update(preferences, KEY_PIH);
    }

    public void update(SharedPreferences preferences, String key) {
        switch (key) {
            case KEY_ACR -> {
                try {
                    argbCompRadix = Integer.parseUnsignedInt(preferences.getString(KEY_ACR, "16"));
                } catch (NumberFormatException e) {
                    argbCompRadix = 16;
                }
                argbCompFormat = argbCompRadix == 16 ? FORMAT_02X : FORMAT_D;
            }
            case KEY_ACT -> {
                argbColorType = "l".equals(preferences.getString(KEY_ACT, "i"));
                mainActivity.setArgbColorType();
            }
            case KEY_ASHA -> autoSetHasAlpha = preferences.getBoolean(KEY_ASHA, false);
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
