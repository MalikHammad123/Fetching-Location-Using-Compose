package com.example.locationfetching

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.locationfetching.interfaces.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
class LocationFetcher(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationProvider {
    override suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    continuation.resumeWithException(PermissionDeniedException())
                    return@suspendCancellableCoroutine
                }

                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location) {}
                    } else {
                        continuation.resumeWithException(LocationNotFoundException("Current location not found"))
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } else {
                continuation.resumeWithException(LocationServicesNotEnabledException())
            }
        }

    override suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    continuation.resumeWithException(PermissionDeniedException())
                    return@suspendCancellableCoroutine
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location) {}
                    } else {
                        continuation.resumeWithException(LocationNotFoundException("Last location not found"))
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } else {
                continuation.resumeWithException(LocationServicesNotEnabledException())
            }
        }

    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Log.d(TAG, "isLocationEnabled: $isEnabled")
        return isEnabled
    }

    class PermissionDeniedException : Exception("Permission denied")
    class LocationServicesNotEnabledException : Exception("Location services not enabled")
    class LocationNotFoundException(message: String) : Exception(message)
}
