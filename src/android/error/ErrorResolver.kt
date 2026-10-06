package com.aub.mobilebanking.phone.eg.error

import com.aub.mobilebanking.phone.eg.error.Error
import com.idemia.wa.api.ErrorCode

interface ErrorResolver {
    fun resolveError(errorCode: ErrorCode?): Error
}