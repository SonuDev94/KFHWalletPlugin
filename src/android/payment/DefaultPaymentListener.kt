package com.aub.mobilebanking.phone.eg.payment

import android.content.Context
import com.aub.mobilebanking.phone.eg.authentication.AuthenticationRepository
import com.aub.mobilebanking.phone.eg.authentication.PrefsAuthenticationRepository
import com.aub.mobilebanking.phone.eg.payment.policies.PaymentAuthenticationPolicy
import com.aub.mobilebanking.phone.eg.payment.policies.PaymentAuthenticationPolicyFactory
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.WalletAgentException
import com.idemia.wa.api.nfc.NfcTransactionListener
import com.idemia.wa.api.nfc.WaAbortReason
import com.idemia.wa.api.nfc.WaNfcTransactionDetails
import com.idemia.wa.api.payment.*
import com.aub.mobilebanking.phone.eg.screenLock.AndroidScreenLockVerifier
import com.aub.mobilebanking.phone.eg.screenLock.AndroidScreenManager
import com.aub.mobilebanking.phone.eg.screenLock.ScreenLockVerifier
import com.aub.mobilebanking.phone.eg.screenLock.ScreenManager
import com.aub.mobilebanking.phone.eg.card.ConfigurationRepository
import com.aub.mobilebanking.phone.eg.card.PrefsConfigurationRepository

//TODO big constructor
class DefaultPaymentListener(
    context: Context,
    private val configurationRepository: ConfigurationRepository = PrefsConfigurationRepository(),
    private val screenLockVerifier: ScreenLockVerifier = AndroidScreenLockVerifier(context),
    paymentAuthenticationPolicyFactory: PaymentAuthenticationPolicyFactory =
        PaymentAuthenticationPolicyFactory(screenLockVerifier, configurationRepository),
    private val screenManager: ScreenManager = AndroidScreenManager(context),
    private val authenticationRepository: AuthenticationRepository = PrefsAuthenticationRepository(context),
    private val isSecondTap: Boolean = false,
    private val isKeyguardAuthorized: Boolean = false,
    private val cdCvmVerifiedType: WaCdCvmVerifiedType = WaCdCvmVerifiedType.NO_CDCVM,
    private val paymentStatusListener: PaymentStatusListener = EmptyPaymentStatusListener,
) : NfcTransactionListener {
    private val paymentAuthenticationPolicy: PaymentAuthenticationPolicy by lazy {
        paymentAuthenticationPolicyFactory.createPaymentAuthenticationPolicy()
    }

    override fun getTimeOfLastAuthentication(): Long {
        return authenticationRepository.getLastAuthenticationTimestamp().value.also {
            AppLogger.d(TAG, "getTimeOfLastAuthentication() $it")
        }
    }

    override fun getFinalAssessment(
        advice: WaAdvice,
        transactionInformation: WaTransactionInformation,
        terminalInformation: WaTerminalInformation
    ): WaAssessment {
        return paymentAuthenticationPolicy.getFinalAssessment(advice).also {
            AppLogger.d(TAG, "getFinalAssessment() waAssessment: $it")
        }
    }

    override fun isCdCvmBlocked(): Boolean {
        return screenLockVerifier.isScreenLockBlocked().also {
            AppLogger.d(TAG, "isCdCvmBlocked() ? $it")
        }
    }

    override fun isCdCvmEnabled(): Boolean {
        return screenLockVerifier.isScreenLockSecure().also {
            AppLogger.d(TAG, "isCdCvmEnabled() ? $it")
        }
    }

    override fun isConsentGiven(): Boolean {
        return screenManager.isScreenOn().also {
            AppLogger.d(TAG, "isConsentGiven() ? $it")
        }
    }

    override fun isAuthenticated(): Boolean {
        return paymentAuthenticationPolicy.isAuthenticated(isSecondTap, isKeyguardAuthorized).also {
            AppLogger.d(TAG, "isAuthenticated() ? $it")
        }
    }

    override fun getCdCvmVerifiedType(): WaCdCvmVerifiedType {
        return cdCvmVerifiedType
    }

    override fun onContactlessTransactionCompleted(transactionDetails: WaNfcTransactionDetails?) {
        AppLogger.d(TAG, "onContactlessTransactionCompleted() outcome: ${transactionDetails?.transactionOutcome()}")
        paymentStatusListener.onTransactionCompleted(transactionDetails)
    }

    override fun onContactlessTransactionIncident(exception: WalletAgentException?) {
        AppLogger.d(TAG, "onContactlessTransactionIncident() $exception, message: ${exception?.message}")
        paymentStatusListener.onTransactionIncident(exception)
    }

    override fun onContactlessTransactionAbort(abortReason: WaAbortReason?, exception: WalletAgentException?) {
        AppLogger.d(
            TAG,
            "onContactlessTransactionAbort() abortReason: $abortReason, $exception, message: ${exception?.message}"
        )
        paymentStatusListener.onTransactionAbort(abortReason, exception)
    }

    companion object {
        private val TAG = DefaultPaymentListener::class.java.simpleName
    }

    private object EmptyPaymentStatusListener : PaymentStatusListener {
        override fun onTransactionCompleted(transactionDetails: WaNfcTransactionDetails?) {
            // no-op
        }

        override fun onTransactionIncident(exception: Exception?) {
            // no-op
        }

        override fun onTransactionAbort(abortReason: WaAbortReason?, exception: Exception?) {
            // no-op
        }
    }
}