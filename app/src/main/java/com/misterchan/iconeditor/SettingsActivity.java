package com.misterchan.iconeditor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        settings = ((MainApplication) getApplicationContext()).getSettings();
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener((sharedPreferences, key) ->
                        settings.update(sharedPreferences, key));
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final Context context = getContext();
            String versionName = null;
            final EditTextPreference etpHms = findPreference(Settings.KEY_HMS);

            try {
                versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
            }
            findPreference(Settings.KEY_CFU).setSummary(versionName);

            etpHms.setOnBindEditTextListener(editText -> {
                editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            });
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final String key = preference.getKey();
            if (key != null) {
                switch (key) {
                    case Settings.KEY_CFU -> {
                        new AlertDialog.Builder(getContext())
                                .setMessage(R.string.check_for_updates_in_system_browser)
                                .setPositiveButton(R.string.ok, (dialog, which) ->
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MISTER-CHAN/icon-editor/releases"))))
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                }
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}