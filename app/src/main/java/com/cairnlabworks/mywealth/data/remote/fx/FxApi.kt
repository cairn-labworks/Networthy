package com.cairnlabworks.mywealth.data.remote.fx

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Public, key-less exchange-rate endpoint. Returns units of each currency per
 * 1 unit of the requested base. Only the base currency code is transmitted.
 */
interface FxApi {

    @GET("v6/latest/{base}")
    suspend fun getLatest(@Path("base") base: String = "USD"): FxResponse

    companion object {
        const val BASE_URL = "https://open.er-api.com/"
    }
}
