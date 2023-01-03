package com.misterchan.iconeditor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

            try {
                versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
            }

            findPreference(Settings.KEY_CFU).setSummary(versionName);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final String key = preference.getKey();
            if (key != null) {
                switch (key) {
                    case Settings.KEY_CFU:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.homepage_address))));
                        break;
                }
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}