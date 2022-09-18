package com.misterchan.iconeditor;

import android.app.Application;

public class MainApplication extends Application {
    private final Settings settings = new Settings();

    public Settings getSettings() {
        return settings;
    }
}
