package com.robj.deblocktest.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class GetPriceResponse(
    val ethereum: Map<String, Double>
)