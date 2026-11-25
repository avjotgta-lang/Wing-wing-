package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppPreferences
import com.example.data.model.PortalRole
import com.example.data.model.RememberedPortalCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wingWingDataStore: DataStore<Preferences> by preferencesDataStore(name = "wingwing_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val REMEMBER_CUSTOMER = booleanPreferencesKey("remember_customer")
        val SAVED_CUSTOMER_EMAIL = stringPreferencesKey("saved_customer_email")
        val SAVED_CUSTOMER_PASSWORD = stringPreferencesKey("saved_customer_password")
        val CURRENT_CUSTOMER_EMAIL = stringPreferencesKey("current_customer_email")
        val CURRENT_CUSTOMER_NAME = stringPreferencesKey("current_customer_name")
        val IS_GUEST_SESSION = booleanPreferencesKey("is_guest_session")
        val SAVED_CUSTOMER_PHONE = stringPreferencesKey("saved_customer_phone")
        val SAVED_DELIVERY_ADDRESS = stringPreferencesKey("saved_delivery_address")
        val SAVED_DELIVERY_LAT = doublePreferencesKey("saved_delivery_lat")
        val SAVED_DELIVERY_LNG = doublePreferencesKey("saved_delivery_lng")
        val WORKER_PHONE = stringPreferencesKey("worker_phone")
        val WORKER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("worker_notifications_enabled")
        val CUSTOM_QR_URI = stringPreferencesKey("custom_qr_uri")
        val PAYMENT_UPI_LABEL = stringPreferencesKey("payment_upi_label")

        fun portalKey(role: PortalRole) = stringPreferencesKey("portal_${role.name}_key")
        fun portalUser(role: PortalRole) = stringPreferencesKey("portal_${role.name}_user")
        fun portalPass(role: PortalRole) = stringPreferencesKey("portal_${role.name}_pass")
    }

    val preferences: Flow<AppPreferences> = context.wingWingDataStore.data.map { prefs ->
        val rememberedPortals = PortalRole.entries.associateWith { role ->
            RememberedPortalCredential(
                portalKey = prefs[Keys.portalKey(role)] ?: "",
                username = prefs[Keys.portalUser(role)] ?: "",
                password = prefs[Keys.portalPass(role)] ?: ""
            )
        }.filterValues { it.portalKey.isNotBlank() && it.username.isNotBlank() }

        AppPreferences(
            isDarkTheme = prefs[Keys.DARK_THEME] ?: false,
            rememberCustomer = prefs[Keys.REMEMBER_CUSTOMER] ?: false,
            savedCustomerEmail = prefs[Keys.SAVED_CUSTOMER_EMAIL] ?: "",
            savedCustomerPassword = prefs[Keys.SAVED_CUSTOMER_PASSWORD] ?: "",
            currentCustomerEmail = prefs[Keys.CURRENT_CUSTOMER_EMAIL] ?: "",
            currentCustomerName = prefs[Keys.CURRENT_CUSTOMER_NAME] ?: "",
            isGuestSession = prefs[Keys.IS_GUEST_SESSION] ?: false,
            savedCustomerPhone = prefs[Keys.SAVED_CUSTOMER_PHONE] ?: "",
            savedDeliveryAddress = prefs[Keys.SAVED_DELIVERY_ADDRESS] ?: "Connaught Place Block B, New Delhi",
            savedDeliveryLat = prefs[Keys.SAVED_DELIVERY_LAT] ?: 28.6315,
            savedDeliveryLng = prefs[Keys.SAVED_DELIVERY_LNG] ?: 77.2167,
            workerPhone = prefs[Keys.WORKER_PHONE] ?: "+91 98104 55210",
            workerNotificationsEnabled = prefs[Keys.WORKER_NOTIFICATIONS_ENABLED] ?: true,
            customQrUri = prefs[Keys.CUSTOM_QR_URI] ?: "",
            paymentUpiLabel = prefs[Keys.PAYMENT_UPI_LABEL] ?: "wingwing.pay@okaxis",
            rememberedPortals = rememberedPortals
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.wingWingDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun saveCustomerSession(
        email: String,
        name: String,
        password: String,
        rememberMe: Boolean,
        isGuest: Boolean
    ) {
        context.wingWingDataStore.edit { prefs ->
            prefs[Keys.CURRENT_CUSTOMER_EMAIL] = email
            prefs[Keys.CURRENT_CUSTOMER_NAME] = name
            prefs[Keys.IS_GUEST_SESSION] = isGuest
            prefs[Keys.REMEMBER_CUSTOMER] = rememberMe
            if (rememberMe && !isGuest) {
                prefs[Keys.SAVED_CUSTOMER_EMAIL] = email
                prefs[Keys.SAVED_CUSTOMER_PASSWORD] = password
            } else if (!rememberMe) {
                prefs.remove(Keys.SAVED_CUSTOMER_EMAIL)
                prefs.remove(Keys.SAVED_CUSTOMER_PASSWORD)
            }
        }
    }

    suspend fun clearCustomerSession() {
        context.wingWingDataStore.edit { prefs ->
            prefs.remove(Keys.CURRENT_CUSTOMER_EMAIL)
            prefs.remove(Keys.CURRENT_CUSTOMER_NAME)
            prefs[Keys.IS_GUEST_SESSION] = false
        }
    }

    suspend fun forgetSavedCustomerLogin() {
        context.wingWingDataStore.edit { prefs ->
            prefs[Keys.REMEMBER_CUSTOMER] = false
            prefs.remove(Keys.SAVED_CUSTOMER_EMAIL)
            prefs.remove(Keys.SAVED_CUSTOMER_PASSWORD)
        }
    }

    suspend fun saveDeliveryProfile(phone: String, address: String, lat: Double, lng: Double) {
        context.wingWingDataStore.edit { prefs ->
            prefs[Keys.SAVED_CUSTOMER_PHONE] = phone
            prefs[Keys.SAVED_DELIVERY_ADDRESS] = address
            prefs[Keys.SAVED_DELIVERY_LAT] = lat
            prefs[Keys.SAVED_DELIVERY_LNG] = lng
        }
    }

    suspend fun saveWorkerPhone(phone: String) {
        context.wingWingDataStore.edit { it[Keys.WORKER_PHONE] = phone }
    }

    suspend fun setWorkerNotificationsEnabled(enabled: Boolean) {
        context.wingWingDataStore.edit { it[Keys.WORKER_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun savePaymentQrSettings(customQrUri: String, upiLabel: String) {
        context.wingWingDataStore.edit { prefs ->
            prefs[Keys.CUSTOM_QR_URI] = customQrUri
            prefs[Keys.PAYMENT_UPI_LABEL] = upiLabel
        }
    }

    suspend fun savePortalRememberedLogin(role: PortalRole, key: String, user: String, pass: String) {
        context.wingWingDataStore.edit { prefs ->
            prefs[Keys.portalKey(role)] = key
            prefs[Keys.portalUser(role)] = user
            prefs[Keys.portalPass(role)] = pass
        }
    }

    suspend fun forgetPortalLogin(role: PortalRole) {
        context.wingWingDataStore.edit { prefs ->
            prefs.remove(Keys.portalKey(role))
            prefs.remove(Keys.portalUser(role))
            prefs.remove(Keys.portalPass(role))
        }
    }
}
