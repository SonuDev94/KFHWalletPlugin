package com.aub.mobilebanking.phone.eg.fcm

import android.util.Log
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object NotificationParser {
    private const val NOTIFICATIONS_KEY = "notifications"
    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    fun parseIncomingNotification(remoteMessage: RemoteMessage?): Notifications? =
        try {
            remoteMessage?.data?.get(NOTIFICATIONS_KEY)?.let {
                moshi.adapter(Notifications::class.java).fromJson(it)
            }
        } catch (exception: Exception) {
            AppLogger.d(NotificationParser::class.java.canonicalName, "${exception.message}")
            null
        }
}