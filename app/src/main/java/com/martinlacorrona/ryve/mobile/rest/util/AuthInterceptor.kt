package com.martinlacorrona.ryve.mobile.rest.util

import android.content.SharedPreferences
import com.martinlacorrona.ryve.mobile.app.Properties
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor used to intercept the actual request and
 * to supply your API Key in REST API calls via a custom header.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sharedPreference: SharedPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val username = sharedPreference.getString(Properties.PREFERENCE_USER, null)
        val password = sharedPreference.getString(Properties.PREFERENCE_PASSWORD, null)

        if (username == null || password == null) return chain.proceed(chain.request()) //NO SAVED

        val authorization =
            chain.request().header("Authorization") ?: Credentials.basic(username, password)

        return chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", authorization)
                .build()
        )
    }
}