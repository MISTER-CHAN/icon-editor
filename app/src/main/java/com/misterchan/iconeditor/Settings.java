package com.misterchan.iconeditor;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.misterchan.iconeditor.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

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
    public static final String KEY_MP = "mp"; // Media Picker
    private static final String KEY_MT = "mt"; // Multithreaded
    private static final String KEY_NIW = "niw", KEY_NIH = "nih"; // New Image Size
    private static final String KEY_PALETTE = "palette"; // Palette
    public static final String KEY_SCC = "scc"; // Show Current Color
    public static final String KEY_SG = "sg"; // Show Grid
    public static final String KEY_SR = "sr"; // Show Rulers
    public static final String KEY_THEME = "theme"; // Theme

    private boolean autoSetHasAlpha = false;
    private boolean colorRep = false;
    private boolean mediaPicker = true;
    private boolean showCurrentColor = false;
    private boolean showGrid = true;
    private boolean showRulers = true;
    private int colorIntCompRadix = 16;
    private int colorPicker = 0;
    private int historyMaxSize = 50;
    private int newImageWidth = 48, newImageHeight = 48;
    private List<Long> palette;
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

    public boolean mediaPicker() {
        return mediaPicker;
    }

    public int newImageWidth() {
        return newImageWidth;
    }

    public int newImageHeight() {
        return newImageHeight;
    }

    public List<Long> palette() {
        return palette;
    }

    public SharedPreferences pref() {
        return preferences;
    }

    public boolean showCurrentColor() {
        return showCurrentColor;
    }

    public boolean showGrid() {
        return showGrid;
    }

    public boolean showRulers() {
        return showRulers;
    }

    private void loadPalette() {
        String str = preferences.getString(KEY_PALETTE,
                "\uFF00\u0000\u0000\u0000" + "\uFFFF\uFFFF\u0000\u0000" +
                        "\uFFFF\u0000\u0000\u0000" + "\uFFFF\uFF00\u0000\u0000" +
                        "\uFF00\uFF00\u0000\u0000" + "\uFF00\uFFFF\u0000\u0000" +
                        "\uFF00\u00FF\u0000\u0000" + "\uFFFF\u00FF\u0000\u0000");

        List<Long> palette = new ArrayList<>() {
            @Override
            public int indexOf(@Nullable Object o) {
                for (int i = 0; i < size(); i++) if (o == get(i)) return i;
                return -1;
            }
        };

        for (int i = 0; i < str.length(); i += 4) {
            palette.add((long) str.charAt(i) << 0x30 | (long) str.charAt(i + 1) << 0x20 | (long) str.charAt(i + 2) << 0x10 | (long) str.charAt(i + 3));
        }

        this.palette = palette;
    }

    public void savePalette(List<Long> palette) {
        this.palette = palette;

        StringBuilder builder = new StringBuilder();
        for (long color : palette) {
            builder.append((char) (color >>> 0x30)).append((char) (color >>> 0x20)).append((char) (color >>> 0x10)).append((char) color);
        }
        preferences.edit().putString(KEY_PALETTE, builder.toString()).apply();
    }

    public void setNewImageSize(int width, int height) {
        preferences.edit().putInt(KEY_NIW, width).putInt(KEY_NIH, height).apply();
        newImageWidth = width;
        newImageHeight = height;
    }

    public void update(SharedPreferences preferences) {
        this.preferences = preferences;
        update(KEY_ASHA);
        update(KEY_CIR);
        update(KEY_CP);
        update(KEY_CR);
        update(KEY_FB);
        update(KEY_HMS);
        update(KEY_MP);
        update(KEY_MT);
        update(KEY_NIW);
        update(KEY_NIH);
        update(KEY_PALETTE);
        update(KEY_SCC);
        update(KEY_SG);
        update(KEY_SR);
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
            case KEY_MP -> mediaPicker = "m".equals(preferences.getString(KEY_MP, "m"));
            case KEY_MT -> mainActivity.setRunnableRunner(preferences.getBoolean(KEY_MT, true));
            case KEY_NIW -> newImageWidth = preferences.getInt(KEY_NIW, 48);
            case KEY_NIH -> newImageHeight = preferences.getInt(KEY_NIH, 48);
            case KEY_PALETTE -> loadPalette();
            case KEY_SCC -> showCurrentColor = preferences.getBoolean(KEY_SCC, false);
            case KEY_SG -> showGrid = preferences.getBoolean(KEY_SG, true);
            case KEY_SR -> showRulers = preferences.getBoolean(KEY_SR, true);
        }
    }
}
