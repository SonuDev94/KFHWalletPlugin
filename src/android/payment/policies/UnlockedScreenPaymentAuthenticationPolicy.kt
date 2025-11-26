package com.idemia.wa.app.domain.payment.policies

import com.aub.mobilebanking.phone.eg.payment.policies.PaymentAuthenticationPolicy
import com.aub.mobilebanking.phone.eg.screenLock.ScreenLockVerifier
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.payment.WaAdvice
import com.idemia.wa.api.payment.WaAssessment

class UnlockedScreenPaymentAuthenticationPolicy(private val screenLockVerifier: ScreenLockVerifier) :
    PaymentAuthenticationPolicy {

    override fun isAuthenticated(isSecondTap: Boolean, isKeyguardAuthorized: Boolean): Boolean {
        val isScreenUnlocked = screenLockVerifier.isScreenUnlocked()
        return (isScreenUnlocked && isSecondTap && isKeyguardAuthorized).also {
            AppLogger.d(
                TAG,
                "isAuthenticated ? $it (== isScreenUnlocked ? $isScreenUnlocked && isSecondTap ? $isSecondTap && isKeyguardAuthorized ? $isKeyguardAuthorized)"
            )
        }
    }

    override fun getFinalAssessment(advice: WaAdvice): WaAssessment {
        screenLockVerifier.isScreenUnlocked().let { isScreenUnlocked ->
            return if (isScreenUnlocked) {
                advice.assessment()
            } else {
                WaAssessment.DECLINE
            }.also {
                AppLogger.d(TAG, "getFinalAssessment() $it, isScreenUnlocked ? $isScreenUnlocked")
            }
        }
    }

    companion object {
        private val TAG = UnlockedScreenPaymentAuthenticationPolicy::class.java.simpleName
    }
}