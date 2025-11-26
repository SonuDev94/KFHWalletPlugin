package com.aub.mobilebanking.phone.eg.payment

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.aub.mobilebanking.phone.eg.KFHWalletPlugin
import com.aub.mobilebanking.phone.eg.repository.WaServicesRepository
import com.aub.mobilebanking.phone.eg.screenLock.AndroidScreenLockVerifier
import com.aub.mobilebanking.phone.eg.screenLock.ScreenLockVerifier
import com.aub.mobilebanking.phone.eg.screenLock.UnlockScreenVerifier
import com.aub.mobilebanking.phone.eg.transaction.LastTransactionStatusRepository
import com.aub.mobilebanking.phone.eg.transaction.TransactionData
import com.aub.mobilebanking.phone.eg.transaction.TransactionHistoryItem
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.aub.mobilebanking.phone.eg.utils.scheduleIoThenMain
import com.idemia.wa.api.Failure
import com.idemia.wa.api.WaToken
import com.idemia.wa.api.WaTokenId
import com.idemia.wa.api.WaTokenStatus
import com.idemia.wa.api.nfc.NfcPaymentService
import com.idemia.wa.api.nfc.NfcTransactionListener
import com.idemia.wa.api.nfc.WaNfcTransactionDetails
import com.idemia.wa.api.nfc.WaNfcTransactionOutcome
import com.idemia.wa.api.nfc.WaPaymentInput
import com.idemia.wa.api.payment.WaCdCvmVerifiedType
import com.idemia.wa.api.wms.TransactionsHistoryListener
import com.idemia.wa.api.wms.WaTransactionHistoryEntry
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins.onError
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.Optional

