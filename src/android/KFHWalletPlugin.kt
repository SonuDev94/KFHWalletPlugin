package com.aub.mobilebanking.phone.eg

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.aub.mobilebanking.phone.eg.fcm.WaFirebaseMessagingService
import com.aub.mobilebanking.phone.eg.model.ActionOnStart
import com.aub.mobilebanking.phone.eg.model.EnrollmentData
import com.aub.mobilebanking.phone.eg.model.InitialData
import com.aub.mobilebanking.phone.eg.nfc.NfcPaymentManager
import com.aub.mobilebanking.phone.eg.payment.PaymentManager
import com.aub.mobilebanking.phone.eg.repository.DefaultWaServicesRepository
import com.aub.mobilebanking.phone.eg.repository.WaServicesRepository
import com.aub.mobilebanking.phone.eg.enrollment.EnrollmentManager
import com.aub.mobilebanking.phone.eg.transaction.TransactionData
import com.aub.mobilebanking.phone.eg.userpresence.UserPresentBroadcastReceiver
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.aub.mobilebanking.phone.eg.utils.Hex
import com.aub.mobilebanking.phone.eg.utils.NetworkCheck
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.idemia.wa.api.WaCardId
import com.idemia.wa.api.WaCdCvmType
import com.idemia.wa.api.WaDisplayData
import com.idemia.wa.api.WaToken
import com.idemia.wa.api.WaTokenId
import com.idemia.wa.api.WaTokenStatus
import com.idemia.wa.api.nfc.WaNfcTransactionOutcome
import com.idemia.wa.api.wms.WaIdvOption
import com.idemia.wa.api.wms.WaIdvType
import com.idemia.wa.api.wms.WaOtp
import com.idemia.wa.api.wms.WaTokenPurpose
import io.reactivex.rxjava3.disposables.Disposable
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * This class echoes a string called from JavaScript.
 */
class KFHWalletPlugin : CordovaPlugin() {
    companion object {
        lateinit var appContext: Context
        lateinit var activity: Activity
        lateinit var application: Application
    }

    private lateinit var waServicesRepository: WaServicesRepository
    private var enrollmentManager: EnrollmentManager? = null
    private var eventCallbackCtx: CallbackContext? = null
    private lateinit var nfcPaymentManager: NfcPaymentManager
    private var nfcDisposable: Disposable? = null
    private var paymentManager: PaymentManager? = null

    override fun pluginInitialize() {
        super.pluginInitialize()
        cordova?.let {
            activity = it.activity
            appContext = it.context
            application = it.context.applicationContext as Application
            waServicesRepository = DefaultWaServicesRepository()
            nfcPaymentManager = NfcPaymentManager(it.context)
            paymentManager = PaymentManager(it.context, waServicesRepository, ::sendEvent)
            if (enrollmentManager == null) {
                enrollmentManager =
                    EnrollmentManager(waServicesRepository, ::sendEvent)
            }
            val userPresentBroadcastReceiver = UserPresentBroadcastReceiver(application)
            userPresentBroadcastReceiver.register()
            userPresentBroadcastReceiver.clearLastAuthenticationTimestampIfOreoOrHigher()
        }

        AppLogger.d("MyCalPlugin", "Context, activity and app initialized globally")
    }

    override fun execute(
        action: String?,
        args: JSONArray?,
        callbackContext: CallbackContext?
    ): Boolean {

        return when (action) {
            "registerNotification" -> {
                eventCallbackCtx = callbackContext
                WaFirebaseMessagingService.jsCallback = callbackContext
                cordova.threadPool.execute {
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                sendEvent("NEW_TOKEN_EVENT", token)
                            } else {
                                sendError(task.exception?.message ?: "Token error")
                            }
                        }
                }
                true
            }

//            "checkInternetConnection" -> {
//                eventCallbackCtx = callbackContext
//                cordova.threadPool.execute {
//                    if (!NetworkCheck.isInternetAvailable(cordova.activity.applicationContext)) {
////                        callbackContext?.error("No internet connection available.")
//                        sendError("No internet connection available.")
//                        return@execute
//                    }
//
//                    if (!NetworkCheck.isVpnActive(cordova.activity.applicationContext)) {
////                        callbackContext?.error("VPN connection is required.")
//                        sendError("VPN connection is required.")
//                        return@execute
//                    }
////                    callbackContext?.success("Internet + VPN OK")
//                    sendEvent("InternetResponse", "Internet + VPN OK")
//                }
//                true
//            }

