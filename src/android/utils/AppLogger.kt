package com.aub.mobilebanking.phone.eg.utils

import android.util.Log

/**
 * Simple centralized logger for the Cordova plugin.
 * Benefits:
 *  - One switch to enable/disable logs
 *  - Consistent TAG prefix
 *  - Safe in ProGuard/R8 (if keep class)
 */
object AppLogger {
    // Toggle logging ON/OFF
    var isLoggingEnabled: Boolean = true

    private const val DEFAULT_TAG = "KFHWalletPlugin"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        if (isLoggingEnabled) Log.d(tag, message)
    }


    fun d(tag: String = DEFAULT_TAG, message: String, t: Throwable? = null) {
        if (isLoggingEnabled) Log.d(tag, message,t)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        if (isLoggingEnabled) Log.i(tag, message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String, t: Throwable? = null) {
        if (isLoggingEnabled) Log.w(tag, message, t)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, t: Throwable? = null) {
        if (isLoggingEnabled) Log.e(tag, message, t)
    }
}