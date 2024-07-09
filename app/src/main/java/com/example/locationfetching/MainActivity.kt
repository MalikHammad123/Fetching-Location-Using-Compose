package com.example.locationfetching

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.locationfetching.ui.theme.LocationFetchingTheme

private const val TAG = "LocationFetchingDebug"

class MainActivity : ComponentActivity() {
    private val locationWrapper by lazy { LocationWrapper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationFetchingTheme {
                LastLocationButton(locationWrapper)
            }
        }
    }
}


fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isEnabled =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    Log.d(TAG, "isLocationEnabled: $isEnabled")
    return isEnabled
}

@Composable
fun LastLocationButton(locationWrapper: LocationWrapper) {
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, fetch location
            locationWrapper.fetchLastLocation(
                onSuccess = { location ->
                    lastLocation = location
                    Log.d(TAG, "Last location fetched: $location")
                },
                onPermissionDenied = {
                    // Handle permission denied logic
                    Log.d(TAG, "Permission denied")
                },
                onLocationServicesNotEnabled = {
                    // Handle location services not enabled logic
                    Log.d(TAG, "Location services not enabled")
                }
            )
        } else {
            // Permission denied
            Log.d(TAG, "Permission denied by user")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            // Request location permission
            if (locationWrapper.hasLocationPermission()) {
                // Permission already granted, fetch location
                locationWrapper.fetchLastLocation(
                    onSuccess = { location ->
                        lastLocation = location
                        Log.d(TAG, "Last location fetched: $location")
                    },
                    onPermissionDenied = {
                        // Handle permission denied logic
                        Log.d(TAG, "Permission denied")
                    },
                    onLocationServicesNotEnabled = {
                        // Handle location services not enabled logic
                        Log.d(TAG, "Location services not enabled")
                    }
                )
            } else {
                // Request location permission
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }) {
            Text(text = "Get Last Location")
        }

        // Display last location details if available
        lastLocation?.let {
            Text(
                text = "Latitude: ${it.latitude}, Longitude: ${it.longitude}",
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Bearing: ${it.bearing}", modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Accuracy: ${it.accuracy} meters", modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}