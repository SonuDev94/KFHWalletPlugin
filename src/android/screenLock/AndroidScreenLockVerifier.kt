package com.aub.mobilebanking.phone.eg.screenLock

import android.app.KeyguardManager
import android.content.Context

class AndroidScreenLockVerifier(private val context: Context) : ScreenLockVerifier {

    override fun isScreenLockSecure(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    override fun isScreenLockBlocked() = false

    override fun isScreenUnlocked(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !keyguardManager.isKeyguardLocked
    }
}