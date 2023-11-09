package com.robj.deblocktest.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGasFeeResponse(
    @SerialName("result")
    val gasFee: GasFee
)

@Serializable
data class GasFee(
    @SerialName("SafeGasPrice")
    val safeGasPrice: Double,
    @SerialName("ProposeGasPrice")
    val proposeGasPrice: Double,
    @SerialName("FastGasPrice")
    val fastGasPrice: Double,
    )