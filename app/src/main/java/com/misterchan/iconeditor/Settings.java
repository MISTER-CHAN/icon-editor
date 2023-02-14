package com.misterchan.iconeditor;

import android.content.SharedPreferences;

class Settings {

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    static final String KEY_ACR = "acr"; // ARGB Color Int Component Radix
    static final String KEY_ACT = "act"; // ARGB Color Type
    static final String KEY_CFU = "cfu"; // Check for Updates
    static final String KEY_FB = "fb"; // Filter Bitmap
    static final String KEY_ITS = "its"; // Independent Translation and Scale
    static final String KEY_LOC = "loc"; // Locale
    static final String KEY_MT = "mt"; // Multithreaded
    static final String KEY_NLL = "nll"; // New Layer Level

    private boolean argbColorType = false;
    private boolean independentTranslAndScale = false;
    private boolean multithreaded = true;
    private boolean newLayerLevel = false;
    private int argbComponentRadix = 16;
    private MainActivity mainActivity;
    private String argbComponentFormat = FORMAT_02X;

    public boolean getArgbColorType() {
        return argbColorType;
    }

    public String getArgbComponentFormat() {
        return argbComponentFormat;
    }

    public int getArgbComponentRadix() {
        return argbComponentRadix;
    }

    public boolean getIndependentTranslAndScale() {
        return independentTranslAndScale;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public boolean getNewLayerLevel() {
        return newLayerLevel;
    }

    public void update(SharedPreferences preferences) {
        update(preferences, KEY_ACR);
        update(preferences, KEY_ACT);
        update(preferences, KEY_FB);
        update(preferences, KEY_ITS);
        update(preferences, KEY_MT);
        update(preferences, KEY_NLL);
    }

    public void update(SharedPreferences preferences, String key) {
        switch (key) {
            case KEY_ACR:
                try {
                    argbComponentRadix = Integer.parseUnsignedInt(preferences.getString(KEY_ACR, "16"));
                } catch (NumberFormatException e) {
                    argbComponentRadix = 16;
                }
                argbComponentFormat = argbComponentRadix == 16 ? FORMAT_02X : FORMAT_D;
                break;

            case KEY_ACT:
                argbColorType = Boolean.parseBoolean(preferences.getString(KEY_ACT, "false"));
                mainActivity.setArgbColorType();
                break;

            case KEY_FB:
                mainActivity.setFilterBitmap(preferences.getBoolean(KEY_FB, false));
                break;

            case KEY_ITS:
                independentTranslAndScale = preferences.getBoolean(KEY_ITS, false);
                break;

            case KEY_LOC:
                break;

            case KEY_MT:
                multithreaded = preferences.getBoolean(KEY_MT, true);
                mainActivity.setRunnableRunner(multithreaded);
                break;

            case KEY_NLL:
                newLayerLevel = Boolean.parseBoolean(preferences.getString(KEY_NLL, "false"));
                break;
        }
    }
}
