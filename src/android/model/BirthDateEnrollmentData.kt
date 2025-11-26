package com.aub.mobilebanking.phone.eg.model

import com.aub.mobilebanking.phone.eg.BuildConfig
import com.idemia.wa.api.wms.*
import java.util.*

data class BirthDateEnrollmentData(
    private val authCode: String,
    private val pan: String,
    private val birthDateDay: String,
    private val birthDateMonth: String,
    private val birthDateYear: String,
    private val tokenPurpose: WaTokenPurpose,
    private val iin : String
) {
    private val authData = WaAuthData(WaAuthType.CSC, authCode.toCharArray())
    private val waBirthDate =
        WaCardBirthDate(birthDateDay.toCharArray(), birthDateMonth.toCharArray(), birthDateYear.toCharArray())
    private val cardData = WaCardDataWithBirthDate(pan.toCharArray(), waBirthDate)
    private val iinData = WaIin(iin.toCharArray())

    val waEnrollmentParams: WaEnrollmentParams
        get() = WaEnrollmentParams.Builder(cardData)
            .setIin(iinData)
            .setAuthData(authData)
            .setPurpose(tokenPurpose)
            .setConsumerLanguageCode(Locale.US)
            // why optional?
            //.setTokenRequestorId(WaTokenRequestorId(BuildConfig.TOKEN_REQUESTOR_ID))
            .build()
}