package com.example.ro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ro.screens.*

sealed class Screen(val route: String) {
    object R0 : Screen("r0")
    object Truck : Screen("truck")
    object ActivityReport : Screen("activity_report")
    object DailyReport : Screen("daily_report")
    object ViewReports : Screen("view_reports")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.DailyReport.route
    ) {
        composable(Screen.R0.route) {
            R0Screen()
        }
        composable(Screen.Truck.route) {
            TruckScreen()
        }
        composable(Screen.ActivityReport.route) {
            ActivityReportScreen()
        }
        composable(Screen.DailyReport.route) {
            DailyReportScreen()
        }
        composable(Screen.ViewReports.route) {
            ViewReportsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
} 