package com.aub.mobilebanking.phone.eg.payment.policies

import com.aub.mobilebanking.phone.eg.screenLock.ScreenLockVerifier
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.payment.WaAdvice
import com.idemia.wa.api.payment.WaAssessment

class UlPaymentAuthenticationPolicy(private val screenLockVerifier: ScreenLockVerifier) :
    PaymentAuthenticationPolicy {

    override fun isAuthenticated(isSecondTap: Boolean, isKeyguardAuthorized: Boolean): Boolean {
        return screenLockVerifier.isScreenUnlocked().also {
            AppLogger.d(TAG, "isAuthenticated() ? $it (== isScreenUnlocked)")
        }
    }

    override fun getFinalAssessment(advice: WaAdvice): WaAssessment {
        return if (finalAssessmentSwitchedOn) finalAssesement else advice.assessment().also {
            AppLogger.d(TAG, "getFinalAssessment() $it")
        }
    }

    companion object {
        var finalAssessmentSwitchedOn = false
        var finalAssesement = WaAssessment.PROCEED
        private val TAG = UlPaymentAuthenticationPolicy::class.java.simpleName
    }
}