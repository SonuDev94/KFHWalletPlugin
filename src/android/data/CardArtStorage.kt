package com.aub.mobilebanking.phone.eg.data

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentHashMap

object CardArtStorage {

    private val cacheMap: ConcurrentHashMap<String, Bitmap> =
        ConcurrentHashMap()

    fun get(key: String): Bitmap? = cacheMap[key]

    fun put(key: String, value: Bitmap) {
        cacheMap[key] = value
    }
}