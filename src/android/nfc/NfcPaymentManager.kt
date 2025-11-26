package com.aub.mobilebanking.phone.eg.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.provider.Settings
import android.util.Log
import io.reactivex.rxjava3.core.Observable

class NfcPaymentManager(private val context: Context) {
    private lateinit var receiver: BroadcastReceiver
    /** ✅ Check if device supports NFC */
    fun nfcExists(): Boolean {
        return NfcAdapter.getDefaultAdapter(context) != null
    }

    /** ✅ Check if NFC is enabled */
    fun isNfcEnabled(): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter?.isEnabled ?: false
    }

    /** ✅ Observe NFC state ON/OFF (RxJava Observable) */
    fun observeNfcSettingsListener(): Observable<NfcState> {
        return Observable.create { emitter ->
            val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val state =
                        intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)
                    if (state == NfcAdapter.STATE_OFF) {
                        emitter.onNext(NfcState.OFF)
                    } else if (state == NfcAdapter.STATE_ON) {
                        emitter.onNext(NfcState.ON)
                    }
                }
            }
            context.registerReceiver(receiver, filter)
        }.doOnDispose {
            unregisterNfcSettingsListener()
        }.doOnTerminate {
            unregisterNfcSettingsListener()
        }
    }

    private fun unregisterNfcSettingsListener() {
        try {
            receiver?.let { context.unregisterReceiver(it) }
        } catch (ex: Exception) {
            Log.e(TAG, "Error while unregistering NFC receiver.", ex)
        }
    }

    /** ✅ Open NFC Settings */
    fun openNfcSettings() {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    companion object {
        private val TAG = NfcPaymentManager::class.java.simpleName
        const val REQUEST_CODE_DEFAULT_PAYMENT_APP = 2001
    }
}

enum class NfcState {
    OFF,
    ON
}