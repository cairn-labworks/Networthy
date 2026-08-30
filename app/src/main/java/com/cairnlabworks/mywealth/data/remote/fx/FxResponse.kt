package com.cairnlabworks.mywealth.data.remote.fx

import com.google.gson.annotations.SerializedName

/** Response from open.er-api.com/v6/latest/{base}. */
data class FxResponse(
    @SerializedName("result") val result: String?,
    @SerializedName("base_code") val baseCode: String?,
    @SerializedName("rates") val rates: Map<String, Double>?,
    @SerializedName("time_last_update_unix") val timeLastUpdateUnix: Long?,
)
