package com.vibe.news.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.*

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCity(): String? {
        return try {
            val location = fusedLocationClient.lastLocation.await() ?: return null
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea
        } catch (e: Exception) {
            null
        }
    }
}
