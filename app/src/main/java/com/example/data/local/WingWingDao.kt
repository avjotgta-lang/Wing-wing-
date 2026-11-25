package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CouponEntity
import com.example.data.model.DishEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderEntity
import com.example.data.model.PortalCredentialEntity
import com.example.data.model.WorkerNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WingWingDao {
    // Kitchens
    @Query("SELECT * FROM kitchens ORDER BY name ASC")
    fun observeKitchens(): Flow<List<KitchenEntity>>

    @Query("SELECT COUNT(*) FROM kitchens")
    suspend fun countKitchens(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitchen(kitchen: KitchenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitchens(kitchens: List<KitchenEntity>)

    @Query("UPDATE kitchens SET isActive = :isActive WHERE id = :id")
    suspend fun setKitchenActive(id: String, isActive: Boolean)

    // Dishes
    @Query("SELECT * FROM dishes ORDER BY shopName ASC, name ASC")
    fun observeDishes(): Flow<List<DishEntity>>

    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun countDishes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: DishEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(dishes: List<DishEntity>)

    @Query("UPDATE dishes SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setDishAvailable(id: String, isAvailable: Boolean)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAtEpochMs DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET status = :status, riderPhone = :riderPhone, riderName = :riderName WHERE id = :orderId")
    suspend fun assignRiderAndStatus(orderId: String, status: String, riderPhone: String, riderName: String)

    @Query("UPDATE orders SET rating = :rating, feedbackComment = :comment WHERE id = :orderId")
    suspend fun attachOrderFeedback(orderId: String, rating: Int, comment: String)

    // Coupons
    @Query("SELECT * FROM coupons ORDER BY createdAtEpochMs DESC")
    fun observeCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT COUNT(*) FROM coupons")
    suspend fun countCoupons(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Query("UPDATE coupons SET isActive = :isActive WHERE code = :code")
    suspend fun setCouponActive(code: String, isActive: Boolean)

    @Query("DELETE FROM coupons WHERE code = :code")
    suspend fun deleteCoupon(code: String)

    // Feedbacks
    @Query("SELECT * FROM feedbacks ORDER BY createdAtEpochMs DESC")
    fun observeFeedbacks(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)

    // Portal Credentials
    @Query("SELECT * FROM portal_credentials ORDER BY createdAtEpochMs DESC")
    fun observeCredentials(): Flow<List<PortalCredentialEntity>>

    @Query("SELECT COUNT(*) FROM portal_credentials")
    suspend fun countCredentials(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: PortalCredentialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredentials(credentials: List<PortalCredentialEntity>)

    @Query("SELECT * FROM portal_credentials WHERE role = :role AND portalKey = :portalKey AND username = :username AND password = :password LIMIT 1")
    suspend fun verifyCredential(role: String, portalKey: String, username: String, password: String): PortalCredentialEntity?

    @Query("DELETE FROM portal_credentials WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomCredential(id: String)

    // Worker Notifications
    @Query("SELECT * FROM worker_notifications ORDER BY createdAtEpochMs DESC")
    fun observeWorkerNotifications(): Flow<List<WorkerNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkerNotification(notification: WorkerNotificationEntity)

    @Query("UPDATE worker_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE worker_notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM worker_notifications")
    suspend fun clearWorkerNotifications()
}