            "enroll" -> {
                cordova.threadPool.execute {
                    try {
                        if (args != null && args.length() > 0) {
                            val obj = args.getJSONObject(0)
                            // 1. Build EnrollmentData from JSON
                            val data = EnrollmentData(
                                authCode = obj?.getString("authCode") ?: "123",
                                tokenPurpose = WaTokenPurpose.valueOf(
                                    obj?.getString("tokenPurpose") ?: "NFC"
                                ),
                                actionOnStart = ActionOnStart.valueOf(
                                    obj?.getString("actionOnStart") ?: "ENROLL_WITH_EXPIRATION_DATE"
                                ),
                                opaque = obj?.optString("opaque"),
                                iin = obj?.optString("iin"),
                                pan = obj?.optString("pan"),
                                expDateMonth = obj?.optString("expDateMonth"),
                                expDateYear = obj?.optString("expDateYear"),
                                birthDateDay = obj?.optString("birthDateDay"),
                                birthDateMonth = obj?.optString("birthDateMonth"),
                                birthDateYear = obj?.optString("birthDateYear")
                            )

                            // 2. Convert it to WaEnrollmentParams
                            val params = when (data.actionOnStart) {
                                ActionOnStart.ENROLL_WITH_BIRTH_DATE ->
                                    data.mapToBirthDateParams().waEnrollmentParams

                                ActionOnStart.ENROLL_WITH_EXPIRATION_DATE ->
                                    data.mapToExpirationDateParams().waEnrollmentParams
                            }
                            eventCallbackCtx = callbackContext
                            //enroll()
                            enrollmentManager?.startEnrollment(params, { error ->
                                sendError("Error in enrollment: ${error.message}")
                                println("Error In Enroll ${error}")
                            })
                        } else {
                            sendError("No arguments received for enroll()")
                        }

                    } catch (e: Exception) {
                        sendError("Error parsing args: ${e.message}")
                    }
                }

                true
            }
            /*"replenish" -> {
                replenish()
                true
            }*/
            "onCdCvmTypeSelected" -> {
                val type = WaCdCvmType.valueOf(args?.getString(0) ?: "DEVICE")
                enrollmentManager?.selectCdCvmType(type)
                true
            }

            "submitTokenPurpose" -> {
                val purpose = WaTokenPurpose.valueOf(args?.getString(0) ?: "DEVICE")
                val selectedTokenPurposes: MutableSet<WaTokenPurpose> = mutableSetOf(purpose)
                enrollmentManager?.submitTokenPurpose(selectedTokenPurposes)
                true
            }

            "submitIdvOption" -> {
                val obj = args?.getJSONObject(0)
                try {
                    val id = obj?.getString("id")
                    val type = WaIdvType.valueOf(obj?.getString("type") ?: "OTP_SMS")
                    val hint = obj?.optString("hint", "")

                    val waOption = WaIdvOption(id, type, hint) // adjust constructor
                    enrollmentManager?.submitIdvOption(waOption)
                    sendEvent("idvOptionSubmitted", obj.toString())
                } catch (e: Exception) {
                    sendError("Invalid IDV option JSON: ${e.message}")
                }
                true
            }

            "submitOtp" -> {
                cordova.threadPool.execute {
                    enrollmentManager?.submitOtp(WaOtp((args?.getString(0) ?: "").toCharArray()))
                }
                true
            }

            "acceptTnc" -> {
                cordova.threadPool.execute {
                    enrollmentManager?.acceptTnc()
                }
                true
            }

