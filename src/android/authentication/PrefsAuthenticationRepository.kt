package com.aub.mobilebanking.phone.eg.authentication

import android.content.Context
import android.content.SharedPreferences
import com.aub.mobilebanking.phone.eg.utils.AppLogger

class PrefsAuthenticationRepository(
    private val context: Context,
    private val preferences: SharedPreferences =
        context.getSharedPreferences(
            AUTHENTICATION_PREFERENCES_FILENAME,
            Context.MODE_PRIVATE
        )
) : AuthenticationRepository {

    override fun getLastAuthenticationTimestamp(): LastAuthenticationTimestamp {
        return LastAuthenticationTimestamp.of(
            preferences.getLong(KEY_LAST_AUTHENTICATION_TIMESTAMP, LastAuthenticationTimestamp.EMPTY.value)
        ).also {
            AppLogger.d(TAG, "getLastAuthenticationTimestamp() $it")
        }
    }

    override fun setLastAuthenticationTimestamp(timestamp: LastAuthenticationTimestamp) {
        AppLogger.d(TAG, "setLastAuthenticationTimestamp() $timestamp")
        with(preferences.edit()) {
            putLong(KEY_LAST_AUTHENTICATION_TIMESTAMP, timestamp.value)
            apply()
        }
    }

    override fun clearLastAuthenticationTimestamp() {
        AppLogger.d(TAG, "clearLastAuthenticationTimestamp()")
        with(preferences.edit()) {
            remove(KEY_LAST_AUTHENTICATION_TIMESTAMP)
            apply()
        }
    }

    companion object {
        private const val AUTHENTICATION_PREFERENCES_FILENAME = "AUTHENTICATION_PREFERENCES"
        private const val KEY_LAST_AUTHENTICATION_TIMESTAMP = "KEY_LAST_AUTH_TIMESTAMP"
        private val TAG = PrefsAuthenticationRepository::class.java.simpleName
    }
}