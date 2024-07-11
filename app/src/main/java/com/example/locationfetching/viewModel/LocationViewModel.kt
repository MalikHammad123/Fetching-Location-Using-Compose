package com.example.locationfetching.viewModel

import androidx.lifecycle.ViewModel
import com.example.locationfetching.interfaces.LocationPermissionChecker
import com.example.locationfetching.interfaces.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationPermissionChecker: LocationPermissionChecker,
    private val locationFetcher: LocationProvider
) : ViewModel() {

    private val _locationPermissionState = MutableStateFlow(false)
    val locationPermissionState: StateFlow<Boolean> get() = _locationPermissionState

    private val _gpsEnabledState = MutableStateFlow(false)
    val gpsEnabledState: StateFlow<Boolean> get() = _gpsEnabledState

    init {
        _locationPermissionState.value = locationPermissionChecker.hasLocationPermission()
        _gpsEnabledState.value = locationFetcher.isLocationEnabled()
    }

    fun checkLocationPermission() {
        _locationPermissionState.value = locationPermissionChecker.hasLocationPermission()
    }

    fun checkGPSEnabled() {
        _gpsEnabledState.value = locationFetcher.isLocationEnabled()
    }
}
