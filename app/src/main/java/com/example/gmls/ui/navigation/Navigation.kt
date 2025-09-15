package com.example.gmls.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import com.example.gmls.R
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
import com.example.gmls.ui.screens.notifications.NotificationsScreen
import com.example.gmls.domain.model.Notification
import com.example.gmls.ui.screens.profile.ProfileScreen
import com.example.gmls.ui.screens.location.LocationPickerScreen
import com.example.gmls.ui.screens.statistics.StatisticsScreen
import com.example.gmls.ui.screens.admin.AdminDashboardScreen
import com.example.gmls.ui.screens.admin.UserManagementScreen
import com.example.gmls.ui.screens.admin.AdminMapScreen
import com.example.gmls.ui.screens.analytics.AnalyticsScreen
import com.example.gmls.ui.viewmodels.AuthState
import com.example.gmls.ui.viewmodels.AuthViewModel
import com.example.gmls.ui.viewmodels.DisasterViewModel
import com.example.gmls.ui.viewmodels.ProfileViewModel
import com.example.gmls.ui.viewmodels.NotificationViewModel
import com.example.gmls.ui.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import com.example.gmls.ui.theme.AppTheme
import com.example.gmls.ui.screens.onboarding.OnboardingScreen
import com.example.gmls.data.local.OnboardingPreferenceManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu

// Consolidated Screen definitions - single source of truth
sealed class Screen(val route: String) {
    // Authentication screens
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    
    // Main user screens
    object Dashboard : Screen("dashboard")
    object DisasterList : Screen("disaster_list")
    object DisasterDetail : Screen("disaster_detail/{disasterId}") {
        fun createRoute(disasterId: String) = "disaster_detail/$disasterId"
    }
    object ReportDisaster : Screen("report_disaster")
    object Map : Screen("map")
    object Profile : Screen("profile")
    object LocationPicker : Screen("location_picker")
    object Statistics : Screen("statistics")
    
    // Settings and utility screens
    object Settings : Screen("settings")
    object Resources : Screen("resources")
    object Emergency : Screen("emergency")
    object Notifications : Screen("notifications")
    
    // Admin screens - consolidated from AdminScreen
    object AdminDashboard : Screen("admin_dashboard")
    object AdminUsers : Screen("admin/users")
    object AdminMap : Screen("admin/map")
    object AdminDisasters : Screen("admin/disasters")
    object AdminAddAdmin : Screen("admin/add")
    object AdminAnalytics : Screen("admin/analytics")
    object AdminSettings : Screen("admin/settings")
    
    // Additional admin screens
    object UserManagement : Screen("user_management")
    object Analytics : Screen("analytics")
    object DisasterManagement : Screen("disaster_management")
    object AuditLogs : Screen("audit_logs")
    object AddUser : Screen("add_user")
    
