package com.example.locationfetching

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationWrapper(private val activity: Activity) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(activity)
    }

    fun fetchLastLocation(
        onSuccess: (Location?) -> Unit,
        onPermissionDenied: () -> Unit,
        onLocationServicesNotEnabled: () -> Unit
    ) {
        if (hasLocationPermission()) {
            fetchLocation(onSuccess, onLocationServicesNotEnabled)
        } else {
            onPermissionDenied()
        }
    }

    private fun fetchLocation(
        onSuccess: (Location?) -> Unit,
        onLocationServicesNotEnabled: () -> Unit
    ) {
        if (isLocationEnabled(activity)) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                onSuccess(location)
            }
        } else {
            onLocationServicesNotEnabled()
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}