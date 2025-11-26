package com.aub.mobilebanking.phone.eg.payment.policies

import com.idemia.wa.api.payment.WaAdvice
import com.idemia.wa.api.payment.WaAssessment

interface PaymentAuthenticationPolicy {
    fun isAuthenticated(isSecondTap: Boolean, isKeyguardAuthorized: Boolean): Boolean

    fun getFinalAssessment(advice: WaAdvice): WaAssessment
}