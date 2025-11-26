package com.aub.mobilebanking.phone.eg.authentication

@Suppress("DataClassPrivateConstructor")
data class LastAuthenticationTimestamp private constructor(val value: Long) {

    companion object {
        val EMPTY: LastAuthenticationTimestamp by lazy {
            of(0)
        }

        fun of(value: Long): LastAuthenticationTimestamp {
            return LastAuthenticationTimestamp(value)
        }
    }
}