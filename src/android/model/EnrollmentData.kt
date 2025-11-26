package com.aub.mobilebanking.phone.eg.model

import com.idemia.wa.api.wms.WaTokenPurpose

data class EnrollmentData(
    val authCode: String,
    val tokenPurpose: WaTokenPurpose,
    val actionOnStart: ActionOnStart,
    val opaque: String? = null,
    val iin: String? = null,
    val pan: String? = null,
    val expDateMonth: String? = null,
    val expDateYear: String? = null,
    val birthDateDay: String? = null,
    val birthDateMonth: String? = null,
    val birthDateYear: String? = null
) {

    fun mapToBirthDateParams(): BirthDateEnrollmentData =
        BirthDateEnrollmentData(
            authCode,
            requireNotNull(pan),
            requireNotNull(birthDateDay),
            requireNotNull(birthDateMonth),
            requireNotNull(birthDateYear),
            tokenPurpose,
            requireNotNull(iin)
        )

    fun mapToExpirationDateParams(): ExpirationDateEnrollmentData =
        ExpirationDateEnrollmentData(
            authCode,
            requireNotNull(pan),
            requireNotNull(expDateMonth),
            requireNotNull(expDateYear),
            tokenPurpose,
            requireNotNull(iin)
        )
}