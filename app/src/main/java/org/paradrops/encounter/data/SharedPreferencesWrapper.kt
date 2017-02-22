package org.paradrops.encounter.data

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.paradrops.encounter.application.MainApplication

class SharedPreferencesWrapper {

    companion object {

        fun get(key: String): String {
            return get(getDefaultSharedPreferences(), key)
        }

        fun getInt(key: String, default: Int): Int {
            try {
                return get(getDefaultSharedPreferences(), key).toInt()
            } catch (exception : NumberFormatException) {
                return default
            }
        }

        fun getLong(key: String, default: Long): Long {
            try {
                return get(getDefaultSharedPreferences(), key).toLong()
            } catch (exception : NumberFormatException) {
                return default
            }
        }

        fun getBoolean(key: String, default: Boolean): Boolean {
            try {
                return get(getDefaultSharedPreferences(), key).toBoolean()
            } catch (exception : Exception) {
                return default
            }
        }

        fun save(key: String, value: String) {
            save(getDefaultSharedPreferences(), key, value)
        }

        fun saveInt(key: String, value: Int) {
            save(getDefaultSharedPreferences(), key, value.toString())
        }

        fun saveLong(key: String, value: Long) {
            save(getDefaultSharedPreferences(), key, value.toString())
        }

        fun saveBoolean(key: String, value: Boolean) {
            save(getDefaultSharedPreferences(), key, value.toString())
        }

        fun getDefaultSharedPreferences(): SharedPreferences {
            val context = MainApplication.applicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        private fun get(preference: SharedPreferences, key: String): String {
            return preference.getString(key, "")
        }

        private fun save(preference: SharedPreferences, key: String, value: String) {
            val editor = preference.edit()
            editor.putString(key, value)
            editor.apply()
        }
    }
}