            "getWalletCardsMaxCount" -> {
                cordova.threadPool.execute {
                    eventCallbackCtx = callbackContext
                    enrollmentManager?.getWalletCardsMaxCount(onError = {
                        sendError("Error getting wallet cards max count: ${it.message}")
                    })
                }
                return true
            }

            "getCardsWithEnrollmentStatus" -> {
                cordova.threadPool.execute {
                    eventCallbackCtx = callbackContext
                    enrollmentManager?.getCardsWithEnrollmentStatus(
                        onSuccess = { cardList ->
                            try {
                                val jsonArray = JSONArray()
                                cardList.forEach { ct ->
                                    val obj = JSONObject()
                                    obj.put("id", ct.card.id.value.toString())
                                    obj.put("cardPan", ct.card.metadata.lastDigits ?: "")
                                    obj.put("cardExp", ct.card.metadata.expDate ?: "")
                                    obj.put("status", ct.status)
                                    obj.put("adapterPosition", ct.adapterPosition)
                                    val tokenIdArray = JSONArray()
                                    ct.tokenList.forEach { token ->
                                        val t = JSONObject().apply {
                                            put("tokenId", token.id.value)
                                            put("tokenStatus", token.status.toString())
                                            put("lastDigits", token.displayData?.lastDigits ?: "")
                                            put("expDate", token.displayData?.expDate ?: "")
                                        }
                                        tokenIdArray.put(t)
                                    }
                                    obj.put("tokens", tokenIdArray)
                                    jsonArray.put(obj)
                                }
                                sendEvent("getCardsResponse", jsonArray)
                            } catch (e: Exception) {
                                sendError("JSON mapping failed: ${e.message}")
                            }
                        },
                        onError = { err ->
                            sendError("Error fetching cards: ${err.message}")
                        }
                    )
                }
                true
            }

            "getCardDetails" -> {
                cordova.threadPool.execute {
                    try {
                        if (args != null && args.length() > 0) {
                            val obj = args.getJSONObject(0)
//                            AppLogger.d("KFHWalletPlugin", "object: $obj");
//                            AppLogger.d("KFHWalletPlugin", "cardId: ${obj.getString("cardId")}")
                            val type = object : TypeToken<List<WaTokenId>>() {}.type
                            val tokenIdList: List<WaTokenId> =
                                Gson().fromJson(obj.getString("tokenIdJson"), type)

                            // Build InitialData
                            val initialData = InitialData(
                                cardId = WaCardId(obj.getString("cardId")),
                                cardPan = obj.optString("cardPan"),
                                cardExp = obj.optString("cardExp"),
                                tokenIdList = tokenIdList
                            )

                            eventCallbackCtx = callbackContext
//                            AppLogger.d("KFHWalletPlugin", "initialData: $initialData")
//                            AppLogger.d("KFHWalletPlugin", "tokenIdList: ${initialData.tokenIdList} and FirstToken:${initialData.tokenIdList[0].value} and FirstTokenValue: ${initialData.tokenIdList[0].value}")
                            enrollmentManager?.loadCardDetails(
                                initialData = initialData, onSuccess = { card ->
                                    try {
                                        val cardJson = JSONObject().apply {
                                            put("status", card.status?.name ?: "UNKNOWN")
                                            put("cardPan", card.cardPan)
                                            put("cardExp", card.cardExp)
                                            put("default", card.default ?: false)

                                            // Serialize each token
                                            val tokensArray = JSONArray()
                                            card.tokenList.forEach { token ->
                                                val tokenJson = JSONObject().apply {
                                                    put("tokenId", token.tokenId)
                                                    put("status", token.status?.name ?: "UNKNOWN")
                                                    put("tokenPan", token.tokenPan ?: "")
                                                    put("tokenExp", token.tokenExp ?: "")
                                                    put("default", token.default)
                                                }
                                                tokensArray.put(tokenJson)
                                            }
                                            put("tokens", tokensArray)
                                        }

                                        // Send back to JS
                                        sendEvent("getCardDetailsSuccess", cardJson)
                                    } catch (ex: Exception) {
                                        sendError("Error creating JSON: ${ex.message}")
                                    }
                                }, onError = {
                                    sendError("Error loading card details: ${it.message}")
                                })
                        } else {
                            sendError("getCardDetails: Missing input arguments")
                        }
                    } catch (e: Exception) {
                        sendError("getCardDetails failed: ${e.message}")
                    }
                }
                true
            }

