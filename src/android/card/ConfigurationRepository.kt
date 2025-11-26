package com.aub.mobilebanking.phone.eg.card

import com.aub.mobilebanking.phone.eg.cardholderVerification.CardholderVerificationType

interface ConfigurationRepository {
    fun getCardholderVerificationType(): CardholderVerificationType?

    fun getCardDigitizationMethod(): CardDigitizationMethod?
}