package com.locateme.indoor_locator;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyValueDB {
    private SharedPreferences sharedPreferences;
    private static String PREF_NAME = "prefs";

    public KeyValueDB() {
        // Blank
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getEmail(Context context) {
        return getPrefs(context).getString("email_key", "default_username");
    }

    public static void setEmail(Context context, String input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("email_key", input);
        editor.commit();
    }
}
