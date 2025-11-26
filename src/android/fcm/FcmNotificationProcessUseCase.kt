package com.aub.mobilebanking.phone.eg.fcm

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.Failure
import com.idemia.wa.api.push.ProcessingListener
import com.idemia.wa.api.push.RemoteMessagingService
import com.idemia.wa.api.push.WaRemoteMessage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class FcmNotificationProcessUseCase(
    private val applicationContext: Context,
    private val remoteMessagingService: Single<RemoteMessagingService>
) {
    private val notificationMsgRepository: NotificationMessagingRepository by lazy {
        DefaultNotificationMessagingRepository(
            applicationContext
        )
    }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(applicationContext) }

    fun processMessage(remoteMessage: WaRemoteMessage, notificationType: NotificationType?): Completable {
        return remoteMessagingService.flatMapCompletable { remoteMessagingService ->
            return@flatMapCompletable Completable.create { emitter ->
                remoteMessagingService.processMessage(remoteMessage, object : ProcessingListener {
                    override fun onFailure(failure: Failure?) {
                        AppLogger.d(TAG, "onFailure() failure: ${failure?.exception}")
                    }

                    override fun onComplete() {
                        notificationType?.let { reactOnNotificationComplete(it) }
                        AppLogger.d(TAG, "onComplete(): notification type $notificationType")
                    }
                })
            }
        }
    }

    private fun onFcmRegistrationComplete() {
        notificationMsgRepository.setIsMessagingRegistered(true)
        val intent = Intent(NotificationType.RegistrationSuccess.name)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun reactOnNotificationComplete(notificationType: NotificationType) {
        when (notificationType) {
            NotificationType.RegistrationSuccess -> onFcmRegistrationComplete()
            NotificationType.SyncNotification -> onSyncCompleted()
        }
    }

    private fun onSyncCompleted() {
        val intent = Intent(NotificationType.SyncNotification.name)
        localBroadcastManager.sendBroadcast(intent)
    }

    companion object {
        private val TAG = FcmNotificationProcessUseCase::class.java.simpleName
    }
}