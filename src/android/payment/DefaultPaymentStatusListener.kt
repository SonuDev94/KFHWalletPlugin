package com.aub.mobilebanking.phone.eg.payment

import com.aub.mobilebanking.phone.eg.transaction.LastTransactionStatusRepository
import com.aub.mobilebanking.phone.eg.transaction.TransactionStatus
import com.idemia.wa.api.nfc.*

abstract class DefaultPaymentStatusListener(
    private val lastTransactionStatusRepository: LastTransactionStatusRepository = LastTransactionStatusRepository()
) : PaymentStatusListener {

    abstract fun onWalletActionRequired(transactionDetails: WaNfcTransactionDetails)

    override fun onTransactionCompleted(transactionDetails: WaNfcTransactionDetails?) {
        if (transactionDetails?.transactionOutcome() == WaNfcTransactionOutcome.WALLET_ACTION_REQUIRED) {
            onWalletActionRequired(transactionDetails)
        }
        lastTransactionStatusRepository.updateTransactionStatus(
            transactionDetails
        )
    }

    override fun onTransactionIncident(exception: Exception?) {
        lastTransactionStatusRepository.updateTransactionStatus(
            TransactionStatus.INCIDENT
        )
    }

    override fun onTransactionAbort(abortReason: WaAbortReason?, exception: Exception?) {
        lastTransactionStatusRepository.updateTransactionStatus(
            TransactionStatus.ABORTED
        )
    }
}