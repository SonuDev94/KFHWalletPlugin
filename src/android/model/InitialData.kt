package com.aub.mobilebanking.phone.eg.model

import com.idemia.wa.api.WaCardId
import com.idemia.wa.api.WaTokenId

data class InitialData(val cardId: WaCardId, val cardPan: String, val cardExp: String, val tokenIdList: List<WaTokenId>)
