package com.aub.mobilebanking.phone.eg.utils

import java.util.*

private val currencyCodeMap = mapOf(
    840 to "USD", // United States Dollar
    978 to "EUR", // Euro
    756 to "CHF", // Swiss Franc
    376 to "ILS", // Israeli Shekel
    818 to "EGP", // Egyptian Pound 🇪🇬
    414 to "KWD", // Kuwaiti Dinar 🇰🇼
    512 to "OMR", // Omani Rial
    784 to "AED", // UAE Dirham 🇦🇪
    682 to "SAR", // Saudi Riyal 🇸🇦
    392 to "JPY", // Japanese Yen
    826 to "GBP"  // British Pound Sterling
)

fun getCurrencyByNumericCode(numericCode: Int): Currency {
    return Currency.getInstance(currencyCodeMap[numericCode])
}