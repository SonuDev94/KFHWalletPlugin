package com.aub.mobilebanking.phone.eg.authentication

interface AuthenticationRepository {

    fun getLastAuthenticationTimestamp(): LastAuthenticationTimestamp

    fun setLastAuthenticationTimestamp(timestamp: LastAuthenticationTimestamp)

    fun clearLastAuthenticationTimestamp()
}