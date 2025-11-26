package com.aub.mobilebanking.phone.eg.repository

import android.graphics.Bitmap
import com.idemia.wa.api.WaCard
import io.reactivex.rxjava3.core.Single

interface CardArtRepository {
    fun fetchCardArt(card: WaCard): Single<Bitmap>
    fun getCardFromStorage(waCardId: String): Bitmap?
}