class PaymentManager(
    private val context: Context,
    private val waServicesRepository: WaServicesRepository, // your repository
    private val sendEvent: (String, Any?) -> Unit
) {
    private val disposables = CompositeDisposable()
    private val lastStatusRepo = LastTransactionStatusRepository() // from uploaded files
    private val paymentAppUseCase = DefaultPaymentAppUseCase(context) // from uploaded files
    private var nextPaymentStatus: NextPaymentStatus = NextPaymentStatus.PAYMENT_TOKEN_NOT_SET

    private val TAG = "PaymentManager"
    private val nfcPaymentService by lazy { waServicesRepository.nfcPaymentService }
    private val transactionsHistoryService by lazy { waServicesRepository.transactionHistoryService }

    private var tokenId: WaTokenId? = null
    private var screenLockedWhenWalletActionRequired: Boolean = false
    private val screenLockVerifier: ScreenLockVerifier = AndroidScreenLockVerifier(context)

    /**
     * Prepare a payment (call SDK preparePayment).
     * This will attach DefaultPaymentListener which will push transaction events to sendEvent.
     */


    fun onCardSelectionOrPaymentStatusChanged(tokenList: List<WaToken>) {
        // to assure proper payment token selection, previous payment should be cancelled
        val dis = waServicesRepository.nfcPaymentService.subscribeOn(Schedulers.io())
            .flatMapCompletable { nfcPaymentService ->
                return@flatMapCompletable if (nextPaymentStatus == NextPaymentStatus.PAYMENT_TOKEN_SET) {
                    cancelPaymentIfNotPending(
                        nfcPaymentService,
                        NextPaymentStatus.PAYMENT_CANCELLED_ON_TOKEN_CHANGE
                    )
                    Completable.complete()
                } else {
                    setPayment(nfcPaymentService, tokenList)
                }
            }.subscribe({}, { err ->
                Log.e(TAG, "Error obtaining nfcPaymentService", err)
                sendEvent("preparePaymentError", err.message)
            })
        disposables.add(dis)
    }

    val EXTRA_MERCHANT_NAME = "EXTRA_MERCHANT_NAME"
    val EXTRA_TRANSACTION_DATE = "EXTRA_TRANSACTION_DATE"
    val EXTRA_TRANSACTION_AMOUNT_WITH_CURRENCY = "EXTRA_TRANSACTION_AMOUNT_WITH_CURRENCY"

    private val paymentStatusListener: PaymentStatusListener by lazy {
        object : DefaultPaymentStatusListener() {
            override fun onWalletActionRequired(transactionDetails: WaNfcTransactionDetails) {
//                WalletActionNavigateToTransactionProcessing(requireContext())
//                    .onWalletActionRequired(transactionDetails)
                processPaymentResult(
                    WaTokenId(transactionDetails.transactionInformation().tokenId()),
                    WaNfcTransactionOutcome.valueOf(transactionDetails.transactionOutcome().name),
                    TransactionData(
                        transactionDetails.transactionInformation().merchantName(),
                        transactionDetails.transactionInformation().amount().toString(),
                        transactionDetails.transactionInformation().currencyCode(),
                        transactionDetails.transactionInformation().transactionDate().time
                            ?: System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private val defaultPaymentListener: DefaultPaymentListener by lazy {
        DefaultPaymentListener(
            context = context,
            paymentStatusListener = paymentStatusListener
        )
    }

    private fun setPayment(
        nfcPaymentService: NfcPaymentService,
        tokenList: List<WaToken>
    ): Completable {
        return Completable.create { emitter ->
            try {
                tokenList.forEach { token ->
                    nextPaymentStatus = when (token.status) {
                        WaTokenStatus.ACTIVE -> {
                            nfcPaymentService.preparePayment(
                                WaPaymentInput(token.id),
                                defaultPaymentListener
                            )
                            sendEvent(
                                "preparePaymentSuccess",
                                "preparePayment called for token=${token.id}"
                            )
                            AppLogger.d(TAG, "preparePayment called for token=${token.id}")
                            NextPaymentStatus.PAYMENT_TOKEN_SET
                        }

                        else -> NextPaymentStatus.PAYMENT_TOKEN_NOT_SET
                    }
                }
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
                sendEvent("preparePaymentError", e.message)
            }
        }
    }

    private fun cancelPaymentIfNotPending(
        nfcPaymentService: NfcPaymentService, afterCancelStatus: NextPaymentStatus
    ) {
        try {
            if (!nfcPaymentService.isPaymentInProgress) {
                nfcPaymentService.cancelPayment()
                nextPaymentStatus = afterCancelStatus
                sendEvent("cancelPaymentSuccess", "cancelPayment invoked")
            }
        } catch (e: Exception) {
            Log.e(TAG, "cancelPayment error", e)
            sendEvent("cancelPaymentError", e.message)
        }
    }

    /**
     * Stop payment (if SDK exposes stop)
     */
    /*  fun stopPayment() {
          val d = waServicesRepository.nfcPaymentService
              .scheduleIoThenMain()
              .subscribe({ nfcPaymentService ->
                  try {
                      // if SDK has stopPayment / endListening / similar
                      nfcPaymentService.stop() // replace with actual method if different
                      sendEvent("stopPaymentSuccess", "stopPayment invoked")
                  } catch (e: Exception) {
                      Log.e(TAG, "stopPayment error", e)
                      sendEvent("stopPaymentError", e.message)
                  }
              }, { err ->
                  Log.e(TAG, "stopPayment service error", err)
                  sendEvent("stopPaymentError", err.message)
              })
          disposables.add(d)
      }*/

    /**
     * Helper to check if app is default payment app (delegates to DefaultPaymentAppUseCase)
     */
    fun isDefaultPaymentApp(): Boolean {
        return paymentAppUseCase.isDefaultPaymentApp()
    }

    /**
     * Ask user to set default payment app - activity must handle onActivityResult (plugin side)
     */
    fun askToBeDefaultPaymentApp(activity: Activity, onError: (Throwable) -> Unit) {
        paymentAppUseCase.askToBeDefaultPaymentApp(activity, onError)
    }


    fun processPaymentResult(
        tokenId: WaTokenId,
        transactionOutcome: WaNfcTransactionOutcome,
        transactionData: TransactionData
    ) {
        screenLockedWhenWalletActionRequired = false
        this.tokenId = tokenId
        when (transactionOutcome) {
            WaNfcTransactionOutcome.WALLET_ACTION_REQUIRED -> {
                screenLockedWhenWalletActionRequired = !screenLockVerifier.isScreenUnlocked()
//                mutableLocalAuthLiveData.value = transactionData
                var jsonObject = JSONObject()
                jsonObject.put(
                    EXTRA_MERCHANT_NAME,
                    transactionData.merchantName
                )
                jsonObject.put(
                    EXTRA_TRANSACTION_AMOUNT_WITH_CURRENCY,
                    transactionData.amountWithCurrency
                )
                jsonObject.put(
                    EXTRA_TRANSACTION_DATE,
                    transactionData.formattedDate
                )
                sendEvent("TransactionData", jsonObject)
            }

            else -> sendEvent("showMessage", "transactionOutcome: $transactionOutcome")
        }
    }

    fun proceedToTransactionConfirmation(tokenId: WaTokenId) {
        if (screenLockedWhenWalletActionRequired) {
            confirmAuth(tokenId, WaCdCvmVerifiedType.NO_CDCVM)
        } else {
            UnlockScreenVerifier.showSystemUnlockScreen(
                activity = KFHWalletPlugin.activity as FragmentActivity,
                onSuccess = { confirmAuth(tokenId, it) },
                onError = { errorCode, errMsg ->
                    AppLogger.d(TAG, "Biometric authentication error: $errorCode - $errMsg")
                    when (errorCode) {
                        BiometricPrompt.ERROR_CANCELED, BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User cancelled, treat as decline
                            sendEvent("authenticationCancelled", "User cancelled authentication")
                            declineAuth()
                        }

                        -1 -> {
                            // Biometric not available (our custom error code)
                            sendEvent("biometricNotAvailable", errMsg.toString())
                            // For "no finger enroll" error, we should still try to proceed with NO_CDCVM
                            if (errMsg.contains("enrolled", ignoreCase = true)) {
                                AppLogger.d(TAG, "No biometric enrolled, proceeding with NO_CDCVM")
                                confirmAuth(tokenId, WaCdCvmVerifiedType.NO_CDCVM)
                            } else {
                                // Other biometric unavailability issues - decline
                                declineAuth()
                            }
                        }

                        else -> {
                            // Real error (hardware/unexpected/etc)
                            sendEvent("biometricError", "Biometric Error ($errorCode): $errMsg")
                            declineAuth()
                        }
                    }
                },
                onFailed = {
                    AppLogger.d(TAG, "Biometric authentication failed")
                    sendEvent("authenticationFailed", "Authentication failed")
                    declineAuth()
                }
            )
        }
    }

    fun confirmAuth(tokenId: WaTokenId, cdcvmType: WaCdCvmVerifiedType) {
        System.currentTimeMillis().let {
            preparePaymentWithLocalAuth(tokenId, cdcvmType)
//            sendEvent("showLoading", "confirmAuth()");
        }
    }

    fun declineAuth() {
        AppLogger.d(TAG, "declineAuth() called - user cancelled authentication or error occurred")
        val dis = nfcPaymentService.scheduleIoThenMain().subscribe({ nfcPaymentService ->
            nfcPaymentService.cancelPayment()
            sendEvent("authenticationDeclined", "Payment authentication was declined")
//            mutableFinishTransactionLiveData.value = Event(Unit)
        }, { err ->
            onError(err)
            AppLogger.e(TAG, "Error during nfc payment cancellation.", err)
        })
        disposables.add(dis)
    }

    private fun preparePaymentWithLocalAuth(
        tokenId: WaTokenId,
        cdCvmVerifiedType: WaCdCvmVerifiedType
    ) {
        val paymentInput = WaPaymentInput(tokenId)
        val dis = nfcPaymentService.scheduleIoThenMain().subscribe({ nfcPaymentService ->
            nfcPaymentService.preparePayment(
                paymentInput,
                DefaultPaymentListener(
                    context,
                    isSecondTap = true,
                    isKeyguardAuthorized = true,
                    cdCvmVerifiedType = cdCvmVerifiedType,
                    paymentStatusListener = paymentStatusListener
                )
            )
            sendEvent(
                "preparePaymentSuccess",
                "preparePayment called for token=${tokenId}"
            )
        }, { err ->
            onError(err)
            AppLogger.e(TAG, "Error during payment preparation.", err)
        })
        disposables.add(dis)
    }

    fun startAutomaticPaymentListener() {
        changeListeningForAutomaticPayments(Optional.of(defaultPaymentListener))
    }

    fun stopAutomaticPaymentListener() {
        changeListeningForAutomaticPayments(Optional.empty())
    }

    private fun changeListeningForAutomaticPayments(listener: Optional<NfcTransactionListener>) {
        val dispo = nfcPaymentService.scheduleIoThenMain().subscribe({ nfcPaymentService ->
            if (listener.isPresent) {
                nfcPaymentService.registerAutomaticPaymentListener(listener.get())
                sendEvent(
                    "PaymentListenerChangeSuccess",
                    "Started listening for automatic payments"
                )
            } else {
                nfcPaymentService.unregisterAutomaticPaymentListener()
                sendEvent(
                    "PaymentListenerChangeSuccess",
                    "Stopped listening for automatic payments"
                )
            }

        }, { err ->
            sendEvent(
                "PaymentListenerChangeError",
                "Error during changing listening for automatic payments. $err"
            )
            AppLogger.e(TAG, "Error during changing listening for automatic payments.", err)
        })
        disposables.add(dispo)
    }

    fun fetchTransactionsHistory(
        tokenId: String,
        timestamp: Long? = null,
        count: Int? = null
    ) {
        val dispo = transactionsHistoryService.scheduleIoThenMain().subscribe(
            { service ->
                val transactionsHistoryListener = object : TransactionsHistoryListener {
                    override fun onTransactionHistoryFailure(failure: Failure) {
                        AppLogger.e(TAG, failure.exception.message ?: "")
                        sendEvent(
                            "TransactionHistoryError",
                            "Error during fetching history. ${failure.exception.message}"
                        )
                    }

                    override fun onTransactionHistoryFetched(
                        transactionsHistoryEntriesList: List<WaTransactionHistoryEntry>,
                        tokenId: WaTokenId
                    ) {
                        val list = transactionsHistoryEntriesList.map {
                            mapTransactionHistoryEntry(it)
                        }
                        val jsonArray = JSONArray()
                        list.forEach { item ->
                            val obj = JSONObject()
                            obj.put("date", item.date)       // or whatever getters WaIdvOption has
                            obj.put("merchantName", item.merchantName)
                            obj.put("transactionStatus", item.transactionStatus)
                            obj.put("amount", item.amount)
                            jsonArray.put(obj)
                        }
                        sendEvent("TransactionHistory", jsonArray)
                    }
                }
                service.fetchTransactionHistory(
                    WaTokenId(tokenId),
                    transactionsHistoryListener,
                    timestamp,
                    count
                )
            }, { throwable ->
                sendEvent(
                    "TransactionHistoryError",
                    "Error during fetching history. ${throwable.message}"
                )
                AppLogger.e(TAG, throwable.message ?: "")
            })

        disposables.add(dispo)
    }

    private fun mapTransactionHistoryEntry(it: WaTransactionHistoryEntry): TransactionHistoryItem {
        return TransactionHistoryItem(
            it.transactionTimestamp, it.merchant, it.status, "${it.totalAmount} ${it.currency}"
        )
    }


    fun clear() {
        disposables.clear()
    }

    private enum class NextPaymentStatus {
        PAYMENT_TOKEN_NOT_SET, PAYMENT_TOKEN_SET, PAYMENT_CANCELLED_ON_VIEW_GONE, PAYMENT_CANCELLED_ON_TOKEN_CHANGE
    }
}