package com.example.gmls.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.components.NavDrawerContent
import com.example.gmls.ui.screens.auth.LoginScreen
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.screens.auth.RegistrationScreen
import com.example.gmls.ui.screens.dashboard.DashboardScreen
import com.example.gmls.ui.screens.disaster.DisasterDetailScreen
import com.example.gmls.ui.screens.disaster.DisasterListScreen
import com.example.gmls.ui.screens.disaster.DisasterReport
import com.example.gmls.ui.screens.disaster.ReportDisasterScreen
import com.example.gmls.ui.screens.map.MapScreen
import com.example.gmls.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object DisasterList : Screen("disaster_list")
    object DisasterDetail : Screen("disaster_detail/{disasterId}")
    object DisasterReport : Screen("disaster_report")
    object Map : Screen("map")
    object Profile : Screen("profile")

    fun createRoute(vararg params: String): String {
        return buildString {
            append(route)
            params.forEach { param ->
                route.replace("{$param}", param)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DisasterResponseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    isAuthenticated: Boolean = false,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (RegistrationData) -> Unit,
    onDisasterReport: (DisasterReport) -> Unit,
    onLogout: () -> Unit
) {
    // Mock data for demonstration
    val disasters = remember {
        mutableListOf(
            Disaster(
                id = "1",
                title = "Flash Flood in Jakarta",
                description = "Heavy rainfall has caused flash flooding in Jakarta. Several streets are submerged and some residents have been evacuated.",
                location = "Jakarta, Indonesia",
                type = DisasterType.FLOOD,
                timestamp = System.currentTimeMillis(),
                affectedCount = 250,
                images = listOf(),
                status = Disaster.Status.VERIFIED,
                latitude = -6.2088,
                longitude = 106.8456
            ),
            Disaster(
                id = "2",
                title = "Earthquake in Bali",
                description = "A magnitude 5.6 earthquake struck Bali. Some buildings have reported minor damage.",
                location = "Bali, Indonesia",
                type = DisasterType.EARTHQUAKE,
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                affectedCount = 100,
                images = listOf(),
                status = Disaster.Status.IN_PROGRESS,
                latitude = -8.4095,
                longitude = 115.1889
            ),
            Disaster(
                id = "3",
                title = "Landslide in West Java",
                description = "Heavy rain has triggered a landslide in West Java. Roads are blocked and some villages are cut off.",
                location = "West Java, Indonesia",
                type = DisasterType.LANDSLIDE,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                affectedCount = 50,
                images = listOf(),
                status = Disaster.Status.RESOLVED,
                latitude = -6.9175,
                longitude = 107.6191
            )
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = currentRoute(navController)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawerContent(
                currentRoute = currentRoute,
                onDestinationClicked = { route ->
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                    }
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        },
        gesturesEnabled = isAuthenticated && drawerState.isOpen
    ) {
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Screen.Dashboard.route else Screen.Login.route,
            modifier = modifier
        ) {
            // Auth screens
            composable(
                route = Screen.Login.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                LoginScreen(
                    onLogin = { email, password ->
                        onLogin(email, password)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onForgotPassword = {
                        // Handle forgot password
                    }
                )
            }

            composable(
                route = Screen.Register.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                RegistrationScreen(
                    onRegister = { registrationData ->
                        onRegister(registrationData)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Main screens
            composable(
                route = Screen.Dashboard.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                DashboardScreen(
                    disasters = disasters,
                    onDisasterClick = { disaster ->
                        navController.navigate("disaster_detail/${disaster.id}")
                    },
                    onReportDisaster = {
                        navController.navigate(Screen.DisasterReport.route)
                    },
                    onSearchClick = {
                        // Handle search
                    },
                    onFilterChange = { /* Handle filter change */ },
                    onNotificationsClick = {
                        // Handle notifications
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(
                route = Screen.DisasterList.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                DisasterListScreen(
                    disasters = disasters,
                    onDisasterClick = { disaster ->
                        navController.navigate("disaster_detail/${disaster.id}")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.DisasterDetail.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) { backStackEntry ->
                val disasterId = backStackEntry.arguments?.getString("disasterId")
                val disaster = disasters.find { it.id == disasterId }

                if (disaster != null) {
                    DisasterDetailScreen(
                        disaster = disaster,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onMapClick = {
                            navController.navigate(Screen.Map.route)
                        }
                    )
                }
            }

            composable(
                route = Screen.DisasterReport.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                ReportDisasterScreen(
                    onSubmit = { report ->
                        onDisasterReport(report)
                        // Add the new disaster to our mock list
                        disasters.add(
                            Disaster(
                                id = (disasters.size + 1).toString(),
                                title = report.title,
                                description = report.description,
                                location = report.location,
                                type = report.type,
                                timestamp = System.currentTimeMillis(),
                                affectedCount = report.affectedCount,
                                images = emptyList(), // In a real app, would upload these images
                                status = Disaster.Status.REPORTED,
                                latitude = 0.0, // Would get real coordinates in a real app
                                longitude = 0.0
                            )
                        )
                        navController.popBackStack()
                    },
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Map.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                MapScreen(
                    disasters = disasters,
                    onDisasterClick = { disaster ->
                        navController.navigate("disaster_detail/${disaster.id}")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onFilterChange = {
                        // Handle filter change
                    }
                )
            }

            composable(
                route = Screen.Profile.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                ProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogout = {
                        onLogout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun NavDrawerContent(
    currentRoute: String?,
    onDestinationClicked: (route: String) -> Unit,
    onLogout: () -> Unit
) {
    // Implementation of the drawer content would go here
}