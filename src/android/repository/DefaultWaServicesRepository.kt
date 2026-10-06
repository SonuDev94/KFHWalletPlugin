package com.aub.mobilebanking.phone.eg.repository

import com.aub.mobilebanking.phone.eg.KFHWalletPlugin
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.InitializationListener
import com.idemia.wa.api.WalletAgentApi
import com.idemia.wa.api.fcm.FcmService
import com.idemia.wa.api.lifecycle.LifecycleService
import com.idemia.wa.api.nfc.NfcPaymentService
import com.idemia.wa.api.push.RemoteMessagingService
import com.idemia.wa.api.wallet.WalletService
import com.idemia.wa.api.wms.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class DefaultWaServicesRepository(
) : WaServicesRepository {
    override val cdCvmService: Single<CdCvmService>
        get() = allServices.map { it.cdCvmService }

    override val enrollmentService: Single<EnrollmentService>
        get() = allServices.map { it.enrollmentService }

    override val pairingService: Single<PairingService>
        get() = allServices.map { it.pairingService }

    override val replenishmentService: Single<ReplenishmentService>
        get() = allServices.map { it.replenishmentService }

    override val resourcesService: Single<ResourcesService>
        get() = allServices.map { it.resourcesService }

    override val walletService: Single<WalletService>
        get() = allServices.map { it.walletService }

    override val nfcPaymentService: Single<NfcPaymentService>
        get() = allServices.map { it.nfcPaymentService }

    override val lifecycleService: Single<LifecycleService>
        get() = allServices.map { it.lifecycleService }

    override val remoteMessagingService: Single<RemoteMessagingService>
        get() = allServices.map { it.remoteMessagingService }

    override val transactionHistoryService: Single<TransactionsHistoryService>
        get() = allServices.map { it.transactionHistoryService }

    override val fcmService: Single<FcmService>
        get() = allServices.map { it.fcmService }

    override val allServices: Single<WaServicesProvider> by lazy {
        Single.create { emitter: SingleEmitter<WaServicesProvider> ->
            if (!emitter.isDisposed) {
                WA_INITIALIZER.subscribe({ services ->
                    emitter.onSuccess(services)
                },
                    { error ->
                        emitter.onError(error)
                    })
            }
        }
    }

    companion object {
        private val WA_INITIALIZER: Single<WaServicesProvider> = Single.create { emitter ->
            WalletAgentApi.initialize(
                KFHWalletPlugin.application,
                object : InitializationListener {
                    override fun onSuccess() {
                        val services = WaServicesProvider(WalletAgentApi.getInstance())
                        AppLogger.d(TAG, "Wallet Agent initialized.")
                        emitter.onSuccess(services)
                    }

                    override fun onFailure(exception: Exception) {
                        AppLogger.d(TAG, "Wallet Agent initialization failure.", exception)
                        emitter.onError(Exception("InitializationListener.onFailure()", exception))
                    }
                })
        }.cache()
        private val TAG = DefaultWaServicesRepository::class.java.simpleName
    }
}