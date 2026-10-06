package com.aub.mobilebanking.phone.eg.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.aub.mobilebanking.phone.eg.data.CardArtStorage
import com.aub.mobilebanking.phone.eg.utils.AppLogger
import com.idemia.wa.api.Failure
import com.idemia.wa.api.WaCard
import com.idemia.wa.api.wms.GetResourceListener
import com.idemia.wa.api.wms.ResourcesService
import com.idemia.wa.api.wms.WaResource
import com.idemia.wa.api.wms.WaResourceType
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class DefaultCardArtRepository(private val resourcesService: Single<ResourcesService>) :
    CardArtRepository {

    override fun fetchCardArt(card: WaCard) : Single<Bitmap> {
        CardArtStorage.get(card.id.value)?.let {
            return Single.just(it)
        }
        return resourcesService.flatMap { resourcesService ->
            return@flatMap Single.create { emitter ->
                AppLogger.d("accessing repo","accessing repo with card $card")
                emitResource(resourcesService, card, EmitterWrapper(emitter, "dupa ${card.metadata.lastDigits}"))
            }
        }
    }

    private fun emitResource(
        resourcesService: ResourcesService,
        card: WaCard,
        emitter: SingleEmitter<Bitmap>
    ) {
        val resourceId = card.cardArts.first().resourceId
        AppLogger.d("accessing repo","accessing repo with card $card")
        resourcesService.getResource(resourceId, object : GetResourceListener {
            override fun onError(failure: Failure?) {
                emitter.onError(Exception(failure?.exception))
            }

            override fun onCompleted(resource: WaResource?) {
                if (resource?.type == WaResourceType.IMAGE_PNG) {
                    val result = resource.data
                    val bitmap =
                        BitmapFactory.decodeByteArray(result, 0, result.size)
                    if (bitmap != null) {
                        CardArtStorage.put(card.id.value, bitmap)
                        emitter.onSuccess(bitmap)
                    } else {
                        emitter.onError(Exception("Bitmap decoding error for resource $resource"))
                    }
                } else {
                    emitter.onError(Exception("Found resource type: ${resource?.type}"))
                }
            }
        })
    }

    override fun getCardFromStorage(waCardId: String): Bitmap? {
        val bitmap = CardArtStorage.get(waCardId)
        return bitmap
    }
}

class EmitterWrapper(private val emitter: SingleEmitter<Bitmap>,
private val name: String) :
    SingleEmitter<Bitmap> by emitter {
    override fun toString(): String {
        return "EmitterWrapper(name='$name')"
    }
}
