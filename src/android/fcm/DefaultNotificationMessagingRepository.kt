package com.aub.mobilebanking.phone.eg.fcm

import android.content.Context
import android.content.SharedPreferences

class DefaultNotificationMessagingRepository(
    context: Context,
    private val preferences: SharedPreferences = context.getSharedPreferences(
        FCM_PREFERENCES_FILENAME, Context
            .MODE_PRIVATE
    )
) :
    NotificationMessagingRepository {

    override fun getIsMessagingRegistered(): Boolean =
        preferences.getBoolean(FCM_PREFERENCES_KEY, false)

    override fun setIsMessagingRegistered(isFcmRegistered: Boolean) {
        with(preferences.edit()) {
            putBoolean(FCM_PREFERENCES_KEY, isFcmRegistered)
            apply()
        }
    }

    companion object {
        private const val FCM_PREFERENCES_FILENAME = "FCM_PREFERENCES_FILENAME"
        private const val FCM_PREFERENCES_KEY = "FCM_REGISTRATION_STATUS"
    }
}