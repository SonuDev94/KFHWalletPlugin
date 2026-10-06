package com.aub.mobilebanking.phone.eg

import android.app.Application
import android.os.Build
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider

//TODO decouple stuff from here. This class basically only does on app creation initialization
class WaAppSystemContext : Application()/*, SystemContext*/ {

    private val isOreoOrHigher: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    override fun onCreate() {
        super.onCreate()
        application = this
        // ensureBksKeyStoreAvailable()
        initLogging()
        registerUserPresentBroadcastReceiverIfOreoOrHigher()
    }

    private fun registerUserPresentBroadcastReceiverIfOreoOrHigher() {
        if (isOreoOrHigher) {
//            UserPresentBroadcastReceiver().register(this)
        }
    }

    private fun initLogging() {
    }

    companion object {
        lateinit var application: Application

        private val TAG = WaAppSystemContext::class.java.simpleName

        // @JvmStatic
        // fun ensureBksKeyStoreAvailable() {
        //     val original = Security.getProvider("BC")
        //     try {
        //         Security.removeProvider("BC")
        //         Security.insertProviderAt(BouncyCastleProvider(), 1)
        //     } catch (t: Throwable) {
        //         android.util.Log.e(TAG, "Failed to register BouncyCastle provider, restoring original", t)
        //         // Restore Android's original provider so BKS/TLS isn't left completely broken
        //         original?.let { Security.insertProviderAt(it, 1) }
        //     }
        // }
    }
}