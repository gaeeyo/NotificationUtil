package jp.syoboi.android.notificationutil

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.annotation.Keep

class Prefs(val prefs: SharedPreferences) {

    companion object {

        private var instance: Prefs? = null

        fun get(context: Context): Prefs {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = Prefs(PreferenceManager.getDefaultSharedPreferences(context))
                    }
                }
            }
            return instance!!
        }
    }

    fun get(key: BooleanValues): Boolean {
        return prefs.getBoolean(key.name, key.defaultValue)
    }

    fun get(key : IntValues) : Int {
        return prefs.getInt(key.name, key.defaultValue)
    }

    fun editor(): Editor {
        return Editor(prefs.edit())
    }
}

class Editor(val editor: SharedPreferences.Editor) {
    fun put(key: BooleanValues, value: Boolean): Editor {
        editor.putBoolean(key.name, value)
        return this
    }

    fun put(key: IntValues, value: Int) : Editor {
        editor.putInt(key.name, value)
        return this
    }

    fun apply() {
        editor.apply()
    }
}

@Keep
enum class BooleanValues(val defaultValue: Boolean) {
    popupNotification(false), speechNotification(false)
}

@Keep
enum class IntValues(val defaultValue: Int) {
    popupTextColor(Color.rgb(255, 255, 255)), popupTextBackgroundColor(Color.argb(127, 0, 0, 0)),
    popupTextLines(4), popupScrollInterval(4000),
}