package com.agorro.subtitledownloader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setSummary(sharedPreferences, SettingsActivity.KEY_PREF_SERVER_IP);
        setSummary(sharedPreferences, SettingsActivity.KEY_PREF_SERIES_FOLDER);
        setSummary(sharedPreferences, SettingsActivity.KEY_PREF_DOMAIN);
        setSummary(sharedPreferences, SettingsActivity.KEY_PREF_USERNAME);
        setSummary(sharedPreferences, SettingsActivity.KEY_PREF_PASSWORD);
    }

    private void setSummary(SharedPreferences sharedPreferences, String key)
    {
        Preference preference = findPreference(key);
        String preferenceValue = sharedPreferences.getString(key, "").trim();
        if (!"".equals(preferenceValue))
        {
            if (SettingsActivity.KEY_PREF_PASSWORD.equals(key))
            {
                preference.setSummary("*****");
            }
            else
            {
                preference.setSummary(sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        setSummary(sharedPreferences, key);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
