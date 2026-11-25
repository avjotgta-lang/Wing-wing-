package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.PortalRole
import com.example.ui.components.CartCheckoutSheet
import com.example.ui.components.OrderConfirmedDialog
import com.example.ui.components.OrderTrackDialog
import com.example.ui.components.RoleGateDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OwnerPortalScreen
import com.example.ui.screens.RestaurantPortalScreen
import com.example.ui.screens.StorefrontScreen
import com.example.ui.screens.WorkerPortalScreen
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.WingWingTheme
import com.example.ui.viewmodel.WingWingViewModel

object WingWingRoutes {
    const val LOGIN = "login"
    const val STOREFRONT = "storefront"
    const val RESTAURANT_PORTAL = "portal_restaurant"
    const val WORKER_PORTAL = "portal_worker"
    const val OWNER_PORTAL = "portal_owner"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WingWingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.preferences.isDarkTheme) {
                if (uiState.preferences.isDarkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    )
                }
            }

            WingWingTheme(darkTheme = uiState.preferences.isDarkTheme) {
                WingWingAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun WingWingAppRoot(viewModel: WingWingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCartSheet by remember { mutableStateOf(false) }
    var gatedRoleRequest by remember { mutableStateOf<PortalRole?>(null) }

    LaunchedEffect(uiState.toastMessage) {
        val msg = uiState.toastMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Automatically route between Login and Storefront when auth state changes on root
    LaunchedEffect(uiState.isLoaded, uiState.isSignedInOrGuest) {
        if (!uiState.isLoaded) return@LaunchedEffect
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (uiState.isSignedInOrGuest && currentRoute == WingWingRoutes.LOGIN) {
            navController.navigate(WingWingRoutes.STOREFRONT) {
                popUpTo(WingWingRoutes.LOGIN) { inclusive = true }
            }
        } else if (!uiState.isSignedInOrGuest && currentRoute == WingWingRoutes.STOREFRONT) {
            navController.navigate(WingWingRoutes.LOGIN) {
                popUpTo(WingWingRoutes.STOREFRONT) { inclusive = true }
            }
        }
    }

    val openPortal: (PortalRole) -> Unit = { role ->
        val route = when (role) {
            PortalRole.RESTAURANT -> WingWingRoutes.RESTAURANT_PORTAL
            PortalRole.WORKER -> WingWingRoutes.WORKER_PORTAL
            PortalRole.OWNER -> WingWingRoutes.OWNER_PORTAL
        }
        if (uiState.unlockedPortals.contains(role)) {
            navController.navigate(route)
        } else {
            val remembered = uiState.preferences.rememberedPortals[role]
            if (remembered != null) {
                viewModel.tryAutoUnlockRememberedPortal(role) { ok ->
                    if (ok) {
                        navController.navigate(route)
                    } else {
                        gatedRoleRequest = role
                    }
                }
            } else {
                gatedRoleRequest = role
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!uiState.isLoaded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CoralPrimary)
            }
        } else {
            val startDest = if (uiState.isSignedInOrGuest) {
                WingWingRoutes.STOREFRONT
            } else {
                WingWingRoutes.LOGIN
            }

            NavHost(
                navController = navController,
                startDestination = startDest
            ) {
                composable(WingWingRoutes.LOGIN) {
                    LoginScreen(
                        uiState = uiState,
                        onToggleTheme = viewModel::toggleTheme,
                        onSignInCustomer = viewModel::signInCustomer,
                        onSignInWithGoogleProfile = viewModel::signInWithGoogleProfile,
                        onContinueAsGuest = viewModel::continueAsGuest,
                        onForgetSavedCustomer = viewModel::forgetSavedCustomerLogin,
                        onOpenPortal = openPortal
                    )
                }

                composable(WingWingRoutes.STOREFRONT) {
                    StorefrontScreen(
                        uiState = uiState,
                        onSelectTab = viewModel::selectTab,
                        onSearchChange = viewModel::updateSearchQuery,
                        onCategorySelect = viewModel::selectCategory,
                        onAddToCart = viewModel::addToCart,
                        onDecrementFromCart = viewModel::decrementFromCart,
                        onOpenCart = { showCartSheet = true },
                        onTrackOrder = { orderId -> viewModel.openOrderTracker(orderId) },
                        onReorder = viewModel::reorderPastOrder,
                        onSubmitFeedback = viewModel::submitFeedback,
                        onToggleTheme = viewModel::toggleTheme,
                        onSignOut = viewModel::signOutCustomer,
                        onOpenPortal = openPortal
                    )
                }

                composable(WingWingRoutes.RESTAURANT_PORTAL) {
                    RestaurantPortalScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onLockPortal = {
                            viewModel.lockPortal(PortalRole.RESTAURANT)
                            navController.popBackStack()
                        },
                        onForgetSavedLogin = {
                            viewModel.forgetSavedPortal(PortalRole.RESTAURANT)
                            navController.popBackStack()
                        },
                        onUpdateOrderStatus = viewModel::updateOrderStatus,
                        onTrackOrder = { orderId -> viewModel.openOrderTracker(orderId) },
                        onToggleDishAvailability = viewModel::toggleDishAvailability,
                        onAddDish = viewModel::addDish
                    )
                }

                composable(WingWingRoutes.WORKER_PORTAL) {
                    WorkerPortalScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onLockPortal = {
                            viewModel.lockPortal(PortalRole.WORKER)
                            navController.popBackStack()
                        },
                        onForgetSavedLogin = {
                            viewModel.forgetSavedPortal(PortalRole.WORKER)
                            navController.popBackStack()
                        },
                        onSaveWorkerPhone = viewModel::saveWorkerPhone,
                        onSetNotificationsEnabled = viewModel::setWorkerNotificationsEnabled,
                        onRingTestAlert = viewModel::ringWorkerTestAlert,
                        onMarkNotificationRead = viewModel::markWorkerNotificationRead,
                        onMarkAllNotificationsRead = viewModel::markAllWorkerNotificationsRead,
                        onClearNotifications = viewModel::clearWorkerNotifications,
                        onUpdateOrderStatus = viewModel::updateOrderStatus,
                        onTrackOrder = { orderId -> viewModel.openOrderTracker(orderId) }
                    )
                }

                composable(WingWingRoutes.OWNER_PORTAL) {
                    OwnerPortalScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onLockPortal = {
                            viewModel.lockPortal(PortalRole.OWNER)
                            navController.popBackStack()
                        },
                        onForgetSavedLogin = {
                            viewModel.forgetSavedPortal(PortalRole.OWNER)
                            navController.popBackStack()
                        },
                        onTrackOrder = { orderId -> viewModel.openOrderTracker(orderId) },
                        onAddKitchen = viewModel::addKitchen,
                        onToggleKitchenActive = viewModel::toggleKitchenActive,
                        onCreatePortalCredential = viewModel::createPortalCredential,
                        onRevokeCredential = viewModel::revokePortalCredential,
                        onCreateCoupon = viewModel::createCoupon,
                        onToggleCouponActive = viewModel::toggleCouponActive,
                        onDeleteCoupon = viewModel::deleteCoupon,
                        onUpdateCustomQr = viewModel::updateCustomQrImage,
                        onResetQrDefault = viewModel::resetQrToDefault,
                        onShowToast = viewModel::showToast
                    )
                }
            }
        }

        // Checkout Sheet
        if (showCartSheet) {
            CartCheckoutSheet(
                uiState = uiState,
                discountInr = viewModel.calculateCurrentDiscount(),
                onDismiss = { showCartSheet = false },
                onIncrement = { dishId ->
                    uiState.dishes.firstOrNull { it.id == dishId }?.let(viewModel::addToCart)
                },
                onDecrement = viewModel::decrementFromCart,
                onRemove = viewModel::removeFromCart,
                onApplyCoupon = viewModel::applyCouponCode,
                onRemoveCoupon = viewModel::removeCoupon,
                onDetectGps = viewModel::detectGpsLocation,
                onReverseGeocode = viewModel::reverseGeocodePin,
                onPlaceOrder = { phone, address, lat, lng, payment ->
                    viewModel.placeOrder(phone, address, lat, lng, payment)
                    showCartSheet = false
                }
            )
        }

        // Order Confirmation Receipt Dialog
        uiState.latestPlacedOrder?.let { placedOrder ->
            OrderConfirmedDialog(
                order = placedOrder,
                onTrackOrder = {
                    val id = placedOrder.id
                    viewModel.clearLatestPlacedOrder()
                    viewModel.openOrderTracker(id)
                },
                onDismiss = { viewModel.clearLatestPlacedOrder() }
            )
        }

        // Live Order Tracker Dialog
        val trackedOrder = uiState.trackedOrderId?.let { id ->
            uiState.orders.firstOrNull { it.id == id }
        }
        if (trackedOrder != null) {
            OrderTrackDialog(
                order = trackedOrder,
                kitchens = uiState.kitchens,
                onDismiss = { viewModel.openOrderTracker(null) }
            )
        }

        // Role Gate Dialog
        gatedRoleRequest?.let { role ->
            RoleGateDialog(
                role = role,
                rememberedCredential = uiState.preferences.rememberedPortals[role],
                onDismiss = { gatedRoleRequest = null },
                onAuthenticate = { key, user, pass, rememberMe ->
                    viewModel.verifyAndUnlockPortal(role, key, user, pass, rememberMe)
                },
                onForgetSaved = { viewModel.forgetSavedPortal(role) },
                onUnlocked = {
                    val targetRole = role
                    gatedRoleRequest = null
                    val route = when (targetRole) {
                        PortalRole.RESTAURANT -> WingWingRoutes.RESTAURANT_PORTAL
                        PortalRole.WORKER -> WingWingRoutes.WORKER_PORTAL
                        PortalRole.OWNER -> WingWingRoutes.OWNER_PORTAL
                    }
                    navController.navigate(route)
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 12.dp)
        ) { data ->
            Snackbar(snackbarData = data)
        }
    }
}
