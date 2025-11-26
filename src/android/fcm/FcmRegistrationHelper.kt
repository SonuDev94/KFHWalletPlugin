package com.aub.mobilebanking.phone.eg.fcm

import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.idemia.wa.api.Failure
import com.idemia.wa.api.fcm.*
import com.idemia.wa.api.wms.PairingService
import java.net.URI
import java.util.*

class FcmRegistrationHelper private constructor(private val pairingService: PairingService,private val fcmAccountNumber : String) {

    fun sendRegistrationMessage(fcmService: FcmService, messageReceiver: (String) -> Unit = {}) {
        messageReceiver("starting FCM registration")
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                AppLogger.e(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            AppLogger.d(TAG, "Fetching FCM registration token successful.", task.exception)
            val token = task.result
            registerToNotificationServer(fcmService, token, messageReceiver)
        })
    }

    private fun registerToNotificationServer(
        fcmService: FcmService,
        fcmToken: String,
        messageReceiver: (String) -> Unit
    ) {
        val fcmRegistration = createFcmRegistrationMessage(fcmToken)
        fcmService.registerInNotificationServer(fcmRegistration, object : FcmListener {
            override fun onFailure(p0: Failure?) {
                messageReceiver("FAILED to register in FCM!")
            }

            override fun onFcmRegistrationSuccess() {
                messageReceiver("Registered in FCM")
            }
        })
    }

    private fun createFcmRegistrationMessage(fcmToken: String): WaFcmRegistration {
        val props = WaFcmTransportProperties(fcmToken, fcmAccountNumber, emptyMap())
        val addressing = WaFcmAddressing(
            UUID.randomUUID().toString(),
            URI.create("wallet://" + pairingService.walletId.value),
            URI.create("ns://notificationserver")
        )
        return WaFcmRegistration(
            addressing,
            URI.create("wallet://" + pairingService.walletId.value),
            props
        )
    }

    companion object {

        private val TAG = FcmRegistrationHelper::class.java.simpleName

        @Volatile
        private var INSTANCE: FcmRegistrationHelper? = null

        fun getInstance(pairingService: PairingService, fcmAccountNumber: String): FcmRegistrationHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FcmRegistrationHelper(pairingService,fcmAccountNumber).also { INSTANCE = it }
            }
    }
}

