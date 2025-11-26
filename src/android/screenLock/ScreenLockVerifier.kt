package com.aub.mobilebanking.phone.eg.screenLock

interface ScreenLockVerifier {
    fun isScreenLockSecure(): Boolean

    fun isScreenLockBlocked(): Boolean

    fun isScreenUnlocked(): Boolean
}