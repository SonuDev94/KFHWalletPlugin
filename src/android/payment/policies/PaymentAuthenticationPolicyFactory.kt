package com.aub.mobilebanking.phone.eg.payment.policies

import com.aub.mobilebanking.phone.eg.screenLock.ScreenLockVerifier
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.payment.WaAssessment
import com.aub.mobilebanking.phone.eg.cardholderVerification.CardholderVerificationType
import com.aub.mobilebanking.phone.eg.card.ConfigurationRepository
import com.idemia.wa.app.domain.payment.policies.UnlockedScreenPaymentAuthenticationPolicy

class PaymentAuthenticationPolicyFactory(
    private val screenLockVerifier: ScreenLockVerifier,
    private val configurationRepository: ConfigurationRepository
) {
    fun createPaymentAuthenticationPolicy(): PaymentAuthenticationPolicy {
        configurationRepository.getCardholderVerificationType().let { verificationType ->
            return when (verificationType) {
                CardholderVerificationType.LVP_LOCKED_HVP_UNLOCKED ->
                    LockedScreenPaymentAuthenticationPolicy(screenLockVerifier)
                CardholderVerificationType.LVP_UNLOCKED_HVP_DOUBLE_TAP ->
                    UnlockedScreenPaymentAuthenticationPolicy(screenLockVerifier)
                CardholderVerificationType.UL_CARDHOLDER_VERIFICATION ->
                    UlPaymentAuthenticationPolicy(screenLockVerifier)
                else -> throw IllegalStateException("CardholderVerificationType should have been set up to this point")
            }.also {
                AppLogger.d(TAG, "createPaymentAuthenticationPolicy() for $verificationType")
            }
        }
    }

    fun setFinalAssessmentForUlSwitchedOn(isOn: Boolean) {
        UlPaymentAuthenticationPolicy.finalAssessmentSwitchedOn = isOn
    }

    fun setFinalAssessmentForUl(assessment: WaAssessment) {
        UlPaymentAuthenticationPolicy.finalAssesement = assessment
    }

    companion object {
        private val TAG = PaymentAuthenticationPolicyFactory::class.java.simpleName
    }
}