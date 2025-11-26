package com.aub.mobilebanking.phone.eg.payment

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.util.Log
import com.aub.mobilebanking.phone.eg.nfc.NfcPaymentManager
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.nfc.WaApduService

class DefaultPaymentAppUseCase(private val context: Context) {
    fun isDefaultPaymentApp(): Boolean {
        return try {
            val cardEmulationManager =
                CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(context))
            val paymentServiceComponent = ComponentName(
                context,
                WaApduService::class.java.canonicalName
                    ?: throw IllegalStateException("WaApduService::class.java.canonicalName is null")
            )
            return cardEmulationManager.isDefaultServiceForCategory(
                paymentServiceComponent,
                CardEmulation.CATEGORY_PAYMENT
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error during checking whether the app is the default payment app.", e)
            false
        }
    }

    fun askToBeDefaultPaymentApp(activity: Activity,onError: (Throwable) -> Unit) {
        try {
            val serviceName = WaApduService::class.java.canonicalName ?: return
            val paymentServiceComponent = ComponentName(activity, serviceName)

            val intent = Intent(CardEmulation.ACTION_CHANGE_DEFAULT).apply {
                putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT)
                putExtra(CardEmulation.EXTRA_SERVICE_COMPONENT, paymentServiceComponent)
            }
            activity.startActivityForResult(intent,
                NfcPaymentManager.REQUEST_CODE_DEFAULT_PAYMENT_APP
            )
        } catch (err: Exception) {
            Log.e(TAG, "Error asking to be default payment app", err)
            onError(err)
        }
    }

    companion object {
        private val TAG = DefaultPaymentAppUseCase::class.java.simpleName
        private const val REQUEST_CODE_DEFAULT_PAYMENT_APP = 1002
    }
}