package com.aub.mobilebanking.phone.eg.enrollment

import android.graphics.Bitmap
import android.util.Log
import com.aub.mobilebanking.phone.eg.KFHWalletPlugin.Companion.application
import com.aub.mobilebanking.phone.eg.error.ErrorResolver
import com.aub.mobilebanking.phone.eg.error.ResourcesBasedErrorResolver
import com.aub.mobilebanking.phone.eg.fcm.DefaultRegistrationNotificationSender
import com.aub.mobilebanking.phone.eg.model.CardConfigurationModel
import com.aub.mobilebanking.phone.eg.model.CdCvmOption
import com.aub.mobilebanking.phone.eg.model.HomeViewCardToken
import com.aub.mobilebanking.phone.eg.model.InitialData
import com.aub.mobilebanking.phone.eg.model.TokenConfigurationListModel
import com.aub.mobilebanking.phone.eg.repository.CardArtRepository
import com.aub.mobilebanking.phone.eg.repository.DefaultCardArtRepository
import com.aub.mobilebanking.phone.eg.repository.WaServicesProvider
import com.aub.mobilebanking.phone.eg.repository.WaServicesRepository
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.aub.mobilebanking.phone.eg.utils.scheduleIoThenMain
import com.idemia.wa.api.ErrorCode
import com.idemia.wa.api.Failure
import com.idemia.wa.api.WaCard
import com.idemia.wa.api.WaCdCvmType
import com.idemia.wa.api.WaCredentialStatus
import com.idemia.wa.api.WaDeviceSecurityRating
import com.idemia.wa.api.WaToken
import com.idemia.wa.api.WaTokenId
import com.idemia.wa.api.lifecycle.LifecycleChangeListener
import com.idemia.wa.api.nfc.NfcPaymentService
import com.idemia.wa.api.wallet.WalletService
import com.idemia.wa.api.wms.EnrollmentListener
import com.idemia.wa.api.wms.OtpAuthCallback
import com.idemia.wa.api.wms.PairingListener
import com.idemia.wa.api.wms.SelectCdCvmCallback
import com.idemia.wa.api.wms.SelectIdvOptionCallback
import com.idemia.wa.api.wms.SelectTokenPurposeCallback
import com.idemia.wa.api.wms.TncCallback
import com.idemia.wa.api.wms.WaEnrollmentParams
import com.idemia.wa.api.wms.WaIdvOption
import com.idemia.wa.api.wms.WaOtp
import com.idemia.wa.api.wms.WaTnc
import com.idemia.wa.api.wms.WaTokenPurpose
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject

