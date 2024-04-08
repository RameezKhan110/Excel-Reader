package com.rameez.hel

import android.content.Context
import android.content.SharedPreferences

object SharedPref {
    private const val PREFS_NAME = "MyPrefs"
    private const val BOOLEAN_KEY = "my_boolean_key"

    fun appLaunched(context: Context, value: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putBoolean(BOOLEAN_KEY, value)
        editor.apply()
    }

    fun isAppLaunched(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(BOOLEAN_KEY, false)
    }
}