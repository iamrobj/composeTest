package com.robj.deblocktest

import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencySwitcherViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val apiClient: ApiClient
) : ViewModel() {

    private val _stateFlow =
        MutableStateFlow<CurrencySwitcherState>(CurrencySwitcherState.IsLoading)
    val stateFlow = _stateFlow.asStateFlow()

    private val cryptoCurrency = savedStateHandle.get<String>(extraCurrency) ?: "ethereum"

    fun fetchPrices(initialValue: Double) {
        viewModelScope.launch {
            apiClient.getPrices(cryptoCurrency = cryptoCurrency,
                currencies = SupportedCurrency.values().map { it.currencyCode })
                .flowOn(Dispatchers.IO).catch { e ->
                    e.printStackTrace()
                    _stateFlow.emit(CurrencySwitcherState.Error("Some error occurred"))
                }.collect { result ->
                    val prices = result.mapNotNull { entry ->
                        SupportedCurrency.values()
                            .find { it.currencyCode.equals(entry.key, ignoreCase = true) }
                            ?.let { currency ->
                                //TODO: Move to  mapper function for easier testing
                                Price(
                                    currency = currency,
                                    conversion = entry.value,
                                    conversionFormatted = ((initialValue.takeIf { it > 0.0 }
                                        ?: entry.value) / entry.value).formatToDecimalPlaces(
                                        decimalPlaces = 2
                                    ),
                                    currencyFormatted = "${currency.currencySymbol}${
                                        (initialValue.takeIf { it > 0.0 }
                                            ?: entry.value).formatToDecimalPlaces(
                                            decimalPlaces = 0
                                        )
                                    }"
                                )
                            }
                    }
                    _stateFlow.emit(CurrencySwitcherState.Success(prices))
                }
        }
    }

}

sealed class CurrencySwitcherState {
    object IsLoading : CurrencySwitcherState()
    data class Error(val errorMsg: String) : CurrencySwitcherState()
    data class Success(val priceList: List<Price>) : CurrencySwitcherState()
}

data class Price(
    val currency: SupportedCurrency,
    val conversion: Double,
    val conversionFormatted: String,
    val currencyFormatted: String
)

enum class SupportedCurrency(
    val currencyCode: String,
    val displayName: String,
    val currencySymbol: String,
    @DrawableRes val flagIcon: Int
) {
    GBP(
        currencyCode = "GBP",
        displayName = "Pounds",
        currencySymbol = "£",
        flagIcon = R.drawable.ic_uk
    ),
    EUR(
        currencyCode = "EUR",
        displayName = "Euros",
        currencySymbol = "€",
        flagIcon = R.drawable.ic_europe
    ),
    USD(
        currencyCode = "USD",
        displayName = "Dollars",
        currencySymbol = "$",
        flagIcon = R.drawable.ic_usa
    )
}