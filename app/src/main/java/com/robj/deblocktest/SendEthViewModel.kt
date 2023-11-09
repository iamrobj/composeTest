package com.robj.deblocktest

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SendEthViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val apiClient: ApiClient
) : ViewModel() {

    private var gasFeeJob: Job? = null
    private val _stateFlow =
        MutableStateFlow<SendEthState>(SendEthState.IsLoading)
    val stateFlow = _stateFlow.asStateFlow()

    private var _currency: SupportedCurrency = SupportedCurrency.GBP
        set(value) {
            field = value
            currency.value = value
        }
    var currency: MutableState<SupportedCurrency> = mutableStateOf(_currency)
    private var price: Double = 0.0
    private val cryptoCurrency: String = "ethereum"
    var gasFee: MutableState<Double?> = mutableStateOf(null)

    init {
        viewModelScope.launch {
            apiClient.getPrices(
                cryptoCurrency = cryptoCurrency,
                currencies = listOf(_currency.currencyCode)
            ).flowOn(Dispatchers.IO)
                .map { result ->
                    result[_currency.currencyCode.lowercase()]
                        ?: throw RuntimeException("Price not found!")
                }
                .catch { e ->
                    e.printStackTrace()
                    _stateFlow.emit(SendEthState.Error("Some error occurred"))
                }.collect { price ->
                    this@SendEthViewModel.price = price
                    _stateFlow.emit(buildUiModel(value = 0.0, isFiat = true))
                }
        }
    }

    fun changeCurrency(currency: SupportedCurrency, price: Double, value: Double, isFiat: Boolean) {
        this.price = price
        this._currency = currency
        onValueChange(value = value, isFiat = isFiat)
    }

    private fun buildUiModel(value: Double, isFiat: Boolean): SendEthState.UiModel {
        return SendEthState.UiModel(
            currency = _currency,
            fiatValue = if (isFiat) {
                value
            } else {
                calcFiatForEth(value)
            }, //TOOD: Format nicer
            ethValue = if (isFiat) {
                calcEthForFiat(value)
            } else {
                value
            },
        )
    }

    fun onValueChange(value: Double, isFiat: Boolean) {
        viewModelScope.launch {
            _stateFlow.emit(buildUiModel(value = value, isFiat = isFiat))
        }
    }

    private fun estimateNetworkFee(gasPrice: Double) = (21000 * gasPrice) / 100000000

    private fun calcFiatForEth(eth: Double) = eth * price

    private fun calcEthForFiat(fiat: Double) = fiat / price

    fun fetchGasFees() {
        gasFeeJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                apiClient.getGasFee()
                    .catch { e ->
                        e.printStackTrace()
                    }.collect { result ->
                        gasFee.value = result.gasFee.fastGasPrice.let {
                            estimateNetworkFee(it)
                        }
                    }
                delay(TimeUnit.SECONDS.toMillis(30))
            }
        }
    }

    fun cancelFetchGasFees() {
        gasFeeJob?.cancel()
    }

}

sealed class SendEthState {
    object IsLoading : SendEthState()
    data class Error(val errorMsg: String) : SendEthState()
    data class UiModel(
        val currency: SupportedCurrency,
        val fiatValue: Double,
        val ethValue: Double,
    ) : SendEthState()
}