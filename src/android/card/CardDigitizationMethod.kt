package com.aub.mobilebanking.phone.eg.card

enum class CardDigitizationMethod(val value: String) {
    MANUAL("MANUAL");

    companion object {
        fun of(value: String): CardDigitizationMethod {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Could not find CardDigitizationMethod for the given value: $value")
        }
    }
}