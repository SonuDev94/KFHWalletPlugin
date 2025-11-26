package com.aub.mobilebanking.phone.eg.transaction

import com.idemia.wa.api.nfc.WaNfcTransactionDetails
import com.idemia.wa.api.nfc.WaNfcTransactionOutcome
import kotlin.properties.Delegates

class LastTransactionStatusRepository : TransactionStatusListener {
    override val lastTransactionStatus get() = LastTransactionStatusRepository.lastTransactionStatus

    override fun setTransactionStatusChangeListener(transactionStatusChangeListener: TransactionStatusChangeListener) {
        LastTransactionStatusRepository.transactionStatusChangeListener = transactionStatusChangeListener
    }

    override fun removeTransactionStatusChangeListener(transactionStatusChangeListener: TransactionStatusChangeListener) {
        if (LastTransactionStatusRepository.transactionStatusChangeListener == transactionStatusChangeListener) {
            LastTransactionStatusRepository.transactionStatusChangeListener = null
        }
    }

    fun updateTransactionStatus(transactionDetails: WaNfcTransactionDetails?) {
        LastTransactionStatusRepository.lastTransactionStatus = when (transactionDetails?.transactionOutcome()) {
            WaNfcTransactionOutcome.AUTHORIZE_ONLINE, WaNfcTransactionOutcome.AUTHENTICATE_OFFLINE -> TransactionStatus
                .SUCCESS
            WaNfcTransactionOutcome.WALLET_ACTION_REQUIRED -> TransactionStatus.WALLET_ACTION_REQUIRED
            else -> TransactionStatus.INCIDENT
        }
    }

    fun updateTransactionStatus(status: TransactionStatus) {
        LastTransactionStatusRepository.lastTransactionStatus = status
    }

    companion object {
        private var transactionStatusChangeListener: TransactionStatusChangeListener? = null

        private var lastTransactionStatus: TransactionStatus by Delegates.observable(
            TransactionStatus.NOT_EXECUTED
        ) { _, _, new ->
            transactionStatusChangeListener?.onTransactionStatusChange(new)
        }
    }
}