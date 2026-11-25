package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class DishCategory(val label: String) {
    ALL("All"),
    BURGERS_SIDES("Burgers & Sides"),
    PIZZA_WRAPS("Pizza & Wraps"),
    SOUTH_INDIAN("South Indian"),
    DRINKS("Drinks")
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    NEW("New", 0),
    PREPARING("Preparing", 1),
    READY("Ready", 2),
    PICKED_UP("Picked Up", 3),
    DELIVERED("Delivered", 4)
}

enum class PaymentMethod(val label: String) {
    QR_UPI("QR Code / UPI"),
    CASH_ON_DELIVERY("Cash on Delivery")
}

enum class PortalRole(val label: String) {
    RESTAURANT("Restaurant"),
    WORKER("Worker"),
    OWNER("Owner")
}

enum class DiscountType(val label: String) {
    FLAT("Flat ₹"),
    PERCENT("% Off")
}

enum class StorefrontTab(val label: String) {
    MENU("Menu"),
    LIVE_ORDERS("Live Orders"),
    ORDER_HISTORY("Order History"),
    FEEDBACK("Feedback")
}

@Entity(tableName = "kitchens")
data class KitchenEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cuisineTag: String,
    val prepTimeRange: String,
    val rating: Double,
    val isActive: Boolean,
    val lat: Double,
    val lng: Double,
    val address: String
)

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shopName: String,
    val kitchenId: String,
    val category: String,
    val priceInr: Int,
    val prepMinutes: Int,
    val description: String,
    val tag: String,
    val imageKey: String,
    val customImageUri: String? = null,
    val isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class OrderItemSnapshot(
    val dishId: String,
    val name: String,
    val shopName: String,
    val priceInr: Int,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryLat: Double,
    val deliveryLng: Double,
    val items: List<OrderItemSnapshot>,
    val subtotalInr: Int,
    val discountInr: Int,
    val couponCode: String?,
    val totalInr: Int,
    val paymentMethod: String,
    val status: String,
    val createdAtEpochMs: Long,
    val riderPhone: String? = null,
    val riderName: String? = null,
    val rating: Int? = null,
    val feedbackComment: String? = null
) {
    val statusEnum: OrderStatus
        get() = OrderStatus.entries.firstOrNull { it.name == status } ?: OrderStatus.NEW

    val paymentMethodEnum: PaymentMethod
        get() = PaymentMethod.entries.firstOrNull { it.name == paymentMethod } ?: PaymentMethod.QR_UPI
}

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val description: String,
    val discountType: String,
    val value: Int,
    val minOrderInr: Int,
    val maxDiscountInr: Int,
    val isActive: Boolean,
    val createdAtEpochMs: Long
) {
    val typeEnum: DiscountType
        get() = DiscountType.entries.firstOrNull { it.name == discountType } ?: DiscountType.FLAT
}

@Entity(tableName = "feedbacks")
data class FeedbackEntity(
    @PrimaryKey val id: String,
    val orderId: String?,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val createdAtEpochMs: Long
)

@Entity(tableName = "portal_credentials")
data class PortalCredentialEntity(
    @PrimaryKey val id: String,
    val role: String,
    val portalKey: String,
    val username: String,
    val password: String,
    val label: String,
    val isDefault: Boolean,
    val createdAtEpochMs: Long
) {
    val roleEnum: PortalRole
        get() = PortalRole.entries.firstOrNull { it.name == role } ?: PortalRole.RESTAURANT
}

@Entity(tableName = "worker_notifications")
data class WorkerNotificationEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val title: String,
    val message: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val totalInr: Int,
    val isRead: Boolean,
    val createdAtEpochMs: Long
)

data class CartItem(
    val dish: DishEntity,
    val quantity: Int
)

data class RememberedPortalCredential(
    val portalKey: String = "",
    val username: String = "",
    val password: String = ""
)

data class AppPreferences(
    val isDarkTheme: Boolean = false,
    val rememberCustomer: Boolean = false,
    val savedCustomerEmail: String = "",
    val savedCustomerPassword: String = "",
    val currentCustomerEmail: String = "",
    val currentCustomerName: String = "",
    val isGuestSession: Boolean = false,
    val savedCustomerPhone: String = "",
    val savedDeliveryAddress: String = "Connaught Place Block B, New Delhi",
    val savedDeliveryLat: Double = 28.6315,
    val savedDeliveryLng: Double = 77.2167,
    val workerPhone: String = "+91 98104 55210",
    val workerNotificationsEnabled: Boolean = true,
    val customQrUri: String = "",
    val paymentUpiLabel: String = "wingwing.pay@okaxis",
    val rememberedPortals: Map<PortalRole, RememberedPortalCredential> = emptyMap()
)
