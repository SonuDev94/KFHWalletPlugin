package com.aub.mobilebanking.phone.eg.model

import com.idemia.wa.api.WaTokenStatus

class TokenConfigurationListModel (
    val tokenId: String?,
    val status: WaTokenStatus?,
    val tokenPan: String?,
    val tokenExp: String?,
    val default: Boolean?,
)