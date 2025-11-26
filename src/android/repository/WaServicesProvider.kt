package com.aub.mobilebanking.phone.eg.repository

import com.idemia.wa.api.WalletAgentApi
import com.idemia.wa.api.fcm.FcmService
import com.idemia.wa.api.lifecycle.LifecycleService
import com.idemia.wa.api.nfc.NfcPaymentService
import com.idemia.wa.api.push.RemoteMessagingService
import com.idemia.wa.api.wallet.WalletService
import com.idemia.wa.api.wms.*
//import io.reactivex.rxjava3.core.Single

class WaServicesProvider(val walletAgentApi: WalletAgentApi) {
    val cdCvmService: CdCvmService
        get() {
            return walletAgentApi.getService(CdCvmService::class.java)
        }

    val enrollmentService: EnrollmentService
        get() {
            return walletAgentApi.getService(EnrollmentService::class.java)
        }

    val pairingService: PairingService
        get() {
            return walletAgentApi.getService(PairingService::class.java)
        }

    val replenishmentService: ReplenishmentService
        get() {
            return walletAgentApi.getService(ReplenishmentService::class.java)
        }

    val resourcesService: ResourcesService
        get() {
            return walletAgentApi.getService(ResourcesService::class.java)
        }

    val walletService: WalletService
        get() {
            return walletAgentApi.getService(WalletService::class.java)
        }

    val nfcPaymentService: NfcPaymentService
        get() {
            return walletAgentApi.getService(NfcPaymentService::class.java)
        }

    val lifecycleService: LifecycleService
        get() {
            return walletAgentApi.getService(LifecycleService::class.java)
        }

    val remoteMessagingService: RemoteMessagingService
        get() {
            return walletAgentApi.getService(RemoteMessagingService::class.java)
        }

    val transactionHistoryService: TransactionsHistoryService
        get() {
            return walletAgentApi.getService(TransactionsHistoryService::class.java)
        }

    val fcmService: FcmService
        get() {
            return walletAgentApi.getService(FcmService::class.java)
        }
}
