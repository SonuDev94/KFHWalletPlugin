package com.aub.mobilebanking.phone.eg.model

import com.idemia.wa.api.WaCard
import com.idemia.wa.api.WaToken
import com.idemia.wa.api.WaTokenId

data class HomeViewCardToken(val card: WaCard, var status: String?, val adapterPosition: Int, val tokenList: List<WaToken> )