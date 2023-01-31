package com.misterchan.iconeditor;

import android.content.SharedPreferences;

class Settings {

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    static final String KEY_ACR = "acr";
    static final String KEY_ACT = "act";
    static final String KEY_NLL = "nll";
    static final String KEY_CFU = "cfu";
    static final String KEY_ITS = "its";
    static final String KEY_LOC = "loc";
    static final String KEY_MT = "mt";

    private boolean argbColorType = false;
    private boolean independentTranslAndScale = false;
    private boolean multithreaded = false;
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

            case KEY_ITS:
                independentTranslAndScale = preferences.getBoolean(KEY_ITS, false);
                break;

            case KEY_LOC:
                break;

            case KEY_MT:
                multithreaded = preferences.getBoolean(KEY_MT, false);
                mainActivity.setRunnableRunner(multithreaded);
                break;

            case KEY_NLL:
                newLayerLevel = Boolean.parseBoolean(preferences.getString(KEY_NLL, "false"));
                break;
        }
    }
}
