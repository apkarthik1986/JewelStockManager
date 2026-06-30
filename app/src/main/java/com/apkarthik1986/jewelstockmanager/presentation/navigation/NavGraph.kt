package com.apkarthik1986.jewelstockmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apkarthik1986.jewelstockmanager.presentation.home.HomeScreen
import com.apkarthik1986.jewelstockmanager.presentation.inventory.InventoryScreen

object NavRoutes {
    const val HOME = "home"
    const val INVENTORY = "inventory?category={category}&boxNumber={boxNumber}"

    fun inventoryRoute(category: String = "", boxNumber: String = "") =
        "inventory?category=$category&boxNumber=$boxNumber"
}

@Composable
fun JewelNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = NavRoutes.HOME) {

        composable(NavRoutes.HOME) {
            HomeScreen(
                onBoxSelected = { category, boxNumber ->
                    navController.navigate(NavRoutes.inventoryRoute(category, boxNumber))
                }
            )
        }

        composable(
            route = NavRoutes.INVENTORY,
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("boxNumber") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val boxNumber = backStackEntry.arguments?.getString("boxNumber") ?: ""
            InventoryScreen(
                onBack = { navController.navigateUp() },
                initialCategory = category,
                initialBoxNumber = boxNumber
            )
        }
    }
}