    // User analytics
    object UserAnalytics : Screen("user/analytics")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DisasterResponseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    disasterViewModel: DisasterViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel(),
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
                },
                currentUser = currentUser
            )
        },
        gesturesEnabled = isLoggedIn && currentRoute != Screen.Map.route && currentRoute != Screen.AdminMap.route
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
                    LaunchedEffect(currentUser) {
                        navigateToDashboard = true
                        if (currentUser?.role == "admin") {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
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
                        title = { Text(stringResource(R.string.registration_failed)) },
                        text = { Text((authState as AuthState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { authViewModel.resetAuthState() }) {
                                Text(stringResource(R.string.ok))
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
                        navController.navigate(Screen.Notifications.route)
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
                route = Screen.Notifications.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                val notificationViewModel: NotificationViewModel = hiltViewModel()
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { notification ->
                        // Mark notification as read
                        notificationViewModel.markAsRead(notification.id)
                        // Navigate to related content if available
                        notification.disasterId?.let { disasterId ->
                            navController.navigate(Screen.DisasterDetail.createRoute(disasterId))
                        }
                    },
                    viewModel = notificationViewModel
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
                            // Set the disaster ID in savedStateHandle for MapScreen to pick up
                            navController.currentBackStackEntry?.savedStateHandle?.set("focus_disaster_id", disasterId)
                            // The line below was causing the error and is removed as its functionality
                            // for MapScreen's focusDisaster parameter is handled by savedStateHandle
                            // disasterViewModel.focusOnDisaster(disaster)
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
                    onSubmit = { report -> 
                        disasterViewModel.reportDisaster(report)
                    },
                    onClose = {
                        navController.popBackStack()
                    },
                    isLoadingFromViewModel = disasterState.isSubmitting,
                    errorMessageFromViewModel = disasterState.error,
                    successMessageFromViewModel = if (disasterState.submissionSuccess) LocalContext.current.getString(R.string.report_submitted_successfully) else null
                )

                // Show error dialog if submission fails
                if (disasterState.error != null) {
                    AlertDialog(
                        onDismissRequest = { disasterViewModel.clearMessages() },
                        title = { Text(stringResource(R.string.report_failed_title)) },
                        text = { Text(disasterState.error ?: stringResource(R.string.unknown_error_occurred)) },
                        confirmButton = {
                            TextButton(onClick = { disasterViewModel.clearMessages() }) {
                                Text(stringResource(R.string.ok))
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
                val focusDisasterId = navController.previousBackStackEntry?.savedStateHandle?.get<String>("focus_disaster_id")
                val focusDisaster = focusDisasterId?.let { id -> 
                    disasterState.disasters.find { it.id == id } ?: disasterViewModel.getDisasterById(id)
                }
                
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
                    },
                    focusDisaster = focusDisaster
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
                route = Screen.Settings.route,
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
                route = Screen.Resources.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                com.example.gmls.ui.screens.resources.ResourcesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Emergency.route,
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
                        // Save latitude and longitude separately as primitive types
                        navController.previousBackStackEntry?.savedStateHandle?.set("picked_latitude", lat)
                        navController.previousBackStackEntry?.savedStateHandle?.set("picked_longitude", lng)
                        // Immediately pop back to avoid any race conditions
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Statistics.route,
                enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) }
            ) {
                StatisticsScreen(
                    disasters = disasterState.disasters
                )
            }

            // Admin Dashboard
            composable(Screen.AdminDashboard.route) {
                // Check if user is admin
                val currentUser by authViewModel.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"
                // More robust admin check with back navigation prevention
                LaunchedEffect(currentUser) {
                    if (currentUser != null && !isAdmin) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (currentUser == null) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                BackHandler(enabled = true) { }
                if (!isAdmin || currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                
                LaunchedEffect(Unit) { adminViewModel.loadAllData() }
                val adminState by adminViewModel.adminState.collectAsState()
                val scope = rememberCoroutineScope()
                Scaffold(
                    topBar = {
                        AdminTopAppBar(
                            title = stringResource(R.string.admin_dashboard),
                            onNavigationIconClick = { scope.launch { drawerState.open() } }
                        )
                    }
                ) { paddingValues ->
                    AdminDashboardScreen(
                        paddingValues = paddingValues,
                        adminState = adminState,
                        adminViewModel = adminViewModel,
                        onNavigateToUserManagement = {
                            navController.navigate(Screen.AdminUsers.route) { launchSingleTop = true }
                        },
                        onNavigateToAnalytics = {
                            navController.navigate(Screen.AdminAnalytics.route) { launchSingleTop = true }
                        },
                        onNavigateToMap = {
                            navController.navigate(Screen.AdminMap.route) { launchSingleTop = true }
                        },
                        onNavigateToDisasterManagement = {
                            navController.navigate(Screen.AdminDisasters.route) { launchSingleTop = true }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.AdminSettings.route) { launchSingleTop = true }
                        },
                        onNavigateToAuditLogs = { },
                        onNavigateToAddUser = {
                            navController.navigate(Screen.AdminAddAdmin.route) { launchSingleTop = true }
                        },
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
            
            // User Management
            composable(Screen.AdminUsers.route) {
                // Check if user is admin
                val currentUser by authViewModel.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"
                
                LaunchedEffect(isAdmin) {
                    if (!isAdmin) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminUsers.route) { inclusive = true }
                        }
                    }
                }
                
                if (!isAdmin || currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                
                // Load users when the screen is first displayed
                LaunchedEffect(Unit) {
                    disasterViewModel.loadData()
                }
                
                // Use the shared disasterViewModel state
                val adminState by disasterViewModel.uiState.collectAsState()
                
                // Load actual user data for user management
                LaunchedEffect(Unit) {
                    adminViewModel.loadAllData()
                }
                
                val adminViewModelState by adminViewModel.adminState.collectAsState()
                
                // Enhanced UserManagementScreen with real user data
                UserManagementScreen(
                    paddingValues = PaddingValues(0.dp),
                    users = adminViewModelState.users,
                    adminViewModel = adminViewModel,
                    onUserClick = { user: User ->
                        // The enhanced screen now handles user details internally
                        // No need for external navigation for user details
                    },
                    onVerifyUser = { user: User ->
                        adminViewModel.verifyUser(user)
                    },
                    onToggleUserStatus = { user: User ->
                        adminViewModel.toggleUserStatus(user)
                    }
                )
            }
            
            // Admin Map
            composable(Screen.AdminMap.route) {
                // Check if user is admin
                val currentUser by authViewModel.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"
                if (!isAdmin || currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                // Load data
                LaunchedEffect(Unit) {
                    disasterViewModel.loadData()
                    // Load user location data for admin map
                    adminViewModel.loadUsersWithLocationData()
                }
                
                val adminState = disasterState
                val adminViewModelState by adminViewModel.adminState.collectAsState()
                
                // Use the new AdminMapScreen with enhanced features
                AdminMapScreen(
                    disasters = adminState.disasters,
                    users = adminViewModel.getUsersWithLocation(),
                    modifier = Modifier.fillMaxSize(),
                    drawerState = drawerState,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Admin Disaster Management
            composable(Screen.AdminDisasters.route) {
                // Check if user is admin
                val currentUser by authViewModel.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"
                
                if (!isAdmin || currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                // Load data
                LaunchedEffect(Unit) {
                    disasterViewModel.loadData()
                }
                
                val adminState = disasterState
                
                // Use the DisasterManagementScreen from AdminDashboardScreen
                com.example.gmls.ui.screens.admin.DisasterManagementScreen(
                    paddingValues = PaddingValues(0.dp),
                    disasters = adminState.disasters,
                    onDisasterClick = { disaster -> 
                        navController.navigate(Screen.DisasterDetail.createRoute(disaster.id))
                    },
                    adminViewModel = adminViewModel
                )
            }
            
            // Admin Add Admin
            composable(Screen.AdminAddAdmin.route) {
                // Check if user is admin
                val isAdmin = currentUser?.role == "admin"
                
                if (!isAdmin) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminAddAdmin.route) { inclusive = true }
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                
                // Create a specialized admin creation screen
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var fullName by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var showSuccessDialog by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                
                val authState by authViewModel.authState.collectAsState()
                
                // Handle state changes
                LaunchedEffect(authState) {
                    isLoading = authState is AuthState.Loading
                    
                    if (authState is AuthState.Authenticated) {
                        showSuccessDialog = true
                    }
                    
                    val currentAuthState = authState
                    if (currentAuthState is AuthState.Error) {
                        errorMessage = currentAuthState.message
                    }
                }
                
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.create_admin_user)) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Admin icon
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = stringResource(R.string.administrator_description),
                            modifier = Modifier
                                .size(80.dp)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            stringResource(R.string.create_admin_user),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Error message
                        if (errorMessage != null) {
                            Text(
                                errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Full name field
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(stringResource(R.string.full_name_label)) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(stringResource(R.string.email_label)) },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Password field
                        var passwordVisible by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.password)) },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password_description) else stringResource(R.string.show_password_description)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Create button
                        Button(
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()) {
                                    // Use authViewModel to register admin user
                                    adminViewModel.createAdmin(email, password, fullName)
                                } else {
                                    errorMessage = context.getString(R.string.please_fill_all_fields)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(stringResource(R.string.create_admin_user))
                            }
                        }
                    }
                }
                
                // Success dialog
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text(stringResource(R.string.admin_created_title)) },
                        text = { Text(stringResource(R.string.admin_created_message)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    navController.navigate(Screen.AdminDashboard.route) {
                                        popUpTo(Screen.AdminAddAdmin.route) { inclusive = true }
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.go_to_dashboard))
                            }
                        }
                    )
                }
            }
            
            // User Analytics
            composable(Screen.UserAnalytics.route) {
                // Check if user is logged in
                val currentUser by authViewModel.currentUser.collectAsState()
                
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        // User is not logged in, redirect to login
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.UserAnalytics.route) { inclusive = true }
                        }
                    }
                }
                
                if (currentUser != null) {
                    AnalyticsScreen(
                        onBackClick = { navController.navigateUp() },
                        isAdminView = currentUser?.role == "admin"
                    )
                } else {
                    // Show loading indicator while checking auth state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Admin Analytics
            composable(Screen.AdminAnalytics.route) {
                // Check if user is admin
                val currentUser by authViewModel.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"
                
                if (!isAdmin || currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                AnalyticsScreen(
                    onBackClick = { navController.navigateUp() },
                    isAdminView = true
                )
            }
            
            // Admin Settings
            composable(Screen.AdminSettings.route) {
                // Check if user is admin
                val currentUser by authViewModel.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"
                
                if (!isAdmin || currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@composable
                }
                com.example.gmls.ui.screens.settings.SettingsScreen(
                    onBackClick = { navController.navigateUp() },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
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
    onLogout: () -> Unit,
    currentUser: User?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header section with user info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser?.profilePictureUrl?.isNotEmpty() == true) {
                        // If user has profile picture, show it
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile_picture),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        // Show first letter of name
                        Text(
                            text = currentUser?.fullName?.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // User name
                Text(
                    text = currentUser?.fullName ?: stringResource(R.string.guest_user),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                
                // User email
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                // User role badge
                if (currentUser?.role?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (currentUser.role.lowercase()) {
                                "admin" -> stringResource(R.string.admin_role)
                                "moderator" -> stringResource(R.string.moderator_role)
                                else -> stringResource(R.string.user_role)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Navigation menu items
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Common navigation items for all users
            item {
                DrawerMenuItem(
                    icon = Icons.Default.Dashboard,
                    label = stringResource(R.string.dashboard),
                    isSelected = currentRoute == Screen.Dashboard.route,
                    onClick = { onDestinationClicked(Screen.Dashboard.route) }
                )
            }
            
            item {
                DrawerMenuItem(
                    icon = Icons.Default.ReportProblem,
                    label = stringResource(R.string.report_disaster),
                    isSelected = currentRoute == Screen.ReportDisaster.route,
                    onClick = { onDestinationClicked(Screen.ReportDisaster.route) }
                )
            }
            
            item {
                DrawerMenuItem(
                    icon = Icons.Default.List,
                    label = stringResource(R.string.disaster_list),
                    isSelected = currentRoute == Screen.DisasterList.route,
                    onClick = { onDestinationClicked(Screen.DisasterList.route) }
                )
            }
            
            item {
                DrawerMenuItem(
                    icon = Icons.Default.Notifications,
                    label = stringResource(R.string.notifications),
                    isSelected = currentRoute == Screen.Notifications.route,
                    onClick = { onDestinationClicked(Screen.Notifications.route) }
                )
            }
            
            item {
                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.profile),
                    isSelected = currentRoute == Screen.Profile.route,
                    onClick = { onDestinationClicked(Screen.Profile.route) }
                )
            }
            
            // Admin-only items
            if (currentUser?.role == "admin") {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Text(
                        text = stringResource(R.string.admin_section),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    DrawerMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        label = stringResource(R.string.admin_dashboard),
                        isSelected = currentRoute == Screen.AdminDashboard.route,
                        onClick = { onDestinationClicked(Screen.AdminDashboard.route) }
                    )
                }
                
                item {
                    DrawerMenuItem(
                        icon = Icons.Default.People,
                        label = stringResource(R.string.user_management),
                        isSelected = currentRoute == Screen.UserManagement.route,
                        onClick = { onDestinationClicked(Screen.UserManagement.route) }
                    )
                }
                
                item {
                    DrawerMenuItem(
                        icon = Icons.Default.Map,
                        label = stringResource(R.string.admin_map),
                        isSelected = currentRoute == Screen.AdminMap.route,
                        onClick = { onDestinationClicked(Screen.AdminMap.route) }
                    )
                }
                
                item {
                    DrawerMenuItem(
                        icon = Icons.Default.Analytics,
                        label = stringResource(R.string.analytics),
                        isSelected = currentRoute == Screen.AdminAnalytics.route,
                        onClick = { onDestinationClicked(Screen.AdminAnalytics.route) }
                    )
                }
            }
            
            // Settings and other items
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    isSelected = currentRoute == Screen.Settings.route,
                    onClick = { onDestinationClicked(Screen.Settings.route) }
                )
            }
            
            item {
                DrawerMenuItem(
                    icon = Icons.Default.Help,
                    label = stringResource(R.string.help_support),
                    isSelected = false,
                    onClick = { 
                        // Handle help/support navigation
                        onDestinationClicked("help")
                    }
                )
            }
        }
        
        // Logout button at bottom
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.logout),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun AdminTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_drawer))
            }
        }
    )
}
