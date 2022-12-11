package com.misterchan.iconeditor;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    static final String KEY_ACR = "acr";
    static final String KEY_CFU = "cfu";
    static final String KEY_ITS = "its";
    static final String KEY_LOC = "loc";
    static final String KEY_MT = "mt";

    private boolean independentTranslAndScale = false;
    private boolean multithreaded = false;
    private int argbChannelsRadix = 16;
    private String argbChannelsFormat = FORMAT_02X;

    public int getArgbChannelsRadix() {
        return argbChannelsRadix;
    }

    public String getArgbChannelsFormat() {
        return argbChannelsFormat;
    }

    public boolean getIndependentTranslAndScale() {
        return independentTranslAndScale;
    }

    public boolean getMultithreaded() {
        return multithreaded;
    }

    public void update(Context context, SharedPreferences preferences) {
        update(context, preferences, KEY_ACR);
        update(context, preferences, KEY_ITS);
        update(context, preferences, KEY_MT);
    }

    public void update(Context context, SharedPreferences preferences, String key) {
        switch (key) {
            case KEY_ACR:
                try {
                    argbChannelsRadix = Integer.parseUnsignedInt(preferences.getString(KEY_ACR, "16"));
                } catch (NumberFormatException e) {
                    argbChannelsRadix = 16;
                }
                argbChannelsFormat = argbChannelsRadix == 16 ? FORMAT_02X : FORMAT_D;
                break;

            case KEY_ITS:
                independentTranslAndScale = preferences.getBoolean(KEY_ITS, false);
                break;

            case KEY_LOC:
                break;

            case KEY_MT:
                multithreaded = preferences.getBoolean(KEY_MT, false);
                break;
        }
    }
}
