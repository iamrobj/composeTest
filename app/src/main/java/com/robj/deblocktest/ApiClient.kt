package com.robj.deblocktest

import com.robj.deblocktest.networking.EthereumApiService
import com.robj.deblocktest.networking.models.GetGasFeeResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.RuntimeException

class ApiClient(   private val ethereumApiService: EthereumApiService,
) {

    suspend fun getPrices(
        cryptoCurrency: String, currencies: List<String>
    ): Flow<Map<String, Double>> = flow {
        emit(
            ethereumApiService.getPrice(cryptoCurrency, currencies.joinToString(separator = ","))[cryptoCurrency]
                ?: throw RuntimeException("Prices not found!")
        )
    }

    suspend fun getGasFee(): Flow<GetGasFeeResponse> = flow {
        emit(
            ethereumApiService.getGasFee()
        )
    }
}