package com.robj.deblocktest.ui.screens.sendEthScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.robj.deblocktest.ui.screens.currencySwitcherScreen.CurrencySwitcherScreen
import com.robj.deblocktest.R
import com.robj.deblocktest.ui.screens.currencySwitcherScreen.SupportedCurrency
import com.robj.deblocktest.utils.formatToDecimalPlaces
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendEthScreen(
    viewModel: SendEthViewModel = hiltViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.fetchGasFees()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.cancelFetchGasFees()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val state by viewModel.stateFlow.collectAsState()
    val currency by viewModel.currency
    val gasFee by viewModel.gasFee
    Scaffold() { _ ->
        Column() {
            Text(
                modifier = Modifier.padding(
                    start = 32.dp,
                    end = 32.dp,
                    top = 72.dp,
                    bottom = 32.dp
                ),
                text = stringResource(id = R.string.send_eth_title),
                style = MaterialTheme.typography.headlineLarge
            )
            when (val state = state) {
                is SendEthState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.errorMsg)
                }

                SendEthState.IsLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is SendEthState.UiModel -> SendEthContent(
                    state = state,
                    currency = currency,
                    gasFee = gasFee,
                    onCurrencyChange = viewModel::changeCurrency,
                    onValueChange = viewModel::onValueChange,
                    onCurrencyFlip = viewModel::flipCurrencies
                )
            }

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendEthContent(
    state: SendEthState.UiModel,
    currency: SupportedCurrency,
    onCurrencyChange: (currency: SupportedCurrency, price: Double, userValue: Double, isFiat: Boolean) -> Unit,
    onValueChange: (newValue: Double, isFiat: Boolean) -> Unit,
    onCurrencyFlip: (uiModel: SendEthState.UiModel, isFiat: Boolean) -> Unit,
    gasFee: Double?
) {
    var showCurrencySwitcher by rememberSaveable { mutableStateOf(false) }
    var userValue by rememberSaveable { mutableDoubleStateOf(0.0) }
    var isFiat by rememberSaveable { mutableStateOf(true) }
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier.padding(start = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .wrapContentHeight()
                .fillMaxWidth(),
            shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color(0xFFE3E3E3)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 16.dp, end = 8.dp, start = 8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    text = stringResource(id = R.string.max_eth_desc).capitalize(Locale.getDefault()),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(
                            0xFF0045F5
                        )
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            modifier = Modifier.padding(end = 2.dp),
                            text = state.userInputCurrencySymbol,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        BasicTextField(
                            value = state.userInputValue,
                            onValueChange = { newValue ->
                                userValue = newValue.toDoubleOrNull() ?: userValue
                                onValueChange(userValue, isFiat)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.titleLarge,
                        )
                    }
                    CurrencyDropDown(currency = currency) {
                        showCurrencySwitcher = true
                    }
                }
                Text(
                    modifier = Modifier.padding(start = 32.dp),
                    text = state.conversionValue,
                    style = MaterialTheme.typography.labelSmall
                )
            }

        }
        Image(
            modifier = Modifier
                .size(50.dp)
                .clickable {
                    isFiat = !isFiat
                    onCurrencyFlip(state, isFiat)
                }
                .background(color = MaterialTheme.colorScheme.background)
                .clip(CircleShape)
                .border(border = BorderStroke(1.dp, Color(0xFFE3E3E3)), shape = CircleShape)
                .padding(8.dp),
            painter = painterResource(id = R.drawable.ic_exchange),
            contentDescription = "Reverse icon",
        )

        if (showCurrencySwitcher) {
            showCurrencyPickerBottomSheet(
                modalBottomSheetState = modalBottomSheetState,
                onDismiss = {
                    showCurrencySwitcher = false
                },
                onCurrencyChange = { currency, price ->
                    onCurrencyChange(currency, price, userValue, isFiat)
                },
                userValue = userValue,
                currency = currency
            )
        }
    }
    Row(
        modifier = Modifier.padding(start = 42.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Outlined.Info,
            contentDescription = "Information"
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(
                id = R.string.est_network_fees,
                gasFee?.let { it.formatToDecimalPlaces() + " ETH" } ?: ""),
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Black)
        )
    }
    Spacer(modifier = Modifier.height(96.dp))
    if (state.exceededBalance) {
        Text(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            text = stringResource(
                id = R.string.error_balance_exceeded
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Red)
        )
    }
    Button(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
        enabled = state.isReadyToSend,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        ),
        onClick = { }) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(
                id = R.string.send_eth_btn,
                currency.currencySymbol + state.fiatValue.formatToDecimalPlaces(
                    decimalPlaces = 0
                )
            ),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CurrencyDropDown(currency: SupportedCurrency, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable {
        onClick()
    }, verticalAlignment = Alignment.CenterVertically) {
        Image(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
            painter = painterResource(id = currency.flagIcon),
            contentDescription = "${currency.displayName} icon"
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = currency.currencyCode,
            style = MaterialTheme.typography.bodyMedium
        )
        Image(imageVector = Icons.Default.KeyboardArrowDown, "Chevron down")
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun showCurrencyPickerBottomSheet(
    modalBottomSheetState: SheetState,
    onDismiss: () -> Unit,
    onCurrencyChange: (currency: SupportedCurrency, price: Double) -> Unit,
    userValue: Double,
    currency: SupportedCurrency
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = "", block = {
        coroutineScope.launch {
            modalBottomSheetState.expand()
        }
    })
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = modalBottomSheetState,
    ) {
        CurrencySwitcherScreen(
            onCurrencySelection = { currency, price ->
                onCurrencyChange(currency, price)
                onDismiss()
                coroutineScope.launch {
                    modalBottomSheetState.hide()
                }
            }, initialValue = userValue, currentSelection = currency
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SendEthScreenPreview() {
    SendEthScreen()
}