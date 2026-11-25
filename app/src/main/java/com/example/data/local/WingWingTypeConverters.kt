package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.OrderItemSnapshot
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WingWingTypeConverters {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val itemListType = Types.newParameterizedType(List::class.java, OrderItemSnapshot::class.java)
    private val adapter = moshi.adapter<List<OrderItemSnapshot>>(itemListType)

    @TypeConverter
    fun fromOrderItems(items: List<OrderItemSnapshot>?): String {
        return adapter.toJson(items ?: emptyList())
    }

    @TypeConverter
    fun toOrderItems(json: String?): List<OrderItemSnapshot> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }
}
