package com.robj.deblocktest.ui.screens.currencySwitcherScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.robj.deblocktest.R

@Composable
fun CurrencySwitcherScreen(
    onCurrencySelection: (currency: SupportedCurrency, price: Double) -> Unit,
    initialValue: Double,
    currentSelection: SupportedCurrency,
    viewModel: CurrencySwitcherViewModel = hiltViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchPrices(initialValue)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val state by viewModel.stateFlow.collectAsState()
    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 400.dp)
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = state) {
            is CurrencySwitcherState.Error -> Text(text = state.errorMsg)
            CurrencySwitcherState.IsLoading -> CircularProgressIndicator()
            is CurrencySwitcherState.Success -> CurrencySwitcherContent(
                priceList = state.priceList,
                initialValue = initialValue,
                onCurrencySelection = onCurrencySelection,
                currentSelection = currentSelection
            )
        }
    }

}

@Composable
fun CurrencySwitcherContent(
    priceList: List<Price>,
    initialValue: Double,
    currentSelection: SupportedCurrency,
    onCurrencySelection: (currency: SupportedCurrency, price: Double) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth()
    ) {
        Text(
            text = stringResource(id = R.string.displayed_currency),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(priceList) { price ->
                CurrencyRow(
                    price = price,
                    initialValue = initialValue,
                    onClick = onCurrencySelection,
                    isSelected = currentSelection == price.currency
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(imageVector = Icons.Filled.Info, contentDescription = "Information")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(id = R.string.displayed_currency_info),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    price: Price,
    isSelected: Boolean,
    initialValue: Double,
    onClick: (currency: SupportedCurrency, price: Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(price.currency, price.conversion)
            },
        shape = RoundedCornerShape(4.dp),
        border = if (!isSelected) {
            BorderStroke(1.dp, Color(0xFFE3E3E3))
        } else {
            BorderStroke(2.dp, Color.Black)
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                painter = painterResource(id = price.currency.flagIcon),
                contentDescription = "${price.currency.displayName} icon"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = price.currency.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = price.conversionFormatted, style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = price.currency.currencyCode,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = price.currencyFormatted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencySwitcherScreenPreview() {
//    CurrencySwitcherScreen(onCurrencySelection = { _, _ ->
//
//    })
}

val extraCurrency: String = "extra_currency"