            "suspendCard" -> {
                cordova.threadPool.execute {
                    try {
                        AppLogger.d("KFHWalletPlugin", "suspendCard called")
                        if (args != null && args.length() > 0) {
                            val obj = args.getJSONObject(0)
                            AppLogger.d("KFHWalletPlugin", "suspendCard request data + $obj")
                            // Convert tokenIdList JSON → List<WaTokenId>
                            val type = object : TypeToken<List<WaTokenId>>() {}.type
                            val tokenIdList: List<WaTokenId> =
                                Gson().fromJson(obj.getString("tokenIdJson"), type)

                            // Build InitialData
                            val initialData = InitialData(
                                cardId = WaCardId(obj.getString("cardId")),
                                cardPan = obj.optString("cardPan"),
                                cardExp = obj.optString("cardExp"),
                                tokenIdList = tokenIdList
                            )
                            eventCallbackCtx = callbackContext
                            enrollmentManager?.suspendCard(
                                initialData = initialData,
                                onSuccess = {
                                    sendEvent("suspendCardSuccess", "Selected tokens suspended")
                                },
                                onError = { error ->
                                    sendError("Error suspending cards: ${error.message}")
                                }
                            )
                        } else {
                            sendError("SuspendCard requires arguments")
                        }
                    } catch (e: Exception) {
                        sendError("SuspendCard arg parsing failed: ${e.message}")
                    }
                }
                true
            }

            "deleteToken" -> {
                cordova.threadPool.execute {
                    try {
                        val tokenIdStr = args?.getString(0) ?: ""
                        if (tokenIdStr.isEmpty()) {
                            sendError("deleteToken: tokenId missing")
                            return@execute
                        }

                        val tokenId = WaTokenId(tokenIdStr)
                        eventCallbackCtx = callbackContext
                        enrollmentManager?.deleteToken(
                            tokenId = tokenId,
                            onSuccess = {
                                sendEvent(
                                    "deleteTokenSuccess",
                                    "Token deleted successfully: $tokenIdStr"
                                )
                            },
                            onError = { error ->
                                sendError("Error deleting token: ${error.message}")
                            }
                        )
                    } catch (e: Exception) {
                        sendError("deleteToken arg parsing failed: ${e.message}")
                    }
                }
                true
            }

            "deleteCard" -> {
                cordova.threadPool.execute {
                    try {
                        if (args != null && args.length() > 0) {
                            val obj = args.getJSONObject(0)
                            AppLogger.d("KFHWalletPlugin", "delete request data + $obj")
                            // Convert tokenIdList JSON → List<WaTokenId>
                            val type = object : TypeToken<List<WaTokenId>>() {}.type
                            val tokenIdList: List<WaTokenId> =
                                Gson().fromJson(obj.getString("tokenIdJson"), type)

                            // Build InitialData
                            val initialData = InitialData(
                                cardId = WaCardId(obj.getString("cardId")),
                                cardPan = obj.optString("cardPan"),
                                cardExp = obj.optString("cardExp"),
                                tokenIdList = tokenIdList
                            )
                            eventCallbackCtx = callbackContext
                            enrollmentManager?.deleteCard(
                                initialData = initialData,
                                onSuccess = {
                                    sendEvent("deleteCardSuccess", "Selected tokens deleted")
                                },
                                onError = { error ->
                                    sendError("Error deleting cards: ${error.message}")
                                }
                            )
                        } else {
                            sendError("DeleteCard requires arguments")
                        }
                    } catch (e: Exception) {
                        sendError("DeleteCard arg parsing failed: ${e.message}")
                    }
                }
                true
            }

