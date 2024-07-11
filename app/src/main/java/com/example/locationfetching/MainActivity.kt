package com.example.locationfetching

import android.location.Location
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.locationfetching.interfaces.LocationPermissionChecker
import com.example.locationfetching.interfaces.LocationProvider
import com.example.locationfetching.ui.theme.LocationFetchingTheme
import com.example.locationfetching.viewModel.LocationViewModel
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "LocationFetchingDebug"


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var locationPermissionChecker: LocationPermissionChecker
    @Inject
    lateinit var locationFetcher: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationFetchingTheme {
                LastLocationButton(
                    locationPermissionChecker,
                    locationFetcher,
                )
            }
        }
    }
}




@Composable
fun LastLocationButton(
    locationPermissionChecker: LocationPermissionChecker,
    locationFetcher: LocationProvider,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val locationPermissionState by viewModel.locationPermissionState.collectAsState()
    val gpsEnabledState by viewModel.gpsEnabledState.collectAsState()
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.checkLocationPermission()
        if (isGranted) {
            fetchLocation(coroutineScope, locationFetcher, onSuccess = { location ->
                lastLocation = location
                Log.d(TAG, "Location fetched: $location")
            })
        } else {
            Log.d(TAG, "Permission denied by user")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            viewModel.checkLocationPermission()
            viewModel.checkGPSEnabled()
            if (locationPermissionState) {
                fetchLocation(coroutineScope, locationFetcher, onSuccess = { location ->
                    lastLocation = location
                    Log.d(TAG, "Location fetched: $location")
                })
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }) {
            Text(text = "Get Location")
        }

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

        Text(
            text = "Location Permission: ${if (locationPermissionState) "Granted" else "Denied"}",
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "GPS Enabled: ${if (gpsEnabledState) "Yes" else "No"}",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

private fun fetchLocation(
    coroutineScope: CoroutineScope,
    locationFetcher: LocationProvider,
    onSuccess: (Location?) -> Unit
) {
    coroutineScope.launch {
        try {
            val location = try {
                locationFetcher.getCurrentLocation()
            } catch (e: LocationFetcher.LocationNotFoundException) {
                locationFetcher.getLastLocation()
            }
            onSuccess(location)
        } catch (e: LocationFetcher.PermissionDeniedException) {
            Log.d(TAG, "Permission denied")
        } catch (e: LocationFetcher.LocationServicesNotEnabledException) {
            Log.d(TAG, "Location services not enabled")
        }
    }
}




