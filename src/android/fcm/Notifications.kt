package com.aub.mobilebanking.phone.eg.fcm

data class Notifications(val notifications: List<Notification>) {
    val payloadType: NotificationType? = notifications.firstOrNull()?.payload?.type
}

data class Notification(val payload: Payload)

data class Payload(val type: NotificationType)