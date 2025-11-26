package com.aub.mobilebanking.phone.eg.fcm

interface NotificationMessagingRepository {
    fun getIsMessagingRegistered(): Boolean

    fun setIsMessagingRegistered(isFcmRegistered: Boolean)
}