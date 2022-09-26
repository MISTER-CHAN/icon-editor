package com.misterchan.iconeditor;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String FORMAT_02X = "%02X";
    private static final String FORMAT_D = "%d";

    private static final String KEY_ACR = "acr";
    private static final String KEY_LOC = "loc";
    private static final String KEY_ITS = "its";

    private boolean independentTranslAndScale = true;
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

    public void update(Context context, SharedPreferences preferences) {
        update(context, preferences, KEY_ACR);
        update(context, preferences, KEY_ITS);
    }

    public void update(Context context, SharedPreferences preferences, String key) {
        switch (key) {
            case KEY_ACR:
                if (preferences.contains(KEY_ACR)) {
                    try {
                        argbChannelsRadix = Integer.parseUnsignedInt(preferences.getString(KEY_ACR, "16"));
                    } catch (NumberFormatException e) {
                        argbChannelsRadix = 16;
                    }
                } else {
                    preferences.edit().putString(KEY_ACR, "16").apply();
                    argbChannelsRadix = 16;
                }
                argbChannelsFormat = argbChannelsRadix == 16 ? FORMAT_02X : FORMAT_D;
                break;

            case KEY_LOC:
                break;

            case KEY_ITS:
                independentTranslAndScale = preferences.getBoolean(KEY_ITS, true);
                break;
        }
    }
}
