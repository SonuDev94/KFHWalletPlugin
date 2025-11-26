package com.aub.mobilebanking.phone.eg.userpresence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.aub.mobilebanking.phone.eg.authentication.AuthenticationRepository
import com.aub.mobilebanking.phone.eg.authentication.LastAuthenticationTimestamp
import com.aub.mobilebanking.phone.eg.authentication.PrefsAuthenticationRepository
import com.aub.mobilebanking.phone.eg.utils.AppLogger

class UserPresentBroadcastReceiver(val context: Context) : BroadcastReceiver() {
    private val authenticationRepository: AuthenticationRepository = PrefsAuthenticationRepository(context)

    fun register() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(this, filter)
    }

    override fun onReceive(context: Context, intent: Intent) {
        AppLogger.d(TAG, "onReceive() ${intent.action}")
        if (intent.action != Intent.ACTION_USER_PRESENT) {
            return
        }
        saveLastAuthenticationTimestamp()
    }

    private fun saveLastAuthenticationTimestamp() {
        System.currentTimeMillis().let {
            AppLogger.d(TAG, "saveLastAuthenticationTimestamp() $it")
            authenticationRepository.setLastAuthenticationTimestamp(
                LastAuthenticationTimestamp.of(it)
            )
        }
    }

     fun clearLastAuthenticationTimestampIfOreoOrHigher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            authenticationRepository.clearLastAuthenticationTimestamp()
        }
    }

    companion object {
        private val TAG = UserPresentBroadcastReceiver::class.java.simpleName
    }
}