package com.aub.mobilebanking.phone.eg.transaction

enum class TransactionStatus {
    SUCCESS, INCIDENT, NOT_EXECUTED, WALLET_ACTION_REQUIRED, ABORTED
}

interface TransactionStatusChangeListener {
    fun onTransactionStatusChange(status: TransactionStatus)
}

interface TransactionStatusListener {
    val lastTransactionStatus: TransactionStatus
    fun setTransactionStatusChangeListener(transactionStatusChangeListener: TransactionStatusChangeListener)
    fun removeTransactionStatusChangeListener(transactionStatusChangeListener: TransactionStatusChangeListener)
}