            "changeTokenDefaultState" -> {
                cordova.threadPool.execute {
                    try {
                        val tokenIdStr = args?.getString(0) ?: ""
                        val isDefault = args?.getBoolean(1) ?: false

                        if (tokenIdStr.isEmpty()) {
                            sendError("changeTokenDefaultState: tokenId missing")
                            return@execute
                        }
                        eventCallbackCtx = callbackContext
                        val tokenId = WaTokenId(tokenIdStr)

                        enrollmentManager?.changeTokenDefaultState(
                            tokenId = tokenId,
                            isDefault = isDefault,
                            onSuccess = {
                                sendEvent(
                                    "changeTokenDefaultStateSuccess",
                                    "Default state changed: tokenId=$tokenIdStr, isDefault=$isDefault"
                                )
                            },
                            onError = { error ->
                                sendError("Error changing default state: ${error.message}")
                            }
                        )
                    } catch (e: Exception) {
                        sendError("changeTokenDefaultState arg parsing failed: ${e.message}")
                    }
                }
                true
            }

            "resumeToken" -> {
                cordova.threadPool.execute {
                    try {
                        val tokenIdStr = args?.getString(0) ?: ""
                        if (tokenIdStr.isEmpty()) {
                            sendError("resumeToken: tokenId missing")
                            return@execute
                        }
                        eventCallbackCtx = callbackContext
                        val tokenId = WaTokenId(tokenIdStr)

                        enrollmentManager?.resumeToken(
                            tokenId = tokenId,
                            onSuccess = {
                                sendEvent(
                                    "resumeTokenSuccess",
                                    "Token resumed successfully: $tokenIdStr"
                                )
                            },
                            onError = { error ->
                                sendError("Error resuming token: ${error.message}")
                            }
                        )
                    } catch (e: Exception) {
                        sendError("resumeToken arg parsing failed: ${e.message}")
                    }
                }
                true
            }

            "checkNfcStatus" -> {
                cordova.threadPool.execute {
                    try {
                        eventCallbackCtx = callbackContext
//                        val manager = NfcPaymentManager(cordova.context)
                        val result = mapOf(
                            "exists" to nfcPaymentManager.nfcExists(),
                            "enabled" to nfcPaymentManager.isNfcEnabled()
                        )
                        sendEvent("checkNfcStatus", Gson().toJson(result))
                    } catch (e: Exception) {
                        sendError("Error checking NFC status: ${e.message}")
                    }
                }
                true
            }

            "openNfcSettings" -> {
                cordova.threadPool.execute {
                    try {
                        eventCallbackCtx = callbackContext
                        nfcPaymentManager.openNfcSettings()
                        sendEvent("openNfcSettings", "Opened NFC settings")
                    } catch (e: Exception) {
                        sendError("Error opening NFC settings: ${e.message}")
                    }
                }
                true
            }

            "isDefaultPaymentApp" -> {
                cordova.threadPool.execute {
                    try {
                        eventCallbackCtx = callbackContext
                        val result = paymentManager?.isDefaultPaymentApp()
                        sendEvent("isDefaultPaymentApp", result.toString())
                    } catch (e: Exception) {
                        sendError("Error checking default payment app: ${e.message}")
                    }
                }
                true
            }

            "askDefaultPaymentApp" -> {
                cordova.activity.runOnUiThread {
                    try {
                        eventCallbackCtx = callbackContext
                        if (nfcPaymentManager.nfcExists()) {
                            if (paymentManager?.isDefaultPaymentApp() == false) {
                                paymentManager?.askToBeDefaultPaymentApp(
                                    cordova.activity,
                                    onError = {
                                        sendError("Error requesting default payment app: ${it.message}")
                                    })
                                sendEvent(
                                    "defaultPaymentAppSuccess",
                                    "Request sent to change default payment app"
                                )
                            } else {
                                sendEvent(
                                    "defaultPaymentAppSuccess",
                                    "Already default payment app"
                                )
                            }

                        } else {
                            sendError("NFC not available on this device")
                        }
                    } catch (e: Exception) {
                        sendError("Error requesting default payment app: ${e.message}")
                    }
                }
                true
            }

