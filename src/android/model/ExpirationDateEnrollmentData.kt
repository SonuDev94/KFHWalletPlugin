package com.aub.mobilebanking.phone.eg.model

import com.aub.mobilebanking.phone.eg.BuildConfig
import com.idemia.wa.api.wms.*
import java.util.*

data class ExpirationDateEnrollmentData(
    private val authCode: String,
    private val pan: String,
    private val expDateMonth: String,
    private val expDateYear: String,
    private val tokenPurpose: WaTokenPurpose,
    private val iin: String
) {
    private val waExpirationDate = WaCardExpDate(expDateMonth.toCharArray(), expDateYear.toCharArray())
    private val authData = WaAuthData(WaAuthType.CSC, authCode.toCharArray())
    private val cardData = WaCardDataWithExpDate(pan.toCharArray(), waExpirationDate)
    private val iinData = WaIin(iin.toCharArray())

    val waEnrollmentParams: WaEnrollmentParams
        get() = WaEnrollmentParams.Builder(cardData)
            .setIin(iinData)
            .setAuthData(authData)
            .setPurpose(tokenPurpose)
            .setConsumerLanguageCode(Locale.US)
                //TODO
            //.setTokenRequestorId(WaTokenRequestorId(BuildConfig.TOKEN_REQUESTOR_ID))
            .build()
}