class EnrollmentManager(
    private val waServicesRepository: WaServicesRepository,
    private val sendEventToJs: (String, Any?) -> Unit,
    private val cardArtRepository: CardArtRepository = DefaultCardArtRepository(waServicesRepository.resourcesService),
    private val registrationNotificationSender: DefaultRegistrationNotificationSender = DefaultRegistrationNotificationSender(
        application
    ),
) {
    private val disposables = CompositeDisposable()
    private val errorResolver: ErrorResolver = ResourcesBasedErrorResolver(application)
    private var tokenCallback: SelectTokenPurposeCallback? = null
    private var idvCallback: SelectIdvOptionCallback? = null
    private var otpCallback: OtpAuthCallback? = null
    private var tncCallback: TncCallback? = null
    private lateinit var selectCdCvmCallback: SelectCdCvmCallback

    val TAG = EnrollmentManager::class.java.simpleName

    fun startEnrollment(
        params: WaEnrollmentParams,
        onError: (Throwable) -> Unit
    ) {
        val enrollDisposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                sendEventToJs("startEnrollment", "enrollment has started")
                AppLogger.d(TAG, "enrollment has started")
                val pairingService = services.pairingService
                if (pairingService.isPaired) {
                    enroll(services, params)
                } else {
                    pairAndEnroll(services, params)
                }
            }, { err ->
                onError(err)
                AppLogger.e(TAG, "Error during enrollment", err)
            })
        disposables.add(enrollDisposable)
    }

    private fun pairAndEnroll(
        services: WaServicesProvider,
        params: WaEnrollmentParams
    ) {
        services.pairingService.pairDevice(object : PairingListener {

            override fun onSelectCdCvmType(
                waCdCvmOptions: Set<WaCdCvmType>,
                callback: SelectCdCvmCallback
            ) {
                selectCdCvmCallback = callback
                if (waCdCvmOptions.size == 1) {
                    selectCdCvmCallback.selectCdCvmType(waCdCvmOptions.first())
                } else {
                    sendEventToJs("cdCvmSelection", createCdCvmOptions(waCdCvmOptions))
                }
            }


            override fun onFailure(failure: Failure) {
                failure?.exception?.printStackTrace()
                sendEventToJs("pairingFailed", failure.toString())
            }

            override fun onComplete(deviceSecurityRating: WaDeviceSecurityRating) {
                AppLogger.d(
                    "PairingListener",
                    "Pairing complete. with DeviceSecurityRating: $deviceSecurityRating and ${deviceSecurityRating.name}"
                )
                enroll(services, params)
                val disposa = registrationNotificationSender.sendRegistrationMessage {
                    Log.d(TAG, it)
                }.scheduleIoThenMain().subscribe({
                    Log.d(TAG, "Registration message sent.")
                }, { error ->
                    Log.e(TAG, "Error during sending registration message. $error")
                })
                disposables.add(disposa)

            }
        })
    }

    private fun createCdCvmOptions(cdCvmTypes: Set<WaCdCvmType>): List<CdCvmOption> {
        return cdCvmTypes.map {
            CdCvmOption(it, it.name)
        }
    }

    private fun enroll(services: WaServicesProvider, params: WaEnrollmentParams) {
        AppLogger.d(
            TAG,
            "Card Size ${services.walletService.cards.size} and ${services.walletService.cardsMaxCount}"
        )
        if (services.walletService.cards.size < services.walletService.cardsMaxCount) {
            services.enrollmentService.enrollCard(params, object : EnrollmentListener {

                override fun onSelectTokenPurpose(
                    purposes: MutableSet<WaTokenPurpose>,
                    cb: SelectTokenPurposeCallback
                ) {
                    tokenCallback = cb
                    sendEventToJs("onSelectTokenPurpose", purposes.toSet())
                }

                override fun onFailure(failure: Failure?) {
                    failure?.exception?.printStackTrace()
                    sendEventToJs("enrollmentFailed", failure.toString())
                }

                override fun onComplete() {
                    setAsDefaultToken(services.nfcPaymentService, services.walletService)
                    sendEventToJs("enrollmentCompleted", "SUCCESS")
                }

                override fun onSelectIdvOption(
                    idvOptions: List<WaIdvOption>,
                    callback: SelectIdvOptionCallback
                ) {
                    idvCallback = callback
//                    sendEventToJs("onSelectIdvOption", createIdvOptions(idvOptions))
                    // Convert WaIdvOption list to JSON
                    val jsonArray = JSONArray()
                    idvOptions.forEach { option ->
                        val obj = JSONObject()
                        obj.put("id", option.id)       // or whatever getters WaIdvOption has
                        obj.put("type", option.type.name)
                        obj.put("hint", option.hint ?: "")
                        jsonArray.put(obj)
                    }
                    sendEventToJs("onSelectIdvOption", jsonArray)
                }

                override fun onSubmitOtp(cb: OtpAuthCallback) {
                    otpCallback = cb
                    sendEventToJs("onSubmitOtp", "OTP_REQUIRED")
                }

                override fun onCardEnrollmentStarted(p0: MutableList<String>?) {
                    sendEventToJs("EnrollmentManager", "onCardEnrollmentStarted")
                }

                override fun onTokenEnrollmentStarted(p0: String?) {
                    sendEventToJs("EnrollmentManager", "onTokenEnrollmentStarted")
                }

                override fun onTokenEnrolled(p0: WaCredentialStatus?) {
                    sendEventToJs("EnrollmentManager", "onTokenEnrolled")
                }

                override fun onAcceptTnc(tnc: WaTnc, cb: TncCallback) {
                    tncCallback = cb
                    sendEventToJs("onAcceptTnc", tnc.content)
                }
            })
        } else {
            AppLogger.e(TAG, "Maximum card count reached.")
            sendEventToJs(
                "Paring Device failed",
                errorResolver.resolveError(ErrorCode.CARDS_MAX_COUNT_REACHED)
            )
        }
    }

    /*private fun createIdvOptions(waIdvOptions: List<WaIdvOption>): List<IdvOption> {
        return waIdvOptions.map {
            when (it.type) {
                WaIdvType.OTP_EMAIL -> {
                    IdvOption(it, "One-Time Password from Email")
                }

                WaIdvType.OTP_SMS -> {
                    IdvOption(it, "One-Time Password from SMS")
                }

                else -> {
                    throw Exception("Unknown IDV option.")
                }
            }
        }
    }*/

    fun selectCdCvmType(selected: WaCdCvmType) = selectCdCvmCallback.selectCdCvmType(selected)

    fun submitTokenPurpose(selectedTokenPurposes: MutableSet<WaTokenPurpose>) {
        tokenCallback?.selectTokenPurpose(selectedTokenPurposes)
    }

    fun submitIdvOption(idvOption: WaIdvOption) {
        idvCallback?.selectIdvOption(idvOption)
    }

    fun submitOtp(otp: WaOtp) {
        otpCallback?.submitOtp(otp)
    }

    fun acceptTnc() {
        tncCallback?.accept()
    }

    fun getWalletCardsMaxCount(
        onError: (Throwable) -> Unit
    ) {
        val disposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                val walletMaxCount = services.walletService.cardsMaxCount
                sendEventToJs("WalletCardsMaxCount", walletMaxCount)
            }, { err ->
                onError(err)
                AppLogger.e(TAG, "Error during get wallet cards max count", err)
            })
        disposables.add(disposable)
    }


    fun getCardsWithEnrollmentStatus(
        onSuccess: (List<HomeViewCardToken>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        sendEventToJs(TAG, "Get Card Data")
        val dispose =
            waServicesRepository.allServices.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .map { services ->
                    AppLogger.d(TAG, "Get Card Data Log")
                    val walletService = services.walletService
                    val nfcService = services.nfcPaymentService
                    val defaultToken = nfcService.defaultToken
                    var adapterPosition = 0
                    val cardTokensList = mutableListOf<HomeViewCardToken>()
//                    AppLogger.d(TAG, "Thread name : ${Thread.currentThread().name}")
                    walletService.cards.forEach { waCard ->
//                        AppLogger.d(TAG, "Thread name : ${Thread.currentThread().name}")
                        try {
                            fetchCardArt(waCard).blockingGet()
                        } catch (e: Exception) {
                            AppLogger.e(TAG, "Card art error for ${waCard.id}", e)
                        }
//                        AppLogger.d(TAG,"load WACardId: ${waCard.id} and cardID Value ${waCard.id.value}")
                        val tokens = walletService.getTokens(waCard.id)
                        var isDefault = false

                        if (defaultToken != null) {
                            isDefault = tokens.any { it.id.value == defaultToken.value }
                        }

                        val cardToken = HomeViewCardToken(
                            card = waCard,
                            status = tokens.firstOrNull()?.status.toString(),
                            adapterPosition = adapterPosition,
                            tokenList = tokens
                        )

                        if (isDefault) {
                            cardTokensList.add(0, cardToken.copy(adapterPosition = 0))
                        } else {
                            cardTokensList.add(cardToken)
                        }

                        adapterPosition += 1
                    }
                    return@map cardTokensList
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    onSuccess(list)
                }, { err ->
                    onError(err)
                    AppLogger.e(TAG, "Error during get Card List", err)
                })
        disposables.add(dispose)
    }

    private fun fetchCardArt(card: WaCard): Single<Bitmap> {
        return cardArtRepository.fetchCardArt(card)
    }

    fun getCardArtOrNull(waCardId: String) = cardArtRepository.getCardFromStorage(waCardId)

    fun changeTokenDefaultState(
        tokenId: WaTokenId,
        isDefault: Boolean,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val enrollDisposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                val nfcPaymentService = services.nfcPaymentService
                try {
                    if (isDefault) {
                        nfcPaymentService.defaultToken = tokenId
                        AppLogger.d("EnrollmentManager", "Token set as default: $tokenId")
                    } else {
                        nfcPaymentService.removeDefaultToken()
                        AppLogger.d("EnrollmentManager", "Default token removed")
                    }
                    onSuccess()
                } catch (e: Exception) {
                    onError(e)
                    AppLogger.e("EnrollmentManager", "Error changing default token state", e)
                    sendEventToJs("changeTokenDefaultStateFailed", e.message ?: "Unknown error")
                }
            }, { err ->
                onError(err)
                AppLogger.e("EnrollmentManager", "Error during setting default token", err)
                sendEventToJs("changeTokenDefaultStateFailed", "Error during setting default token")
            })
        disposables.add(enrollDisposable)
    }


    fun deleteToken(
        tokenId: WaTokenId,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val enrollDisposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                val lifecycleService = services.lifecycleService
                lifecycleService.delete(tokenId, object : LifecycleChangeListener {
                    override fun onSuccess() {
                        AppLogger.d("EnrollmentManager", "Token deleted successfully: $tokenId")
                        onSuccess()
                    }

                    override fun onFailure(fail: Failure?) {
                        val errMsg = "Failed to delete token: ${fail?.exception?.message}"
                        AppLogger.e("EnrollmentManager", errMsg, fail?.exception)
                        onError(fail?.exception ?: Exception(errMsg))
                        sendEventToJs("deleteToneFailed", fail?.toString())
                    }
                })
            }, { err ->
                onError(err)
                AppLogger.e("EnrollmentManager", "Error during token deletion", err)
            })
        disposables.add(enrollDisposable)
    }

    fun deleteCard(
        initialData: InitialData,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {

        val enrollDisposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                sendEventToJs(TAG, "Delete Card(s) started")
                initialData.tokenIdList.forEach { cardToken ->
                    services.lifecycleService.delete(cardToken, object : LifecycleChangeListener {
                        override fun onSuccess() {
                            AppLogger.d(
                                TAG,
                                "Token $cardToken deleted successfully"
                            )
                            sendEventToJs(
                                "deleteCardSuccess",
                                "Token $cardToken deleted successfully"
                            )
                            onSuccess()
                        }

                        override fun onFailure(fail: Failure?) {
                            AppLogger.e(
                                TAG,
                                "Fail to delete token $cardToken",
                                fail?.exception
                            )
                            sendEventToJs("deleteCardFailed", fail?.toString())
                        }
                    })
                }
            }, { err ->
                onError(err)
                AppLogger.e(TAG, "Error during token deletion.", err)
            })
        disposables.add(enrollDisposable)
    }

    fun suspendCard(
        initialData: InitialData,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val enrollDisposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                initialData.tokenIdList.forEach { cardToken ->
                    services.lifecycleService.suspend(cardToken, object : LifecycleChangeListener {
                        override fun onSuccess() {
                            AppLogger.d(TAG, "Successful token suspension.")
                            // Optionally refresh card list after suspension
                            onSuccess()
                        }

                        override fun onFailure(fail: Failure?) {
                            AppLogger.e(TAG, "Fail to suspend token", fail?.exception)
                            //onError(fail?.exception ?: Exception("Suspend failed"))
                            sendEventToJs("suspendCardFailed", fail?.toString())
                        }
                    })
                }
            }, { err ->
                onError(err)
                AppLogger.e(TAG, "Error during token suspension.", err)
            })
        disposables.add(enrollDisposable)
    }

    fun resumeToken(
        tokenId: WaTokenId,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val enrollDisposable: Disposable = waServicesRepository.allServices
            .scheduleIoThenMain()
            .subscribe({ services ->
                val lifecycleService = services.lifecycleService
                lifecycleService.resume(tokenId, object : LifecycleChangeListener {
                    override fun onSuccess() {
                        AppLogger.d("EnrollmentManager", "Token resumed successfully: $tokenId")
                        onSuccess()
                    }

                    override fun onFailure(fail: Failure?) {
                        AppLogger.e("EnrollmentManager", "Fail to resume token", fail?.exception)
                        onError(fail?.exception ?: Exception("Unknown resume failure"))
                        sendEventToJs("resumeTokenFailed", fail?.toString())
                    }
                })
            }, { err ->
                onError(err)
                AppLogger.e("EnrollmentManager", "Error during token resume", err)
            })
        disposables.add(enrollDisposable)
    }

    fun loadCardDetails(
        initialData: InitialData,
        onSuccess: (cardConfiguration: CardConfigurationModel) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val dis = waServicesRepository.allServices.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).map { services ->
            val walletService = services.walletService
            val card = walletService.cards.firstOrNull { it.id == initialData.cardId }
            var bitmap: Bitmap? = null
            card?.let {
                bitmap = cardArtRepository.fetchCardArt(it).blockingGet()
            }
            val nfcPaymentService = services.nfcPaymentService
            AppLogger.d(
                TAG,
                "load CardId: ${initialData.cardId}  and cardID Value ${initialData.cardId.value}"
            )
            val tokenList = walletService.getTokens(initialData.cardId)
            val isDefault = isDefaultToken(nfcPaymentService, initialData.tokenIdList)
            val waTokenList = mutableListOf<TokenConfigurationListModel>()

            tokenList.forEach { token ->
                waTokenList.add(
                    TokenConfigurationListModel(
                        token.id.value,
                        token.status,
                        token.displayData?.lastDigits,
                        token.displayData?.expDate,
                        true
                    )
                )
            }

            val cardConfiguration = CardConfigurationModel(
                tokenList.firstOrNull()?.status,
                initialData.cardPan,
                initialData.cardExp,
                waTokenList,
                isDefault,
                bitmap
            )
            return@map cardConfiguration
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cardConfiguration ->
//            mutableTokenConfigurationLiveData.value = cardConfiguration
            onSuccess(cardConfiguration)
        }, { err ->
            onError(err)
            AppLogger.e(TAG, "Error during token configuration for display.", err)
        })
        disposables.add(dis)

    }

    private fun setAsDefaultToken(paymentService: NfcPaymentService, walletService: WalletService) {
        val defaultTokenId = getFirstToken(walletService)?.id
        if (defaultTokenId != null) {
            paymentService.defaultToken = defaultTokenId
        }
    }

    private fun getFirstToken(walletService: WalletService): WaToken? {
        val cardId = walletService.cards.firstOrNull()?.id
        if (cardId != null) {
            return walletService.getTokens(cardId).firstOrNull()
        }
        return null
    }

    private fun isDefaultToken(
        nfcPaymentService: NfcPaymentService,
        tokenIdList: List<WaTokenId>
    ): Boolean {
        return tokenIdList.any { it == nfcPaymentService.defaultToken }
    }

    /** 🔑 Dispose all ongoing subscriptions when plugin is destroyed */
    fun clear() {
        disposables.clear()
    }
}