package com.aub.mobilebanking.phone.eg.model

import android.graphics.Bitmap
import com.idemia.wa.api.WaTokenStatus

class CardConfigurationModel (
    val status: WaTokenStatus?,
    val cardPan: String,
    val cardExp: String,
    val tokenList: List<TokenConfigurationListModel>,
    val default: Boolean?,
    val bitmap: Bitmap?
)