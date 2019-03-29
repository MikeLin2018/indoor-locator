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

        return getPrefs(context).getString("email_key", "default_email");
    }

    public static void setEmail(Context context, String input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("email_key", input);
        editor.commit();
    }

    public static void setUserId(Context context, int input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt("user_id", input);
        editor.commit();
    }

    public static int getUserId(Context context) {
        return getPrefs(context).getInt("userId", 0);
    }

    public static void setPassword(Context context, String input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("password", input);
        editor.commit();
    }

    public static String getPassword(Context context) {
        return getPrefs(context).getString("password", "default_password");
    }

    public static void setName(Context context, String input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("name", input);
        editor.commit();
    }

    public static String getName(Context context) {
        return getPrefs(context).getString("name", "default_name");
    }


}
