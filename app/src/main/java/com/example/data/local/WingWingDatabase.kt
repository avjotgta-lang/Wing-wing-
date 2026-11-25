package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.CouponEntity
import com.example.data.model.DishEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderEntity
import com.example.data.model.PortalCredentialEntity
import com.example.data.model.WorkerNotificationEntity

@Database(
    entities = [
        KitchenEntity::class,
        DishEntity::class,
        OrderEntity::class,
        CouponEntity::class,
        FeedbackEntity::class,
        PortalCredentialEntity::class,
        WorkerNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(WingWingTypeConverters::class)
abstract class WingWingDatabase : RoomDatabase() {
    abstract fun wingWingDao(): WingWingDao

    companion object {
        @Volatile
        private var INSTANCE: WingWingDatabase? = null

        fun getInstance(context: Context): WingWingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WingWingDatabase::class.java,
                    "wingwing_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
