package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesRepository
import com.example.data.local.WingWingDatabase
import com.example.data.model.AppPreferences
import com.example.data.model.CartItem
import com.example.data.model.CouponEntity
import com.example.data.model.DiscountType
import com.example.data.model.DishCategory
import com.example.data.model.DishEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PortalCredentialEntity
import com.example.data.model.PortalRole
import com.example.data.model.StorefrontTab
import com.example.data.model.WorkerNotificationEntity
import com.example.data.repository.WingWingRepository
import com.example.util.AlertNotifier
import com.example.util.LocationHelper
import com.example.util.QrCodeMatrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WingWingUiState(
    val isLoaded: Boolean = false,
    val preferences: AppPreferences = AppPreferences(),
    val kitchens: List<KitchenEntity> = emptyList(),
    val dishes: List<DishEntity> = emptyList(),
    val orders: List<OrderEntity> = emptyList(),
    val coupons: List<CouponEntity> = emptyList(),
    val feedbacks: List<FeedbackEntity> = emptyList(),
    val credentials: List<PortalCredentialEntity> = emptyList(),
    val workerNotifications: List<WorkerNotificationEntity> = emptyList(),
    val selectedTab: StorefrontTab = StorefrontTab.MENU,
    val searchQuery: String = "",
    val selectedCategory: DishCategory = DishCategory.ALL,
    val cartQuantities: Map<String, Int> = emptyMap(),
    val appliedCouponCode: String? = null,
    val couponError: String? = null,
    val isDetectingLocation: Boolean = false,
    val locationMessage: String? = null,
    val trackedOrderId: String? = null,
    val latestPlacedOrder: OrderEntity? = null,
    val toastMessage: String? = null,
    val unlockedPortals: Set<PortalRole> = emptySet()
) {
    val cartItems: List<CartItem>
        get() = cartQuantities.mapNotNull { (dishId, qty) ->
            val dish = dishes.firstOrNull { it.id == dishId }
            if (dish != null && qty > 0) CartItem(dish, qty) else null
        }

    val cartCount: Int
        get() = cartQuantities.values.sum()

    val cartSubtotal: Int
        get() = cartItems.sumOf { it.dish.priceInr * it.quantity }

    val appliedCoupon: CouponEntity?
        get() = appliedCouponCode?.let { code ->
            coupons.firstOrNull { it.code.equals(code, ignoreCase = true) }
        }

    val unreadWorkerNotificationsCount: Int
        get() = workerNotifications.count { !it.isRead }

    val isSignedInOrGuest: Boolean
        get() = preferences.currentCustomerEmail.isNotBlank() || preferences.isGuestSession
}

class WingWingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WingWingRepository
    private val localState = MutableStateFlow(LocalInteractionState())

    private data class LocalInteractionState(
        val isLoaded: Boolean = false,
        val selectedTab: StorefrontTab = StorefrontTab.MENU,
        val searchQuery: String = "",
        val selectedCategory: DishCategory = DishCategory.ALL,
        val cartQuantities: Map<String, Int> = emptyMap(),
        val appliedCouponCode: String? = null,
        val couponError: String? = null,
        val isDetectingLocation: Boolean = false,
        val locationMessage: String? = null,
        val trackedOrderId: String? = null,
        val latestPlacedOrder: OrderEntity? = null,
        val toastMessage: String? = null,
        val unlockedPortals: Set<PortalRole> = emptySet()
    )

    val uiState: StateFlow<WingWingUiState>

    init {
        val db = WingWingDatabase.getInstance(application)
        val prefsRepo = PreferencesRepository(application)
        repository = WingWingRepository(db.wingWingDao(), prefsRepo)
        AlertNotifier.ensureChannel(application)

        viewModelScope.launch {
            repository.ensureSeeded()
            localState.update { it.copy(isLoaded = true) }
        }

        val dataFlow = combine(
            repository.preferences,
            repository.kitchens,
            repository.dishes,
            repository.orders,
            repository.coupons
        ) { prefs, kitchens, dishes, orders, coupons ->
            DataBundle1(prefs, kitchens, dishes, orders, coupons)
        }

        val secondaryFlow = combine(
            repository.feedbacks,
            repository.credentials,
            repository.workerNotifications,
            localState
        ) { feedbacks, creds, notifs, local ->
            DataBundle2(feedbacks, creds, notifs, local)
        }

        uiState = combine(dataFlow, secondaryFlow) { b1, b2 ->
            WingWingUiState(
                isLoaded = b2.local.isLoaded,
                preferences = b1.prefs,
                kitchens = b1.kitchens,
                dishes = b1.dishes,
                orders = b1.orders,
                coupons = b1.coupons,
                feedbacks = b2.feedbacks,
                credentials = b2.credentials,
                workerNotifications = b2.notifs,
                selectedTab = b2.local.selectedTab,
                searchQuery = b2.local.searchQuery,
                selectedCategory = b2.local.selectedCategory,
                cartQuantities = b2.local.cartQuantities,
                appliedCouponCode = b2.local.appliedCouponCode,
                couponError = b2.local.couponError,
                isDetectingLocation = b2.local.isDetectingLocation,
                locationMessage = b2.local.locationMessage,
                trackedOrderId = b2.local.trackedOrderId,
                latestPlacedOrder = b2.local.latestPlacedOrder,
                toastMessage = b2.local.toastMessage,
                unlockedPortals = b2.local.unlockedPortals
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WingWingUiState()
        )
    }

    private data class DataBundle1(
        val prefs: AppPreferences,
        val kitchens: List<KitchenEntity>,
        val dishes: List<DishEntity>,
        val orders: List<OrderEntity>,
        val coupons: List<CouponEntity>
    )

    private data class DataBundle2(
        val feedbacks: List<FeedbackEntity>,
        val credentials: List<PortalCredentialEntity>,
        val notifs: List<WorkerNotificationEntity>,
        val local: LocalInteractionState
    )

    fun toggleTheme() {
        viewModelScope.launch {
            repository.setDarkTheme(!uiState.value.preferences.isDarkTheme)
        }
    }

    fun showToast(message: String) {
        localState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        localState.update { it.copy(toastMessage = null) }
    }

    fun signInCustomer(email: String, password: String, rememberMe: Boolean) {
        val cleanEmail = email.trim()
        val displayName = cleanEmail.substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            .ifBlank { "WingWing Customer" }

        viewModelScope.launch {
            repository.saveCustomerSession(
                email = cleanEmail,
                name = displayName,
                password = password,
                rememberMe = rememberMe,
                isGuest = false
            )
            showToast("Welcome back, $displayName")
        }
    }

    fun signInWithGoogleProfile(name: String, email: String, rememberMe: Boolean) {
        viewModelScope.launch {
            repository.saveCustomerSession(
                email = email.trim(),
                name = name.trim().ifBlank { "Google User" },
                password = "",
                rememberMe = rememberMe,
                isGuest = false
            )
            showToast("Signed in as ${name.trim().ifBlank { email }}")
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            repository.saveCustomerSession(
                email = "",
                name = "Guest Diner",
                password = "",
                rememberMe = false,
                isGuest = true
            )
        }
    }

    fun signOutCustomer() {
        viewModelScope.launch {
            repository.clearCustomerSession()
            localState.update { it.copy(selectedTab = StorefrontTab.MENU) }
        }
    }

    fun forgetSavedCustomerLogin() {
        viewModelScope.launch {
            repository.forgetSavedCustomerLogin()
            showToast("Saved customer login cleared")
        }
    }

    fun selectTab(tab: StorefrontTab) {
        localState.update { it.copy(selectedTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        localState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: DishCategory) {
        localState.update { it.copy(selectedCategory = category) }
    }

    fun addToCart(dish: DishEntity) {
        if (!dish.isAvailable) {
            showToast("${dish.name} is currently paused by the kitchen")
            return
        }
        localState.update { state ->
            val current = state.cartQuantities[dish.id] ?: 0
            state.copy(cartQuantities = state.cartQuantities + (dish.id to (current + 1)))
        }
    }

    fun decrementFromCart(dishId: String) {
        localState.update { state ->
            val current = state.cartQuantities[dishId] ?: 0
            val nextMap = if (current <= 1) {
                state.cartQuantities - dishId
            } else {
                state.cartQuantities + (dishId to (current - 1))
            }
            state.copy(cartQuantities = nextMap)
        }
    }

    fun removeFromCart(dishId: String) {
        localState.update { state ->
            state.copy(cartQuantities = state.cartQuantities - dishId)
        }
    }

    fun reorderPastOrder(order: OrderEntity) {
        val availableDishes = uiState.value.dishes.filter { it.isAvailable }.associateBy { it.id }
        val restored = mutableMapOf<String, Int>()
        var unavailableCount = 0
        order.items.forEach { item ->
            if (availableDishes.containsKey(item.dishId)) {
                restored[item.dishId] = (restored[item.dishId] ?: 0) + item.quantity
            } else {
                unavailableCount++
            }
        }
        if (restored.isEmpty()) {
            showToast("None of the dishes from ${order.id} are currently in stock.")
            return
        }
        localState.update { state ->
            state.copy(
                cartQuantities = restored,
                appliedCouponCode = null,
                couponError = null,
                toastMessage = if (unavailableCount > 0) {
                    "Added available items from ${order.id} ($unavailableCount unavailable)"
                } else {
                    "Re-ordered ${order.id} into your bag!"
                }
            )
        }
    }

    fun applyCouponCode(rawCode: String) {
        val clean = rawCode.trim().uppercase()
        if (clean.isEmpty()) {
            localState.update { it.copy(appliedCouponCode = null, couponError = null) }
            return
        }
        val coupon = uiState.value.coupons.firstOrNull { it.code.equals(clean, ignoreCase = true) }
        if (coupon == null) {
            localState.update { it.copy(couponError = "Invalid coupon code: $clean") }
            return
        }
        val (_, error) = repository.calculateDiscount(coupon, uiState.value.cartSubtotal)
        if (error != null) {
            localState.update { it.copy(couponError = error) }
        } else {
            localState.update {
                it.copy(
                    appliedCouponCode = coupon.code,
                    couponError = null,
                    toastMessage = "Coupon ${coupon.code} applied!"
                )
            }
        }
    }

    fun removeCoupon() {
        localState.update { it.copy(appliedCouponCode = null, couponError = null) }
    }

    fun calculateCurrentDiscount(): Int {
        val state = uiState.value
        val (discount, _) = repository.calculateDiscount(state.appliedCoupon, state.cartSubtotal)
        return discount
    }

    fun detectGpsLocation(onLocationResolved: (String, Double, Double) -> Unit) {
        val context = getApplication<Application>()
        localState.update { it.copy(isDetectingLocation = true, locationMessage = "Detecting GPS position...") }
        viewModelScope.launch {
            val result = LocationHelper.detectCurrentLocation(context)
            result.onSuccess { loc ->
                localState.update {
                    it.copy(
                        isDetectingLocation = false,
                        locationMessage = "GPS locked: ${loc.formattedAddress}"
                    )
                }
                onLocationResolved(loc.formattedAddress, loc.lat, loc.lng)
            }.onFailure { err ->
                localState.update {
                    it.copy(
                        isDetectingLocation = false,
                        locationMessage = err.message ?: "Could not read GPS"
                    )
                }
            }
        }
    }

    fun reverseGeocodePin(lat: Double, lng: Double, onResolved: (String) -> Unit) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val addr = LocationHelper.reverseGeocode(context, lat, lng)
            onResolved(addr)
        }
    }

    fun placeOrder(
        phone: String,
        address: String,
        lat: Double,
        lng: Double,
        paymentMethod: PaymentMethod
    ) {
        val state = uiState.value
        if (state.cartItems.isEmpty()) return

        viewModelScope.launch {
            val order = repository.placeOrder(
                customerName = state.preferences.currentCustomerName.ifBlank { "Guest Diner" },
                customerEmail = state.preferences.currentCustomerEmail,
                customerPhone = phone,
                deliveryAddress = address,
                deliveryLat = lat,
                deliveryLng = lng,
                cartItems = state.cartItems,
                appliedCoupon = state.appliedCoupon,
                paymentMethod = paymentMethod
            )
            localState.update {
                it.copy(
                    cartQuantities = emptyMap(),
                    appliedCouponCode = null,
                    couponError = null,
                    latestPlacedOrder = order,
                    toastMessage = "Order ${order.id} placed!"
                )
            }
            if (state.preferences.workerNotificationsEnabled) {
                AlertNotifier.sendWorkerDispatchNotification(
                    context = getApplication(),
                    orderId = order.id,
                    totalInr = order.totalInr,
                    address = order.deliveryAddress,
                    playChime = true
                )
            }
        }
    }

    fun clearLatestPlacedOrder() {
        localState.update { it.copy(latestPlacedOrder = null) }
    }

    fun openOrderTracker(orderId: String?) {
        localState.update { it.copy(trackedOrderId = orderId) }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val riderPhone = if (status == OrderStatus.PICKED_UP) {
                uiState.value.preferences.workerPhone
            } else null
            repository.updateOrderStatus(orderId, status, riderPhone)
            showToast("Order $orderId → ${status.label}")
        }
    }

    fun submitFeedback(orderId: String?, rating: Int, comment: String) {
        val customerName = uiState.value.preferences.currentCustomerName.ifBlank { "WingWing Customer" }
        viewModelScope.launch {
            repository.submitFeedback(orderId, customerName, rating, comment)
            showToast("Thank you for your $rating★ feedback!")
        }
    }

    // Partner Portal Auth
    suspend fun verifyAndUnlockPortal(
        role: PortalRole,
        portalKey: String,
        username: String,
        password: String,
        rememberMe: Boolean
    ): Boolean {
        val matched = repository.verifyPortalLogin(role, portalKey, username, password, rememberMe)
        return if (matched != null) {
            localState.update { it.copy(unlockedPortals = it.unlockedPortals + role) }
            true
        } else {
            false
        }
    }

    fun tryAutoUnlockRememberedPortal(role: PortalRole, onResult: (Boolean) -> Unit) {
        val remembered = uiState.value.preferences.rememberedPortals[role]
        if (remembered == null) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val ok = verifyAndUnlockPortal(
                role = role,
                portalKey = remembered.portalKey,
                username = remembered.username,
                password = remembered.password,
                rememberMe = true
            )
            onResult(ok)
        }
    }

    fun lockPortal(role: PortalRole) {
        localState.update { it.copy(unlockedPortals = it.unlockedPortals - role) }
    }

    fun forgetSavedPortal(role: PortalRole) {
        viewModelScope.launch {
            repository.forgetPortalLogin(role)
            localState.update { it.copy(unlockedPortals = it.unlockedPortals - role) }
            showToast("Forgot saved ${role.label} login")
        }
    }

    // Restaurant Operations
    fun toggleDishAvailability(dishId: String, available: Boolean) {
        viewModelScope.launch {
            repository.toggleDishAvailability(dishId, available)
        }
    }

    fun addDish(
        name: String,
        kitchen: KitchenEntity,
        category: DishCategory,
        priceInr: Int,
        prepMinutes: Int,
        description: String,
        tag: String,
        pickedImageUri: Uri?
    ) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val savedPath = pickedImageUri?.let {
                QrCodeMatrix.copyUriToInternalFile(context, it, "dish")
            }
            repository.addDish(
                name = name,
                shopName = kitchen.name,
                kitchenId = kitchen.id,
                category = category,
                priceInr = priceInr,
                prepMinutes = prepMinutes,
                description = description,
                tag = tag,
                customImageUri = savedPath
            )
            showToast("Added $name to ${kitchen.name}")
        }
    }

    // Worker Operations
    fun saveWorkerPhone(phone: String) {
        viewModelScope.launch {
            repository.saveWorkerPhone(phone.trim())
            showToast("Rider mobile saved: ${phone.trim()}")
        }
    }

    fun setWorkerNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setWorkerNotificationsEnabled(enabled)
        }
    }

    fun ringWorkerTestAlert() {
        AlertNotifier.sendWorkerDispatchNotification(
            context = getApplication(),
            orderId = "#WW-TEST",
            totalInr = 349,
            address = "Connaught Place Outer Circle",
            playChime = true
        )
        showToast("Test dispatch alert triggered")
    }

    fun markWorkerNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllWorkerNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun clearWorkerNotifications() {
        viewModelScope.launch { repository.clearWorkerNotifications() }
    }

    // Owner Operations
    fun addKitchen(name: String, cuisine: String, prepRange: String, address: String) {
        viewModelScope.launch {
            repository.addKitchen(name, cuisine, prepRange, address)
            showToast("Onboarded kitchen: $name")
        }
    }

    fun toggleKitchenActive(kitchenId: String, active: Boolean) {
        viewModelScope.launch { repository.toggleKitchenActive(kitchenId, active) }
    }

    fun createPortalCredential(
        role: PortalRole,
        portalKey: String,
        username: String,
        password: String,
        label: String
    ) {
        viewModelScope.launch {
            repository.createPortalCredential(role, portalKey, username, password, label)
            showToast("Issued ${role.label} key $portalKey")
        }
    }

    fun revokePortalCredential(id: String) {
        viewModelScope.launch {
            repository.revokePortalCredential(id)
            showToast("Revoked portal credential")
        }
    }

    fun createCoupon(
        code: String,
        description: String,
        discountType: DiscountType,
        value: Int,
        minOrderInr: Int,
        maxDiscountInr: Int
    ) {
        viewModelScope.launch {
            repository.createCoupon(code, description, discountType, value, minOrderInr, maxDiscountInr)
            showToast("Coupon ${code.trim().uppercase()} created")
        }
    }

    fun toggleCouponActive(code: String, active: Boolean) {
        viewModelScope.launch { repository.toggleCouponActive(code, active) }
    }

    fun deleteCoupon(code: String) {
        viewModelScope.launch {
            repository.deleteCoupon(code)
            showToast("Deleted coupon $code")
        }
    }

    fun updateCustomQrImage(uri: Uri?, upiLabel: String) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val currentUri = uiState.value.preferences.customQrUri
            val finalPath = if (uri != null) {
                QrCodeMatrix.copyUriToInternalFile(context, uri, "payment_qr") ?: currentUri
            } else {
                currentUri
            }
            repository.savePaymentQrSettings(finalPath, upiLabel.trim().ifBlank { "wingwing.pay@okaxis" })
            showToast("Payment QR settings updated")
        }
    }

    fun resetQrToDefault(upiLabel: String) {
        viewModelScope.launch {
            repository.savePaymentQrSettings("", upiLabel.trim().ifBlank { "wingwing.pay@okaxis" })
            showToast("Restored built-in WingWing UPI QR")
        }
    }
}
