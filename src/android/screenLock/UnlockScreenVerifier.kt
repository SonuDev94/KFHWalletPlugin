package com.aub.mobilebanking.phone.eg.screenLock

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aub.mobilebanking.phone.eg.R
import com.idemia.wa.api.payment.WaCdCvmVerifiedType

object UnlockScreenVerifier {
    private const val TAG = "UnlockScreenVerifier"

    fun showSystemUnlockScreen(
        activity: FragmentActivity,
        onSuccess: (verifiedType: WaCdCvmVerifiedType) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        // Check biometric availability first
        val biometricManager = BiometricManager.from(activity)
        val availabilityResult = checkBiometricAvailability(activity)

        Log.d(TAG, "Biometric availability: $availabilityResult")

        if (!availabilityResult.first) {
            Log.w(TAG, "Biometric authentication not available: ${availabilityResult.second}")
            onError(-1, availabilityResult.second)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, getBiometricCallback(onSuccess, onError, onFailed))
        val promptInfo = getPromptInfo(activity)

        Log.d(TAG, "Starting biometric authentication")
        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkBiometricAvailability(context: Context): Pair<Boolean, String> {
        val biometricManager = BiometricManager.from(context)

        // Check for strong biometric + device credential
        val strongResult = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        // Check for weak biometric + device credential
        val weakResult = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        // Check for device credential only
        val credentialResult =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)

        Log.d(TAG, "Strong biometric result: $strongResult")
        Log.d(TAG, "Weak biometric result: $weakResult")
        Log.d(TAG, "Device credential result: $credentialResult")

        return when {
            strongResult == BiometricManager.BIOMETRIC_SUCCESS ->
                Pair(true, "Strong biometric authentication available")

            weakResult == BiometricManager.BIOMETRIC_SUCCESS ->
                Pair(true, "Weak biometric authentication available")

            credentialResult == BiometricManager.BIOMETRIC_SUCCESS ->
                Pair(true, "Device credential authentication available")

            else -> {
                val errorMessage = when (strongResult) {
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                        "No biometric hardware available"

                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                        "Biometric hardware temporarily unavailable"

                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                        "No fingerprints or biometric credentials enrolled. Please set up fingerprint/face unlock or screen lock (PIN/Pattern/Password) in device settings."

                    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                        "Security update required for biometric authentication"

                    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                        "Biometric authentication not supported"

                    BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                        "Biometric status unknown"

                    else -> "Biometric authentication unavailable (code: $strongResult)"
                }
                Pair(false, errorMessage)
//                Pair(false,"enrolled")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getPromptInfo(context: Context): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.transaction_system_unlock_title))
            .setSubtitle(context.getString(R.string.transaction_system_unlock_info))

        getAuthenticators(context)?.let {
            builder.setAllowedAuthenticators(it)
        } ?: run {
            // Fallback to device credential if biometric not available
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }
        return builder.build()
    }

    private fun getAuthenticators(context: Context): Int? {
        val biometricManager = BiometricManager.from(context)

        val strongCredentialsAuthenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val weakCredentialsAuthenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return when {
            biometricManager.canAuthenticate(strongCredentialsAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Using strong biometric + device credential")
                strongCredentialsAuthenticators
            }
            biometricManager.canAuthenticate(weakCredentialsAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Using weak biometric + device credential")
                weakCredentialsAuthenticators
            }

            biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Using device credential only")
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            }

            else -> {
                Log.w(TAG, "No suitable authenticators found")
                null
            }
        }
    }

    private fun getBiometricCallback(
        onSuccess: (verifiedType: WaCdCvmVerifiedType) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "Authentication error: $errorCode, $errString")
                onError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                val cdCvmType = when (result.authenticationType) {
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> {
                        Log.d(TAG, "Authentication succeeded with biometric")
                        WaCdCvmVerifiedType.BIOMETRIC
                    }

                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> {
                        Log.d(TAG, "Authentication succeeded with device credential")
                        WaCdCvmVerifiedType.CREDENTIAL
                    }

                    else -> {
                        Log.d(TAG, "Authentication succeeded with unknown type")
                        WaCdCvmVerifiedType.NO_CDCVM
                    }
                }
                onSuccess(cdCvmType)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed")
                onFailed()
            }
        }
    }
}