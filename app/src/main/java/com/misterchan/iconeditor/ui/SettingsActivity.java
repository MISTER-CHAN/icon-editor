package com.misterchan.iconeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ext.SdkExtensions;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if ("s".equals(preferences.getString(Settings.KEY_THEME, "d"))) {
            setTheme(R.style.Theme_IconEditor_SquareCorner);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> Settings.INST.update(key));
        Settings.applyDisplayCutouts(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.topMargin = insets.top;
            mlp.rightMargin = insets.right;
            mlp.bottomMargin = insets.bottom;
            v.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final Context context = getContext();
            String versionName = null;

            try {
                versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
            }
            findPreference(Settings.KEY_CFU).setSummary(versionName);

            ((EditTextPreference) findPreference(Settings.KEY_HMS)).setOnBindEditTextListener(editText -> {
                editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            });

            if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) < 2) {
                findPreference(Settings.KEY_MP).setVisible(false);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final String key = preference.getKey();
            if (Settings.KEY_CFU.equals(key)) {
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(R.string.check_for_updates_in_system_browser)
                        .setPositiveButton(R.string.ok, (dialog, which) ->
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MISTER-CHAN/icon-editor/releases"))))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}