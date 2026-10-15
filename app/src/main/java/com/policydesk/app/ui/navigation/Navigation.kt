package com.policydesk.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.policydesk.app.ui.addpolicy.AddPolicyScreen
import com.policydesk.app.ui.dashboard.DashboardScreen
import com.policydesk.app.ui.detail.VehicleDetailScreen
import com.policydesk.app.ui.importer.ImportScreen
import com.policydesk.app.ui.monthview.MonthViewScreen
import com.policydesk.app.ui.pdfreview.PdfReviewScreen
import com.policydesk.app.ui.renew.RenewPolicyScreen
import com.policydesk.app.ui.reports.ReportsScreen
import com.policydesk.app.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object MonthView : Screen("month_view")
    object Reports : Screen("reports")
    object AddPolicy : Screen("add_policy")
    object ImportSpreadsheet : Screen("import_spreadsheet")
    object PdfReview : Screen("pdf_review")
    object Settings : Screen("settings")
    object Customers : Screen("customers")
    object VehicleDetail : Screen("vehicle_detail/{regNo}") {
        fun createRoute(regNo: String) = "vehicle_detail/$regNo"
    }
    object RenewPolicy : Screen("renew_policy/{regNo}") {
        fun createRoute(regNo: String) = "renew_policy/$regNo"
    }
}

@Composable
fun PolicyDeskNavHost(
    initialPdfUri: android.net.Uri? = null,
    initialRegNo: String? = null
) {
    val navController = rememberNavController()
    val startDestination = when {
        initialPdfUri != null -> Screen.PdfReview.route
        !initialRegNo.isNullOrBlank() -> Screen.VehicleDetail.createRoute(initialRegNo)
        else -> Screen.Dashboard.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddPolicy = {
                    navController.navigate(Screen.AddPolicy.route)
                },
                onNavigateToMonthView = {
                    navController.navigate(Screen.MonthView.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToCustomers = {
                    navController.navigate(Screen.Customers.route)
                },
                onNavigateToImport = {
                    navController.navigate(Screen.ImportSpreadsheet.route)
                },
                onNavigateToPdf = {
                    navController.navigate(Screen.PdfReview.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToVehicleDetail = { regNo ->
                    navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                }
            )
        }

        composable(Screen.Customers.route) {
            com.policydesk.app.ui.customers.CustomersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicleDetail = { regNo ->
                    navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                }
            )
        }

        composable(Screen.MonthView.route) {
            MonthViewScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicleDetail = { regNo ->
                    navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicleDetail = { regNo ->
                    navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                },
                onNavigateToRenew = { regNo ->
                    navController.navigate(Screen.RenewPolicy.createRoute(regNo))
                }
            )
        }

        composable(Screen.AddPolicy.route) {
            AddPolicyScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicleDetail = { regNo ->
                    navController.navigate(Screen.VehicleDetail.createRoute(regNo)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }

        composable(Screen.ImportSpreadsheet.route) {
            ImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onImportFinished = {
                    navController.popBackStack(Screen.Dashboard.route, false)
                }
            )
        }

        composable(Screen.PdfReview.route) {
            PdfReviewScreen(
                initialUri = initialPdfUri,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicleDetail = { regNo ->
                    navController.navigate(Screen.VehicleDetail.createRoute(regNo)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VehicleDetail.route,
            arguments = listOf(navArgument("regNo") { type = NavType.StringType })
        ) {
            VehicleDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRenew = { regNo ->
                    navController.navigate(Screen.RenewPolicy.createRoute(regNo))
                }
            )
        }

        composable(
            route = Screen.RenewPolicy.route,
            arguments = listOf(navArgument("regNo") { type = NavType.StringType })
        ) {
            RenewPolicyScreen(
                onNavigateBack = { navController.popBackStack() },
                onRenewalComplete = { regNo ->
                    navController.popBackStack()
                }
            )
        }
    }
}
