package com.aub.mobilebanking.phone.eg.screenLock

import android.content.Context
import android.os.PowerManager
import com.aub.mobilebanking.phone.eg.utils.AppLogger


class AndroidScreenManager(private val context: Context,
                           private val powerManager: PowerManager
                           = context.getSystemService(Context.POWER_SERVICE) as PowerManager) :
    ScreenManager {
    override fun isScreenOn() = powerManager.isInteractive.also {
        AppLogger.d(TAG, "isScreenOn() ? $it")
    }

    companion object {
        private val TAG = AndroidScreenManager::class.java.simpleName
    }
}