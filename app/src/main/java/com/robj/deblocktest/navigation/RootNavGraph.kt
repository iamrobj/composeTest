package com.robj.deblocktest.navigation

import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.robj.deblocktest.CurrencySwitcherScreen
import com.robj.deblocktest.SendEthScreen

@Composable
fun RootNavGraph(
    modifier: Modifier = Modifier,
    mainNavController: NavHostController
) {
    NavHost(
        modifier = modifier
            .imePadding(),
        navController = mainNavController,
        startDestination = Screens.SendEthereum.name
    ) {
        composable(route = Screens.SendEthereum.name) {
            SendEthScreen()
        }
    }
}