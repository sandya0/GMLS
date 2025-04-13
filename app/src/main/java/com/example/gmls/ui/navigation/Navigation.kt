package com.example.gmls.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.NavDrawerContent
import com.example.gmls.ui.screens.auth.LoginScreen
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.screens.auth.RegistrationScreen
import com.example.gmls.ui.screens.dashboard.DashboardScreen
import com.example.gmls.ui.screens.disaster.DisasterDetailScreen
import com.example.gmls.ui.screens.disaster.DisasterListScreen
import com.example.gmls.ui.screens.disaster.ReportDisasterScreen
import com.example.gmls.ui.screens.map.MapScreen
import com.example.gmls.ui.screens.profile.ProfileScreen
import com.example.gmls.ui.viewmodels.AuthState
import com.example.gmls.ui.viewmodels.AuthViewModel
import com.example.gmls.ui.viewmodels.DisasterViewModel
import com.example.gmls.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch
import com.example.gmls.ui.theme.AppTheme
import com.example.gmls.ui.screens.onboarding.OnboardingScreen
import com.example.gmls.data.local.OnboardingPreferenceManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import com.example.gmls.ui.screens.location.LocationPickerScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object DisasterList : Screen("disaster_list")
    object DisasterDetail : Screen("disaster_detail/{disasterId}") {
        fun createRoute(disasterId: String) = "disaster_detail/$disasterId"
    }
    object ReportDisaster : Screen("report_disaster")
    object Map : Screen("map")
    object Profile : Screen("profile")
    object Onboarding : Screen("onboarding")
    object LocationPicker : Screen("location_picker")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DisasterResponseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    disasterViewModel: DisasterViewModel = hiltViewModel(),
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val disasterState by disasterViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = currentRoute(navController)

    val context = LocalContext.current
    val onboardingPreferenceManager = remember { OnboardingPreferenceManager(context) }
    var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }

    // Observe onboarding completion from DataStore
    LaunchedEffect(Unit) {
        onboardingPreferenceManager.onboardingCompletedFlow.collect { completed ->
            onboardingCompleted = completed
        }
    }

    // Decide start destination
    val startDestination = when {
        onboardingCompleted == false -> Screen.Onboarding.route
        authState is AuthState.Authenticated -> Screen.Dashboard.route
        else -> Screen.Login.route
    }

    if (onboardingCompleted == null) {
        // Show splash or loading while onboarding state is loading
        Box(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
        return
    }

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
                    authViewModel.logout()
                }
            )
        },
        gesturesEnabled = isLoggedIn && drawerState.isOpen
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            // Onboarding screen
            composable(
                route = Screen.Onboarding.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                val scope = rememberCoroutineScope()
                OnboardingScreen(
                    onFinish = {
                        scope.launch {
                            onboardingPreferenceManager.setOnboardingCompleted(true)
                        }
                        if (authState is AuthState.Authenticated) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Auth screens
            composable(
                route = Screen.Login.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                var navigateToDashboard by remember { mutableStateOf(false) }
                if (authState is AuthState.Authenticated && !navigateToDashboard) {
                    LaunchedEffect(Unit) {
                        navigateToDashboard = true
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                LoginScreen(
                    onLogin = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onForgotPassword = {
                        // Handle forgot password
                    },
                    isLoading = authState is AuthState.Loading,
                    errorMessage = if (authState is AuthState.Error) (authState as AuthState.Error).message else null
                )
            }

            composable(
                route = Screen.Register.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                RegistrationScreen(
                    onRegister = { registrationData ->
                        authViewModel.register(registrationData)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    isLoading = authState is AuthState.Loading
                )

                // Show error dialog if registration fails
                if (authState is AuthState.Error) {
                    AlertDialog(
                        onDismissRequest = { authViewModel.resetAuthState() },
                        title = { Text("Registration Failed") },
                        text = { Text((authState as AuthState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { authViewModel.resetAuthState() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }

            // Main screens
            composable(
                route = Screen.Dashboard.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val profileState by profileViewModel.profileState.collectAsState()
                
                DashboardScreen(
                    disasters = disasterState.disasters,
                    onDisasterClick = { disaster ->
                        navController.navigate(Screen.DisasterDetail.createRoute(disaster.id))
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onNotificationsClick = {
                        navController.navigate("notifications")
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                    },
                    onReportDisaster = {
                        navController.navigate(Screen.ReportDisaster.route)
                    }
                )
            }

            // Add notifications screen route
            composable(
                route = "notifications",
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                com.example.gmls.ui.screens.notifications.NotificationsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { /* TODO: handle notification click */ }
                )
            }

            composable(
                route = Screen.DisasterList.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                DisasterListScreen(
                    disasters = disasterState.disasters,
                    onDisasterClick = { disaster ->
                        navController.navigate(Screen.DisasterDetail.createRoute(disaster.id))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onReportDisaster = {
                        navController.navigate(Screen.ReportDisaster.route)
                    },
                    isLoading = disasterState.isLoading
                )
            }

            composable(
                route = Screen.DisasterDetail.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) { backStackEntry ->
                val disasterId = backStackEntry.arguments?.getString("disasterId") ?: ""
                val disaster = disasterViewModel.getDisasterById(disasterId)

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
                route = Screen.ReportDisaster.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                val disasterViewModel: DisasterViewModel = hiltViewModel()
                val disasterState by disasterViewModel.uiState.collectAsState()
                val currentUser = currentUser
                var showDialog by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Only pop back stack when submission is successful
                if (disasterState.submissionSuccess) {
                    LaunchedEffect(Unit) {
                        disasterViewModel.clearSubmissionState()
                        navController.popBackStack()
                    }
                }

                ReportDisasterScreen(
                    navController = navController,
                    currentUserId = currentUser?.id ?: "",
                    onSubmit = { report, pickedLatLng ->
                        disasterViewModel.reportDisaster(report, pickedLatLng)
                    },
                    onClose = {
                        navController.popBackStack()
                    },
                    isLoading = disasterState.isSubmitting,
                    errorMessage = disasterState.error
                )

                // Show error dialog if submission fails
                if (disasterState.error != null) {
                    AlertDialog(
                        onDismissRequest = { disasterViewModel.clearMessages() },
                        title = { Text("Report Failed") },
                        text = { Text(disasterState.error ?: "Unknown error") },
                        confirmButton = {
                            TextButton(onClick = { disasterViewModel.clearMessages() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }

            composable(
                route = Screen.Map.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                MapScreen(
                    disasters = disasterState.disasters,
                    onDisasterClick = { disaster ->
                        navController.navigate(Screen.DisasterDetail.createRoute(disaster.id))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onFilterChange = { type ->
                        disasterViewModel.filterByType(type)
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
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Add settings, resources, and emergency screens
            composable(
                route = "settings",
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                com.example.gmls.ui.screens.settings.SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
            composable(
                route = "resources",
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                com.example.gmls.ui.screens.resources.ResourcesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "emergency",
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                com.example.gmls.ui.screens.emergency.EmergencyScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.LocationPicker.route
            ) {
                LocationPickerScreen(
                    onLocationPicked = { lat, lng ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("picked_location", Pair(lat, lng))
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun currentRoute(navController: NavController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
}

@Composable
fun NavDrawerContent(
    currentRoute: String?,
    onDestinationClicked: (route: String) -> Unit,
    onLogout: () -> Unit
) {
    // Implementation of the drawer content would go here
}