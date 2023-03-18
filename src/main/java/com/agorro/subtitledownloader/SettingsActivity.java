package com.agorro.subtitledownloader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity
{
    public static final String KEY_PREF_SERVER_IP = "server_ip";
    public static final String KEY_PREF_DOMAIN = "user_domain";
    public static final String KEY_PREF_USERNAME = "user_username";
    public static final String KEY_PREF_PASSWORD = "user_password";
    public static final String KEY_PREF_SERIES_FOLDER = "series_folder";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}