package com.aub.mobilebanking.phone.eg.error

import android.app.Application
import com.aub.mobilebanking.phone.eg.R
import com.idemia.wa.api.ErrorCode

class ResourcesBasedErrorResolver(private val context: Application) : ErrorResolver {
    override fun resolveError(errorCode: ErrorCode?): Error {
        val error: Error
        when (errorCode) {
            ErrorCode.DEVICE_ALREADY_PAIRED ->
                error = Error(
                    title = context.getString(R.string.error_title_device_paired),
                    text = context.getString(R.string.error_text_device_paired),
                    positiveActionText = context.getString(R.string.error_button_device_paired)
                )
            ErrorCode.CANNOT_PAIR_DEVICE ->
                error = Error(
                    title = context.getString(R.string.error_title_cannot_pair),
                    text = context.getString(R.string.error_text_cannot_pair),
                    positiveActionText = context.getString(R.string.error_button_cannot_pair)
                )
            ErrorCode.CARDS_MAX_COUNT_REACHED ->
                error = Error(
                    title = context.getString(R.string.error_title_cards_max_count_reached),
                    text = context.getString(R.string.error_text_cards_max_count_reached),
                    positiveActionText = context.getString(
                        R.string.error_button_cards_max_count_reached
                    ),
                )
            ErrorCode.CONNECTIVITY_ERROR ->
                error = Error(
                    title = context.getString(R.string.error_title_connectivity),
                    text = context.getString(R.string.error_text_connectivity),
                    positiveActionText = context.getString(
                        R.string.error_button_positive_connectivity
                    ),
                    negativeActionText = context.getString(
                        R.string.error_button_negative_connectivity
                    )
                )
            ErrorCode.REQUEST_FAILED,
            ErrorCode.UNKNOWN_ERROR ->
                error = Error(
                    title = context.getString(R.string.error_title_unknown),
                    text = context.getString(R.string.error_text_unknown),
                    positiveActionText = context.getString(R.string.error_button_unknown)
                )
            else ->
                error = Error(
                    title = context.getString(R.string.error_title_unknown),
                    text = context.getString(R.string.error_text_unknown),
                    positiveActionText = context.getString(R.string.error_button_unknown)
                )
        }
        return error
    }
}