            // ✅ Observe NFC Settings Listener
            "observeNFCSettingsListener" -> {
                cordova.threadPool.execute {
                    eventCallbackCtx = callbackContext
                    nfcDisposable?.dispose() // ensure only one subscription
                    sendEvent("ObserveNFCSettingsListener", "started")
                    nfcDisposable = nfcPaymentManager.observeNfcSettingsListener()
                        .subscribe({ state ->
                            sendEvent("NfcStateChanged", state.name)
                        }, { error ->
                            sendError("observeNFCSettingsListener error: ${error.message}")
                        })
                }
                true
            }

            // ✅ Unregister NFC Settings Listener
            "unregisterNfcSettingsListener" -> {
                eventCallbackCtx = callbackContext
                nfcDisposable?.dispose()
                nfcDisposable = null
                sendEvent("unregisterNfcSettingsListener", "stopped")
                true
            }

            "preparePayment" -> {
                cordova.threadPool.execute {
                    try {
                        eventCallbackCtx = callbackContext
                        AppLogger.d("KFHWalletPlugin", "preparePayment args = $args")
                        if (args != null && args.length() > 0) {
                            val waTokenArray = args.getJSONArray(0)
                            val waTokenList = mutableListOf<WaToken>()
                            for (i in 0 until waTokenArray.length()) {
                                val obj = waTokenArray.getJSONObject(i)
                                val tokenId = obj.getString("tokenId")
                                val tokenStatus = obj.getString("tokenStatus")
                                val lastDigits = obj.getString("lastDigits")
                                val expDate = obj.getString("expDate")
                                val token = WaToken.Builder()
                                    .setId(WaTokenId(tokenId))
                                    .setStatus(WaTokenStatus.valueOf(tokenStatus))
                                    .setDisplayData(WaDisplayData(expDate, lastDigits))
                                    .build()
                                waTokenList.add(token)
                            }
                            paymentManager?.onCardSelectionOrPaymentStatusChanged(waTokenList)
                        }
                    } catch (e: Exception) {
                        sendError("preparePayment failed: ${e.message}")
                    }
                }
                true
            }

            "checkBiometricAvailability" -> {
                cordova.activity.runOnUiThread {
                    try {
                        eventCallbackCtx = callbackContext
                        val biometricManager =
                            androidx.biometric.BiometricManager.from(cordova.context)

                        val strongResult = biometricManager.canAuthenticate(
                            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )

                        val weakResult = biometricManager.canAuthenticate(
                            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )

                        val credentialResult = biometricManager.canAuthenticate(
                            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )

                        val result = JSONObject().apply {
                            put(
                                "strongBiometric",
                                strongResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                            )
                            put(
                                "weakBiometric",
                                weakResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                            )
                            put(
                                "deviceCredential",
                                credentialResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                            )
                            put("strongResult", strongResult)
                            put("weakResult", weakResult)
                            put("credentialResult", credentialResult)

                            val statusMessage = when (strongResult) {
                                androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> "Biometric authentication available"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric credentials enrolled"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometric not supported"
                                else -> "Unknown biometric status"
                            }
                            put("statusMessage", statusMessage)
                            put(
                                "available",
                                strongResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS ||
                                        weakResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS ||
                                        credentialResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                            )
                        }

                        sendEvent("checkBiometricAvailability", result)
                    } catch (e: Exception) {
                        sendError("Error checking biometric availability: ${e.message}")
                    }
                }
                true
            }

