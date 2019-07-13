package sh.now.alecsisduart.who_says.helpers

import android.content.Context
import android.content.SharedPreferences
import java.lang.UnsupportedOperationException

private const val DEFAULT_SHARED_PREF_NAME = "CONFIGURATION"

object SharedPreferencesHelper {

    /**
     * Returns the default [SharedPreferences] with the [DEFAULT_SHARED_PREF_NAME] name in
     * [Context.MODE_PRIVATE]
     */
    @JvmStatic
    fun defaultPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(DEFAULT_SHARED_PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Returns the custom [SharedPreferences] with the specified [name] in [Context.MODE_PRIVATE]
     */
    fun customPreferences(context: Context, name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)


    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    /**
     * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
     */
    operator fun SharedPreferences.set(key: String, value: Any?) {
        edit {
            when (value) {
                is String? -> it.putString(key, value)
                is String -> it.putString(key, value)
                is Int -> it.putInt(key, value)
                is Boolean -> it.putBoolean(key, value)
                is Float -> it.putFloat(key, value)
                is Long -> it.putLong(key, value)
                else -> throw UnsupportedOperationException("Not yet implemented")
            }
        }
    }

    /**
     * finds value on given key.
     * [T] is the type of value
     * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
     */
    inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    }
}