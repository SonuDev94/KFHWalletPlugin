package com.aub.mobilebanking.phone.eg.card

import com.aub.mobilebanking.phone.eg.cardholderVerification.CardholderVerificationType
import com.aub.mobilebanking.phone.eg.utils.AppLogger

class PrefsConfigurationRepository : ConfigurationRepository {
    override fun getCardholderVerificationType(): CardholderVerificationType {
        return CardholderVerificationType.LVP_LOCKED_HVP_UNLOCKED
    }

    override fun getCardDigitizationMethod(): CardDigitizationMethod {
        AppLogger.d(TAG, "getCardDigitizationMethod()")
        return CardDigitizationMethod.MANUAL
    }

    companion object {
        private val TAG = PrefsConfigurationRepository::class.java.simpleName
    }
}