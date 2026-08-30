package com.cairnlabworks.mywealth.data.remote.stock

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Public Yahoo Finance chart endpoint. Only the ticker symbol is ever sent;
 * no personal or portfolio data leaves the device.
 */
interface StockApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("range") range: String = "1d",
        @Query("interval") interval: String = "1d",
    ): ChartResponse

    companion object {
        const val BASE_URL = "https://query1.finance.yahoo.com/"
    }
}
