package com.fuellog.app.ui.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fuellog.app.ui.car.CarScreen
import com.fuellog.app.ui.fuel.FuelDetailScreen
import com.fuellog.app.ui.fuel.FuelFormScreen
import com.fuellog.app.ui.home.MainScreen
import com.fuellog.app.ui.service.ServiceDetailScreen
import com.fuellog.app.ui.service.ServiceFormScreen
import com.fuellog.app.ui.stats.StatsScreen
import com.fuellog.app.ui.theme.BackgroundBlack

object Routes {
    const val RECORD_ID = "recordId"

    const val MAIN = "main"
    const val STATS = "stats"
    const val CAR = "car"

    const val FUEL_DETAIL = "fuel_detail/{$RECORD_ID}"
    const val FUEL_FORM = "fuel_form?$RECORD_ID={$RECORD_ID}"
    const val SERVICE_DETAIL = "service_detail/{$RECORD_ID}"
    const val SERVICE_FORM = "service_form?$RECORD_ID={$RECORD_ID}"

    fun fuelDetail(recordId: Long): String = "fuel_detail/$recordId"
    fun fuelForm(recordId: Long?): String =
        if (recordId == null) "fuel_form" else "fuel_form?$RECORD_ID=$recordId"
    fun serviceDetail(recordId: Long): String = "service_detail/$recordId"
    fun serviceForm(recordId: Long?): String =
        if (recordId == null) "service_form" else "service_form?$RECORD_ID=$recordId"
}

@Composable
fun FuelLogApp() {
    Surface(color = BackgroundBlack) {
        AppNavHost()
    }
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenCar = { navController.navigate(Routes.CAR) },
                onAddFuel = { navController.navigate(Routes.fuelForm(null)) },
                onAddService = { navController.navigate(Routes.serviceForm(null)) },
                onFuelRecordClick = { id -> navController.navigate(Routes.fuelDetail(id)) },
                onServiceRecordClick = { id -> navController.navigate(Routes.serviceDetail(id)) }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CAR) {
            CarScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.FUEL_DETAIL,
            arguments = listOf(navArgument(Routes.RECORD_ID) { type = NavType.LongType })
        ) { entry ->
            val recordId = entry.arguments?.getLong(Routes.RECORD_ID) ?: -1L
            if (recordId != -1L) {
                FuelDetailScreen(
                    recordId = recordId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.fuelForm(id)) }
                )
            }
        }

        composable(
            route = Routes.FUEL_FORM,
            arguments = listOf(
                navArgument(Routes.RECORD_ID) { type = NavType.LongType; defaultValue = -1L }
            )
        ) { entry ->
            val recordId = entry.arguments?.getLong(Routes.RECORD_ID)?.takeIf { it > 0 }
            FuelFormScreen(
                recordId = recordId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SERVICE_DETAIL,
            arguments = listOf(navArgument(Routes.RECORD_ID) { type = NavType.LongType })
        ) { entry ->
            val recordId = entry.arguments?.getLong(Routes.RECORD_ID) ?: -1L
            if (recordId != -1L) {
                ServiceDetailScreen(
                    recordId = recordId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.serviceForm(id)) }
                )
            }
        }

        composable(
            route = Routes.SERVICE_FORM,
            arguments = listOf(
                navArgument(Routes.RECORD_ID) { type = NavType.LongType; defaultValue = -1L }
            )
        ) { entry ->
            val recordId = entry.arguments?.getLong(Routes.RECORD_ID)?.takeIf { it > 0 }
            ServiceFormScreen(
                recordId = recordId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}