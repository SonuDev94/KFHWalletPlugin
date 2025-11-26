package com.aub.mobilebanking.phone.eg.transaction

data class TransactionHistoryItem(
    val date: String,
    val merchantName: String?,
    val transactionStatus: String,
    val amount: String
)