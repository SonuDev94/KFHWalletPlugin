package com.aub.mobilebanking.phone.eg.payment

import com.idemia.wa.api.nfc.WaAbortReason
import com.idemia.wa.api.nfc.WaNfcTransactionDetails

interface PaymentStatusListener {
    fun onTransactionCompleted(transactionDetails: WaNfcTransactionDetails?)

    fun onTransactionIncident(exception: Exception?)

    fun onTransactionAbort(abortReason: WaAbortReason?, exception: Exception?)
}