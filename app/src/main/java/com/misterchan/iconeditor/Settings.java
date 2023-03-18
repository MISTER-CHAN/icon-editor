package com.misterchan.iconeditor;

import android.content.SharedPreferences;

public class Settings {
    private static final Settings INSTANCE = new Settings();

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    static final String KEY_ACR = "acr"; // ARGB Color Int Component Radix
    static final String KEY_ACT = "act"; // ARGB Color Type
    static final String KEY_CFU = "cfu"; // Check for Updates
    static final String KEY_FB = "fb"; // Filter Bitmap
    static final String KEY_HMS = "hms"; // History Max Size
    static final String KEY_ITS = "its"; // Independent Translation and Scale
    static final String KEY_LOC = "loc"; // Locale
    static final String KEY_MT = "mt"; // Multithreaded
    static final String KEY_NLL = "nll"; // New Layer Level

    private boolean argbColorType = false;
    private int historyMaxSize = 50;
    private boolean indTranslAndScale = false;
    private boolean newLayerLevel = false;
    private int argbCompRadix = 16;
    private MainActivity mainActivity;
    private String argbCompFormat = FORMAT_02X;

    private Settings() {
    }

    public static Settings getInstance() {
        return INSTANCE;
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

    public int historyMaxSize() {
        return historyMaxSize;
    }

    public boolean indTranslAndScale() {
        return indTranslAndScale;
    }

    public boolean newLayerLevel() {
        return newLayerLevel;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void update(SharedPreferences preferences) {
        update(preferences, KEY_ACR);
        update(preferences, KEY_ACT);
        update(preferences, KEY_FB);
        update(preferences, KEY_HMS);
        update(preferences, KEY_ITS);
        update(preferences, KEY_MT);
        update(preferences, KEY_NLL);
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
            case KEY_FB -> mainActivity.setFilterBitmap(preferences.getBoolean(KEY_FB, false));
            case KEY_HMS -> {
                try {
                    historyMaxSize = Integer.parseUnsignedInt(preferences.getString(KEY_HMS, "50"));
                } catch (NumberFormatException e) {
                    historyMaxSize = 50;
                }
            }
            case KEY_ITS -> indTranslAndScale = preferences.getBoolean(KEY_ITS, false);
            case KEY_MT -> mainActivity.setRunnableRunner(preferences.getBoolean(KEY_MT, true));
            case KEY_NLL -> newLayerLevel = "sel".equals(preferences.getString(KEY_NLL, "top"));
        }
    }
}
