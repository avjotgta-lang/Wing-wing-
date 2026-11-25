package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class DetectedLocation(
    val lat: Double,
    val lng: Double,
    val formattedAddress: String
)

object LocationHelper {
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): Result<DetectedLocation> {
        if (!hasLocationPermission(context)) {
            return Result.failure(IllegalStateException("Location permission not granted"))
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val rawLocation = suspendCancellableCoroutine<Location?> { cont ->
            val tokenSource = CancellationTokenSource()
            cont.invokeOnCancellation { tokenSource.cancel() }

            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .addOnSuccessListener { current ->
                    if (current != null) {
                        if (cont.isActive) cont.resume(current)
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (cont.isActive) cont.resume(last)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }
        val location = rawLocation ?: return Result.failure(
            IllegalStateException("Unable to read GPS coordinates right now. Move pin on map or type address.")
        )

        val addressText = reverseGeocode(context, location.latitude, location.longitude)
        return Result.success(
            DetectedLocation(
                lat = location.latitude,
                lng = location.longitude,
                formattedAddress = addressText
            )
        )
    }

    @Suppress("DEPRECATION")
    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocation(lat, lng, 1)
                val first = results?.firstOrNull()
                if (first != null) {
                    listOfNotNull(
                        first.featureName,
                        first.thoroughfare,
                        first.subLocality,
                        first.locality
                    ).distinct().joinToString(", ").ifBlank {
                        "Pinned Location (${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)})"
                    }
                } else {
                    "Pinned Location (${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)})"
                }
            }.getOrDefault(
                "Pinned Location (${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)})"
            )
        }
    }

    fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).coerceAtLeast(0.4)
    }

    fun estimateEtaMinutes(distanceKm: Double, prepMinutes: Int = 12): Int {
        val travelMinutes = (distanceKm * 4.5).roundToInt().coerceIn(5, 45)
        return prepMinutes + travelMinutes
    }

    val presetLandmarks = listOf(
        DetectedLocation(28.6315, 77.2167, "Connaught Place Block B, New Delhi"),
        DetectedLocation(28.6280, 77.2190, "Janpath Central Plaza, New Delhi"),
        DetectedLocation(28.6353, 77.2250, "Barakhamba Metro Gate 2, New Delhi"),
        DetectedLocation(28.6229, 77.2090, "Parliament Street Enclave, New Delhi"),
        DetectedLocation(28.6392, 77.2145, "Paharganj Main Bazaar Crossing, New Delhi")
    )
}
