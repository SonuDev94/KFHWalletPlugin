package com.aub.mobilebanking.phone.eg.fcm

import com.aub.mobilebanking.phone.eg.repository.DefaultWaServicesRepository
import com.aub.mobilebanking.phone.eg.repository.WaServicesRepository
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.aub.mobilebanking.phone.eg.utils.scheduleIoThenMain
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.idemia.wa.api.push.WaRemoteMessage
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginResult
import org.json.JSONObject
import java.lang.Exception

class WaFirebaseMessagingService : FirebaseMessagingService() {
    val NEW_TOKEN_EVENT = "NEW_TOKEN_EVENT"
    val MESSAGE_RECEIVED_EVENT = "MESSAGE_RECEIVED_EVENT"
    private val TAG = "MessagingService"
    private val disposables = CompositeDisposable()

    companion object {
        var jsCallback: CallbackContext? = null
    }

    private val wServicesRepository: WaServicesRepository by lazy {
        DefaultWaServicesRepository()
    }

    private val fcmNotificationProcessUseCase by lazy {
        return@lazy FcmNotificationProcessUseCase(
            applicationContext,
            wServicesRepository.remoteMessagingService
        )
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppLogger.d("FCM", "New Token: $token")

        // Send token to Cordova JS if callback is registered
        jsCallback?.let {
            val result = PluginResult(
                PluginResult.Status.OK,
                JSONObject().put("event", NEW_TOKEN_EVENT).put("data", token)
            )
            result.keepCallback = true
            it.sendPluginResult(result)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        AppLogger.d("FCM", "Message received: ${remoteMessage.data}")

        val notifications = NotificationParser.parseIncomingNotification(remoteMessage)
        val disposal = fcmNotificationProcessUseCase.processMessage(
            WaRemoteMessage(remoteMessage.data),
            notifications?.payloadType
        ).scheduleIoThenMain()
            .subscribe({}, { err -> })
        AppLogger.d(TAG, "Error during processing message.")
        disposables.add(disposal)

        val message = JSONObject()
        message.put("title", remoteMessage.notification?.title ?: "")
        message.put("body", remoteMessage.notification?.body ?: "")
        message.put("data", JSONObject(remoteMessage.data as Map<*, *>))

        jsCallback?.let {
            val result = PluginResult(
                PluginResult.Status.OK,
                JSONObject().put("event", MESSAGE_RECEIVED_EVENT).put("data", message)
            )
            result.keepCallback = true
            it.sendPluginResult(result)
        }
    }

    override fun onMessageSent(message: String) {
        super.onMessageSent(message)
        AppLogger.d(TAG, "onMessageSent() message: $message")
    }

    override fun onSendError(message: String, exception: Exception) {
        super.onSendError(message, exception)
        AppLogger.d(TAG, "onSendError() message: $message", exception)
    }

    private fun logMessage(remoteMessage: RemoteMessage?) {
        AppLogger.d(TAG, "From: ${remoteMessage?.from}, messageId: ${remoteMessage?.messageId}")
        val notification = remoteMessage?.notification
        AppLogger.d(TAG, "title: ${notification?.title}, body: ${notification?.body}")
        remoteMessage?.data?.keys?.forEach { key ->
            AppLogger.d(TAG, "key: $key")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}