package com.robj.deblocktest.networking

import com.robj.deblocktest.networking.models.GetGasFeeResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface EthereumApiService {

    @GET("v3/simple/price")
    suspend fun getPrice(
        @Query("ids") cryptoCurrency: String,
        @Query("vs_currencies") currencies: String
    ): Map<String, Map<String, Double>?>

    @GET
    suspend fun getGasFee(
        @Url url: String = "https://api.etherscan.io/api?module=gastracker&action=gasoracle"
    ): GetGasFeeResponse
}