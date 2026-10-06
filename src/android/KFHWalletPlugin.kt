package com.aub.mobilebanking.phone.eg;

import android.app.Activity
import android.app.Application
import android.content.Context
import com.aub.mobilebanking.phone.eg.enrollment.EnrollmentManager
import com.aub.mobilebanking.phone.eg.model.EnrollmentData
import com.aub.mobilebanking.phone.eg.model.InitialData
import com.aub.mobilebanking.phone.eg.repository.DefaultWaServicesRepository
import com.aub.mobilebanking.phone.eg.repository.WaServicesRepository
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.idemia.wa.api.WaCardId
import com.idemia.wa.api.WaCdCvmType
import com.idemia.wa.api.WaTokenId
import com.idemia.wa.api.wms.WaIdvOption
import com.idemia.wa.api.wms.WaIdvType
import com.idemia.wa.api.wms.WaOtp
import com.idemia.wa.api.wms.WaTokenPurpose
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs
import org.apache.cordova.PluginResult

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class KFHWalletPlugin : CordovaPlugin() {
    companion object {
        lateinit var appContext: Context
        lateinit var activity: Activity
        lateinit var application: Application
    }

    private lateinit var waServicesRepository: WaServicesRepository
    private var enrollmentManager: EnrollmentManager? = null
    private var eventCallbackCtx: CallbackContext? = null


    override fun pluginInitialize() {
        super.pluginInitialize()
        // WaAppSystemContext.ensureBksKeyStoreAvailable()
        cordova?.let {
            activity = it.activity
            appContext = it.context
            application = it.context.applicationContext as Application
            waServicesRepository = DefaultWaServicesRepository()
            if (enrollmentManager == null) {
                enrollmentManager =
                    EnrollmentManager(waServicesRepository, ::sendEvent)
            }
        }

        AppLogger.d("MyCalPlugin", "Context, activity and app initialized globally")
    }

    override fun execute(
        action: String?,
        args: JSONArray?,
        callbackContext: CallbackContext?
    ): Boolean {
        // return super.execute(action, args, callbackContext)
        return when (action) {
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
                                opaque = obj?.optString("opaque"),
                                iin = obj?.optString("iin"),
                                pan = obj?.optString("pan"),
                                expDateMonth = obj?.optString("expDateMonth"),
                                expDateYear = obj?.optString("expDateYear"),
                            )

                            // 2. Convert it to WaEnrollmentParams
                            val params = data.mapToExpirationDateParams().waEnrollmentParams
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

            else -> false
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

}
