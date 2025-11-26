package com.aub.mobilebanking.phone.eg.fcm

import android.content.Context
import com.aub.mobilebanking.phone.eg.R
import com.aub.mobilebanking.phone.eg.repository.DefaultWaServicesRepository
import com.aub.mobilebanking.phone.eg.repository.WaServicesRepository
import io.reactivex.rxjava3.core.Completable

class DefaultRegistrationNotificationSender(
    private val context: Context,
    private val waServicesRepository: WaServicesRepository
    = DefaultWaServicesRepository()
) {
    fun sendRegistrationMessage(messageReceiver: (String) -> Unit): Completable {
        return Completable.fromSingle(waServicesRepository.allServices.map { services ->
            FcmRegistrationHelper.getInstance(services.pairingService, context.getString(R.string.fcm_account_number))
                .sendRegistrationMessage(services.fcmService, messageReceiver)
        })
    }
}