            "proceedToTransactionConfirmation" -> {
                cordova.activity.runOnUiThread {
                    try {
                        val tokenIdStr = args?.getString(0) ?: ""
                        if (tokenIdStr.isEmpty()) {
                            sendError("proceedToTransactionConfirmation: tokenId missing")
                            return@runOnUiThread
                        }
                        val tokenId = WaTokenId(tokenIdStr)

                        eventCallbackCtx = callbackContext
                        paymentManager?.proceedToTransactionConfirmation(tokenId)
                    } catch (e: Exception) {
                        sendError("Error in proceedToTransactionConfirmation: ${e.message}")
                    }
                }
                true
            }

            "startAutomaticPaymentListener" -> {
                cordova.threadPool.execute {
                    eventCallbackCtx = callbackContext
                    paymentManager?.startAutomaticPaymentListener()
                }
                true
            }

            "stopAutomaticPaymentListener" -> {
                cordova.threadPool.execute {
                    eventCallbackCtx = callbackContext
                    paymentManager?.stopAutomaticPaymentListener()
                }
                true
            }

            "processPaymentResult" -> {
                cordova.threadPool.execute {
                    val tokenIdStr = args?.getString(0) ?: ""
                    if (tokenIdStr.isEmpty()) {
                        sendError("proceedToTransactionConfirmation: tokenId missing")
                        return@execute
                    }
                    val tokenId = WaTokenId(tokenIdStr)
                    eventCallbackCtx = callbackContext
                    paymentManager?.processPaymentResult(
                        tokenId = tokenId,
                        transactionOutcome = WaNfcTransactionOutcome.WALLET_ACTION_REQUIRED,
                        transactionData = TransactionData(
                            "44656D6F204D65726368616E74",
                            "100.00",
                            "414",
                            System.currentTimeMillis()
                        )
                    )
                }
                true
            }
            "transactionHistory" -> {
                cordova.threadPool.execute {
                    val tokenIdStr = args?.getString(0) ?: ""
                    if (tokenIdStr.isEmpty()) {
                        sendError("proceedToTransactionHistory: tokenId missing")
                        return@execute
                    }
                    eventCallbackCtx = callbackContext
                    paymentManager?.fetchTransactionsHistory(
                        tokenId = tokenIdStr,
                    )
                }
                true
            }


            else -> false
        }
    }

    /*   private fun initialize() {
        WalletAgentApi.initialize(cordova.activity.application, object : InitializationListener {
            override fun onSuccess() {
                callbackCtx?.success("SDK Initialized")
            }

            override fun onFailure(e: Exception?) {
                callbackCtx?.error("Init failed: ${e?.message}")
            }
        })
    */


    /* private fun replenish() {
         val replService = WalletAgentApi.getInstance().getService(ReplenishmentService::class.java)
         replService.replenish(object: ReplenishmentListener {
             override fun onReplenishmentSuccess() {
                 callbackCtx?.success("Replenished")
             }
             override fun onFailure(reason: Int) {
                 callbackCtx?.error("Failed: $reason")
             }
         })
     }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == NfcPaymentManager.REQUEST_CODE_DEFAULT_PAYMENT_APP) {
            if (resultCode == Activity.RESULT_OK) {
                sendEvent("defaultPaymentAppSuccess", "App is now default payment app")
            } else {
                sendEvent("defaultPaymentAppFailed", "User denied or failed to set default")
            }
        }
    }

    private fun sendEvent(event: String, data: Any?) {
        val result = PluginResult(
            PluginResult.Status.OK,
            JSONObject().put("event", event).put("data", data)
        )
        result.keepCallback = true
        eventCallbackCtx?.sendPluginResult(result)
    }

    private fun sendError(message: String) {
        val result = PluginResult(PluginResult.Status.ERROR, message)
        result.keepCallback = true
        eventCallbackCtx?.sendPluginResult(result)
    }

    override fun onReset() {
        super.onReset()
        enrollmentManager?.clear()
        nfcDisposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        enrollmentManager?.clear()
        nfcDisposable?.dispose()
        paymentManager?.clear()
    }

}

