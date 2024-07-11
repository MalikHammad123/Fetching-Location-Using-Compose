package com.example.locationfetching.interfaces

import android.location.Location

/**
 *  Abstraction for providing location and checking location permission status
 *  [link](http://slate.nu.edu.pk/portal/site/91f9ddb7-e6d5-4f3b-9e0f-e2772f17612e/page/96744368-c863-42b1-a6da-acb55c2ebf8c)
 */

interface LocationProvider {
    suspend fun getCurrentLocation(): Location?
    suspend fun getLastLocation(): Location?
    fun isLocationEnabled(): Boolean
}

interface LocationPermissionChecker {
    fun hasLocationPermission(): Boolean
}



