package com.app.covidpredict

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.covidpredict.ui.components.NavBar
import com.app.covidpredict.ui.components.TopBar
import com.app.covidpredict.ui.screen.DashboardScreen
import com.app.covidpredict.ui.screen.DataScreen
import com.app.covidpredict.ui.screen.GrafikScreen
import com.app.covidpredict.ui.screen.PrediksiScreen
import com.app.covidpredict.viewmodels.DashboardViewModel
import com.app.covidpredict.viewmodels.DataViewModel
import com.app.covidpredict.viewmodels.GrafikViewModel
import com.app.covidpredict.viewmodels.PrediksiViewModel
import com.app.covidpredict.viewmodels.SharedViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
    val sharedViewModel: SharedViewModel = viewModel()

    val navigateTo = remember(navController) {
        { route: String ->
            navController.navigate(route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo("dashboard") {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            NavBar(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateTo(route) }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            composable("dashboard") {
                val viewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel,
                    onNavigateToPrediction = { navigateTo("prediksi") },
                    onNavigateToData = { navigateTo("data") }
                )
            }
            composable("data") {
                val viewModel: DataViewModel = viewModel()
                DataScreen(
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel
                )
            }
            composable("prediksi") {
                val viewModel: PrediksiViewModel = viewModel()
                PrediksiScreen(viewModel = viewModel)
            }
            composable("grafik") {
                val viewModel: GrafikViewModel = viewModel()
                GrafikScreen(viewModel = viewModel)
            }
        }
    }
}