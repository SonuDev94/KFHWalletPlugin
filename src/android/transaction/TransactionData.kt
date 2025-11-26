package com.aub.mobilebanking.phone.eg.transaction

import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.aub.mobilebanking.phone.eg.utils.Hex
import com.aub.mobilebanking.phone.eg.utils.getCurrencyByNumericCode
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

private const val TAG = "TransactionData"
//private const val TRANSACTION_TERMINAL_DATE_FORMAT = "yyMMdd"
private const val HUNDRED = 100
private const val DECIMAL_PLACES_2 = 2

class TransactionData(
    merchantNameHex: String,
    amount: String,
    currency: String,
    date: Long
) {
    val merchantName: String
    val amountWithCurrency: String
    val formattedDate: Date

    init {
        formattedDate = createDate(date)
        merchantName = createMerchantName(merchantNameHex)
        this.amountWithCurrency = createAmountWithCurrency(amount, currency)
        AppLogger.d(TAG, "merchantNameHex: $merchantNameHex, amount: $amount, currency: $currency, date: $date")
        AppLogger.d(
            TAG,
            "merchantName: $merchantName, amountWithCurrency: $amountWithCurrency, date: $date"
        )
    }

    private fun createAmountWithCurrency(amount: String, currencyCode: String): String {
        val currencyNumericCode = Integer.valueOf(currencyCode)
        val currency = getCurrencyByNumericCode(currencyNumericCode)
        val amountAuthorized = BigDecimal(amount)
            .divide(BigDecimal(HUNDRED), DECIMAL_PLACES_2, RoundingMode.UNNECESSARY)
        return "${currency.currencyCode} $amountAuthorized"
    }

    private fun createMerchantName(merchantNameHex: String): String {
        return String(Hex.decode(merchantNameHex), Charsets.UTF_8)
    }

    private fun createDate(timestamp: Long): Date {
//        val dateFormat = SimpleDateFormat(TRANSACTION_TERMINAL_DATE_FORMAT)
        val transactionDate = Date(timestamp)
        transactionDate.time = System.currentTimeMillis()
        return transactionDate
    }
}