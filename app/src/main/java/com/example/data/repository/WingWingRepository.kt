package com.example.data.repository

import com.example.data.local.PreferencesRepository
import com.example.data.local.WingWingDao
import com.example.data.model.AppPreferences
import com.example.data.model.CartItem
import com.example.data.model.CouponEntity
import com.example.data.model.DiscountType
import com.example.data.model.DishCategory
import com.example.data.model.DishEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemSnapshot
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PortalCredentialEntity
import com.example.data.model.PortalRole
import com.example.data.model.WorkerNotificationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

class WingWingRepository(
    private val dao: WingWingDao,
    private val prefsRepo: PreferencesRepository
) {
    val kitchens: Flow<List<KitchenEntity>> = dao.observeKitchens()
    val dishes: Flow<List<DishEntity>> = dao.observeDishes()
    val orders: Flow<List<OrderEntity>> = dao.observeOrders()
    val coupons: Flow<List<CouponEntity>> = dao.observeCoupons()
    val feedbacks: Flow<List<FeedbackEntity>> = dao.observeFeedbacks()
    val credentials: Flow<List<PortalCredentialEntity>> = dao.observeCredentials()
    val workerNotifications: Flow<List<WorkerNotificationEntity>> = dao.observeWorkerNotifications()
    val preferences: Flow<AppPreferences> = prefsRepo.preferences

    suspend fun ensureSeeded() {
        if (dao.countKitchens() == 0) {
            dao.insertKitchens(defaultKitchens())
        }
        if (dao.countDishes() == 0) {
            dao.insertDishes(defaultDishes())
        }
        if (dao.countCoupons() == 0) {
            dao.insertCoupons(defaultCoupons())
        }
        if (dao.countCredentials() == 0) {
            dao.insertCredentials(defaultCredentials())
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) = prefsRepo.setDarkTheme(enabled)

    suspend fun saveCustomerSession(
        email: String,
        name: String,
        password: String,
        rememberMe: Boolean,
        isGuest: Boolean
    ) = prefsRepo.saveCustomerSession(email, name, password, rememberMe, isGuest)

    suspend fun clearCustomerSession() = prefsRepo.clearCustomerSession()

    suspend fun forgetSavedCustomerLogin() = prefsRepo.forgetSavedCustomerLogin()

    suspend fun saveWorkerPhone(phone: String) = prefsRepo.saveWorkerPhone(phone)

    suspend fun setWorkerNotificationsEnabled(enabled: Boolean) =
        prefsRepo.setWorkerNotificationsEnabled(enabled)

    suspend fun savePaymentQrSettings(customQrUri: String, upiLabel: String) =
        prefsRepo.savePaymentQrSettings(customQrUri, upiLabel)

    suspend fun verifyPortalLogin(
        role: PortalRole,
        portalKey: String,
        username: String,
        password: String,
        rememberMe: Boolean
    ): PortalCredentialEntity? {
        val cleanKey = portalKey.trim().uppercase()
        val cleanUser = username.trim().uppercase()
        val cleanPass = password.trim()
        val matched = dao.verifyCredential(role.name, cleanKey, cleanUser, cleanPass)
            ?: if (role != PortalRole.OWNER) {
                // Allow Owner master key to unlock or check exact role first
                null
            } else null

        if (matched != null && rememberMe) {
            prefsRepo.savePortalRememberedLogin(role, cleanKey, cleanUser, cleanPass)
        }
        return matched
    }

    suspend fun forgetPortalLogin(role: PortalRole) = prefsRepo.forgetPortalLogin(role)

    fun calculateDiscount(coupon: CouponEntity?, subtotalInr: Int): Pair<Int, String?> {
        if (coupon == null) return 0 to null
        if (!coupon.isActive) return 0 to "Coupon ${coupon.code} is paused."
        if (subtotalInr < coupon.minOrderInr) {
            return 0 to "Minimum order ₹${coupon.minOrderInr} required for ${coupon.code}."
        }
        val rawDiscount = when (coupon.typeEnum) {
            DiscountType.FLAT -> coupon.value
            DiscountType.PERCENT -> ((subtotalInr * coupon.value) / 100.0).roundToInt()
        }
        val capped = if (coupon.maxDiscountInr > 0) min(rawDiscount, coupon.maxDiscountInr) else rawDiscount
        return min(capped, subtotalInr) to null
    }

    suspend fun placeOrder(
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryLat: Double,
        deliveryLng: Double,
        cartItems: List<CartItem>,
        appliedCoupon: CouponEntity?,
        paymentMethod: PaymentMethod
    ): OrderEntity {
        val subtotal = cartItems.sumOf { it.dish.priceInr * it.quantity }
        val (discount, _) = calculateDiscount(appliedCoupon, subtotal)
        val total = (subtotal - discount).coerceAtLeast(0)
        val orderCode = "#WW${Random.nextInt(1000, 9999)}"
        val now = System.currentTimeMillis()

        val snapshots = cartItems.map {
            OrderItemSnapshot(
                dishId = it.dish.id,
                name = it.dish.name,
                shopName = it.dish.shopName,
                priceInr = it.dish.priceInr,
                quantity = it.quantity
            )
        }

        val order = OrderEntity(
            id = orderCode,
            customerName = customerName.ifBlank { "WingWing Guest" },
            customerEmail = customerEmail.ifBlank { "guest@wingwing.app" },
            customerPhone = customerPhone.trim(),
            deliveryAddress = deliveryAddress.trim(),
            deliveryLat = deliveryLat,
            deliveryLng = deliveryLng,
            items = snapshots,
            subtotalInr = subtotal,
            discountInr = discount,
            couponCode = if (discount > 0) appliedCoupon?.code else null,
            totalInr = total,
            paymentMethod = paymentMethod.name,
            status = OrderStatus.NEW.name,
            createdAtEpochMs = now
        )
        dao.insertOrder(order)

        prefsRepo.saveDeliveryProfile(
            phone = customerPhone.trim(),
            address = deliveryAddress.trim(),
            lat = deliveryLat,
            lng = deliveryLng
        )

        val primaryShop = snapshots.firstOrNull()?.shopName ?: "WingWing Kitchen"
        val itemCount = snapshots.sumOf { it.quantity }
        val notification = WorkerNotificationEntity(
            id = "notif_${UUID.randomUUID()}",
            orderId = order.id,
            title = "New Order ${order.id} • ₹${order.totalInr}",
            message = "$itemCount item(s) from $primaryShop → ${order.deliveryAddress}",
            customerPhone = order.customerPhone,
            deliveryAddress = order.deliveryAddress,
            totalInr = order.totalInr,
            isRead = false,
            createdAtEpochMs = now
        )
        dao.insertWorkerNotification(notification)
        return order
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus, riderPhone: String? = null) {
        if (newStatus == OrderStatus.PICKED_UP && !riderPhone.isNullOrBlank()) {
            dao.assignRiderAndStatus(
                orderId = orderId,
                status = newStatus.name,
                riderPhone = riderPhone.trim(),
                riderName = "WingWing Rider"
            )
        } else {
            dao.updateOrderStatus(orderId, newStatus.name)
        }
    }

    suspend fun submitFeedback(
        orderId: String?,
        customerName: String,
        rating: Int,
        comment: String
    ) {
        val cleanComment = comment.trim()
        val entry = FeedbackEntity(
            id = "fb_${UUID.randomUUID()}",
            orderId = orderId,
            customerName = customerName.ifBlank { "WingWing Customer" },
            rating = rating.coerceIn(1, 5),
            comment = cleanComment,
            createdAtEpochMs = System.currentTimeMillis()
        )
        dao.insertFeedback(entry)
        if (!orderId.isNullOrBlank()) {
            dao.attachOrderFeedback(orderId, rating.coerceIn(1, 5), cleanComment)
        }
    }

    suspend fun addDish(
        name: String,
        shopName: String,
        kitchenId: String,
        category: DishCategory,
        priceInr: Int,
        prepMinutes: Int,
        description: String,
        tag: String,
        customImageUri: String?
    ) {
        val imageKey = when (category) {
            DishCategory.BURGERS_SIDES -> "burger"
            DishCategory.PIZZA_WRAPS -> "pizza"
            DishCategory.SOUTH_INDIAN -> "dosa"
            DishCategory.DRINKS -> "drinks"
            DishCategory.ALL -> "hero"
        }
        dao.insertDish(
            DishEntity(
                id = "dish_${UUID.randomUUID()}",
                name = name.trim(),
                shopName = shopName.trim(),
                kitchenId = kitchenId,
                category = category.label,
                priceInr = priceInr.coerceAtLeast(1),
                prepMinutes = prepMinutes.coerceIn(5, 90),
                description = description.trim(),
                tag = tag.trim().ifBlank { "Chef Special" },
                imageKey = imageKey,
                customImageUri = customImageUri,
                isAvailable = true
            )
        )
    }

    suspend fun toggleDishAvailability(dishId: String, isAvailable: Boolean) {
        dao.setDishAvailable(dishId, isAvailable)
    }

    suspend fun addKitchen(
        name: String,
        cuisineTag: String,
        prepTimeRange: String,
        address: String
    ) {
        dao.insertKitchen(
            KitchenEntity(
                id = "kitchen_${UUID.randomUUID()}",
                name = name.trim(),
                cuisineTag = cuisineTag.trim().ifBlank { "Multi-Cuisine" },
                prepTimeRange = prepTimeRange.trim().ifBlank { "20–30 min" },
                rating = 4.7,
                isActive = true,
                lat = 28.6289 + Random.nextDouble(-0.015, 0.015),
                lng = 77.2120 + Random.nextDouble(-0.015, 0.015),
                address = address.trim().ifBlank { "Central Culinary Hub" }
            )
        )
    }

    suspend fun toggleKitchenActive(kitchenId: String, isActive: Boolean) {
        dao.setKitchenActive(kitchenId, isActive)
    }

    suspend fun createCoupon(
        code: String,
        description: String,
        discountType: DiscountType,
        value: Int,
        minOrderInr: Int,
        maxDiscountInr: Int
    ) {
        dao.insertCoupon(
            CouponEntity(
                code = code.trim().uppercase(),
                description = description.trim(),
                discountType = discountType.name,
                value = value.coerceAtLeast(1),
                minOrderInr = minOrderInr.coerceAtLeast(0),
                maxDiscountInr = maxDiscountInr.coerceAtLeast(0),
                isActive = true,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleCouponActive(code: String, isActive: Boolean) {
        dao.setCouponActive(code, isActive)
    }

    suspend fun deleteCoupon(code: String) {
        dao.deleteCoupon(code)
    }

    suspend fun createPortalCredential(
        role: PortalRole,
        portalKey: String,
        username: String,
        password: String,
        label: String
    ) {
        dao.insertCredential(
            PortalCredentialEntity(
                id = "cred_${UUID.randomUUID()}",
                role = role.name,
                portalKey = portalKey.trim().uppercase(),
                username = username.trim().uppercase(),
                password = password.trim(),
                label = label.trim().ifBlank { "${role.label} Access" },
                isDefault = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun revokePortalCredential(id: String) {
        dao.deleteCustomCredential(id)
    }

    suspend fun markNotificationRead(id: String) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()
    suspend fun clearWorkerNotifications() = dao.clearWorkerNotifications()

    private fun defaultKitchens() = listOf(
        KitchenEntity(
            id = "k_flame",
            name = "WingWing Grill House",
            cuisineTag = "Wings & Smash Burgers",
            prepTimeRange = "18–25 min",
            rating = 4.9,
            isActive = true,
            lat = 28.6304,
            lng = 77.2177,
            address = "Outer Circle, Connaught Place"
        ),
        KitchenEntity(
            id = "k_crust",
            name = "Crust & Ember",
            cuisineTag = "Wood-Fired Pizzas & Wraps",
            prepTimeRange = "22–28 min",
            rating = 4.8,
            isActive = true,
            lat = 28.6258,
            lng = 77.2110,
            address = "Janpath Market Lane"
        ),
        KitchenEntity(
            id = "k_dakshin",
            name = "Dakshin Spice Co.",
            cuisineTag = "Authentic South Indian",
            prepTimeRange = "15–20 min",
            rating = 4.9,
            isActive = true,
            lat = 28.6340,
            lng = 77.2215,
            address = "Barakhamba Arcade"
        ),
        KitchenEntity(
            id = "k_brew",
            name = "Zest & Tonic Bar",
            cuisineTag = "Craft Coolers & Cold Brew",
            prepTimeRange = "8–12 min",
            rating = 4.7,
            isActive = true,
            lat = 28.6295,
            lng = 77.2192,
            address = "Minto Road Pavilion"
        )
    )

    private fun defaultDishes() = listOf(
        DishEntity(
            id = "d_1",
            name = "Coral Blaze Smash Burger",
            shopName = "WingWing Grill House",
            kitchenId = "k_flame",
            category = DishCategory.BURGERS_SIDES.label,
            priceInr = 289,
            prepMinutes = 18,
            description = "Double seared patty, aged cheddar, caramelized onions, smoky coral glaze & crinkle fries.",
            tag = "Bestseller",
            imageKey = "burger",
            isAvailable = true
        ),
        DishEntity(
            id = "d_2",
            name = "Crispy Korean Honey Wings (8 pcs)",
            shopName = "WingWing Grill House",
            kitchenId = "k_flame",
            category = DishCategory.BURGERS_SIDES.label,
            priceInr = 349,
            prepMinutes = 20,
            description = "Double-fried crispy wings tossed in sticky gochujang wildflower honey & toasted sesame.",
            tag = "Chef Signature",
            imageKey = "hero",
            isAvailable = true
        ),
        DishEntity(
            id = "d_3",
            name = "Loaded Truffle Parmesan Fries",
            shopName = "WingWing Grill House",
            kitchenId = "k_flame",
            category = DishCategory.BURGERS_SIDES.label,
            priceInr = 179,
            prepMinutes = 12,
            description = "Hand-cut skin-on fries dusted with white truffle oil, aged parmesan & garlic aioli.",
            tag = "Crispy Side",
            imageKey = "burger",
            isAvailable = true
        ),
        DishEntity(
            id = "d_4",
            name = "Chili Honey Pepperoni Flatbread",
            shopName = "Crust & Ember",
            kitchenId = "k_crust",
            category = DishCategory.PIZZA_WRAPS.label,
            priceInr = 419,
            prepMinutes = 24,
            description = "48-hour sourdough crust, charred cup pepperoni, buffalo mozzarella, basil & wild chili honey.",
            tag = "Wood-Fired",
            imageKey = "pizza",
            isAvailable = true
        ),
        DishEntity(
            id = "d_5",
            name = "Smoked Peri-Peri Chicken Wrap",
            shopName = "Crust & Ember",
            kitchenId = "k_crust",
            category = DishCategory.PIZZA_WRAPS.label,
            priceInr = 239,
            prepMinutes = 16,
            description = "Char-grilled peri-peri strips, pickled jalapenos, crisp slaw & garlic toum in toasted lavash.",
            tag = "High Protein",
            imageKey = "pizza",
            isAvailable = true
        ),
        DishEntity(
            id = "d_6",
            name = "Ghee Roast Masala Dosa",
            shopName = "Dakshin Spice Co.",
            kitchenId = "k_dakshin",
            category = DishCategory.SOUTH_INDIAN.label,
            priceInr = 199,
            prepMinutes = 15,
            description = "Paper-thin fermented rice crepe roasted in A2 ghee with spiced potato masala, sambar & two chutneys.",
            tag = "Crispy Classic",
            imageKey = "dosa",
            isAvailable = true
        ),
        DishEntity(
            id = "d_7",
            name = "Mysore Podi Butter Idli Platter",
            shopName = "Dakshin Spice Co.",
            kitchenId = "k_dakshin",
            category = DishCategory.SOUTH_INDIAN.label,
            priceInr = 169,
            prepMinutes = 14,
            description = "Steamed pillowy idlis tossed in gunpowder podi spice, melted butter & curry leaf tempering.",
            tag = "House Favorite",
            imageKey = "dosa",
            isAvailable = true
        ),
        DishEntity(
            id = "d_8",
            name = "Mango Passionfruit Fizz",
            shopName = "Zest & Tonic Bar",
            kitchenId = "k_brew",
            category = DishCategory.DRINKS.label,
            priceInr = 149,
            prepMinutes = 8,
            description = "Alphonso mango puree shaken with tangy passionfruit, sparkling tonic & crushed mint.",
            tag = "Chilled",
            imageKey = "drinks",
            isAvailable = true
        ),
        DishEntity(
            id = "d_9",
            name = "Valencia Orange Cold Brew Tonic",
            shopName = "Zest & Tonic Bar",
            kitchenId = "k_brew",
            category = DishCategory.DRINKS.label,
            priceInr = 159,
            prepMinutes = 8,
            description = "18-hour single-origin cold brew layered over botanical tonic & charred orange zest.",
            tag = "Energizing",
            imageKey = "drinks",
            isAvailable = true
        )
    )

    private fun defaultCoupons() = listOf(
        CouponEntity(
            code = "WING50",
            description = "Flat ₹50 off on orders above ₹249",
            discountType = DiscountType.FLAT.name,
            value = 50,
            minOrderInr = 249,
            maxDiscountInr = 50,
            isActive = true,
            createdAtEpochMs = System.currentTimeMillis()
        ),
        CouponEntity(
            code = "CORAL20",
            description = "20% off up to ₹120 on orders ₹399+",
            discountType = DiscountType.PERCENT.name,
            value = 20,
            minOrderInr = 399,
            maxDiscountInr = 120,
            isActive = true,
            createdAtEpochMs = System.currentTimeMillis() - 1000L
        )
    )

    private fun defaultCredentials() = listOf(
        PortalCredentialEntity(
            id = "cred_owner_default",
            role = PortalRole.OWNER.name,
            portalKey = "XYZ1213",
            username = "HUKAM",
            password = "12345",
            label = "Primary Owner",
            isDefault = true,
            createdAtEpochMs = System.currentTimeMillis()
        ),
        PortalCredentialEntity(
            id = "cred_rest_default",
            role = PortalRole.RESTAURANT.name,
            portalKey = "XYZ1213",
            username = "HUKAM",
            password = "12345",
            label = "Main Kitchen Hub",
            isDefault = true,
            createdAtEpochMs = System.currentTimeMillis() - 1000L
        ),
        PortalCredentialEntity(
            id = "cred_worker_default",
            role = PortalRole.WORKER.name,
            portalKey = "XYZ1213",
            username = "HUKAM",
            password = "12345",
            label = "Primary Fleet Rider",
            isDefault = true,
            createdAtEpochMs = System.currentTimeMillis() - 2000L
        )
    )
}
