package com.aub.mobilebanking.phone.eg.repository

import com.idemia.wa.api.fcm.FcmService
import com.idemia.wa.api.lifecycle.LifecycleService
import com.idemia.wa.api.nfc.NfcPaymentService
import com.idemia.wa.api.push.RemoteMessagingService
import com.idemia.wa.api.wallet.WalletService
import com.idemia.wa.api.wms.*
import io.reactivex.rxjava3.core.Single

interface WaServicesRepository {
    val allServices: Single<WaServicesProvider>
    val cdCvmService: Single<CdCvmService>
    val enrollmentService: Single<EnrollmentService>
    val pairingService: Single<PairingService>
    val replenishmentService: Single<ReplenishmentService>
    val resourcesService: Single<ResourcesService>
    val walletService: Single<WalletService>
    val nfcPaymentService: Single<NfcPaymentService>
    val lifecycleService: Single<LifecycleService>
    val remoteMessagingService: Single<RemoteMessagingService>
    val transactionHistoryService: Single<TransactionsHistoryService>
    val fcmService: Single<FcmService>
}

