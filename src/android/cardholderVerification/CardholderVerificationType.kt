package com.aub.mobilebanking.phone.eg.cardholderVerification

enum class CardholderVerificationType(val value: String) {
    LVP_LOCKED_HVP_UNLOCKED("LVP_LOCKED_HVP_UNLOCKED"),
    LVP_UNLOCKED_HVP_DOUBLE_TAP("LVP_UNLOCKED_HVP_DOUBLE_TAP"),
    UL_CARDHOLDER_VERIFICATION("UL_CARDHOLDER_VERIFICATION");

    companion object {
        fun of(value: String): CardholderVerificationType {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Could not find CardholderVerificationType for the given value: $value")
        }
    }
}