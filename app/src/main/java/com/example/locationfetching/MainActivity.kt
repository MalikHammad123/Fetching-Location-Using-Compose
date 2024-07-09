package com.example.locationfetching

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.locationfetching.ui.theme.LocationFetchingTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

private const val TAG = "LocationFetchingDebug"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationFetchingTheme {
                LastLocationButton()
            }
        }
    }
}

@Composable
fun LastLocationButton() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasLocationPermission = isGranted
                Log.d(TAG, "Permission result: granted=$isGranted")
                if (!isGranted) {
                    // Permission denied logic
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity, android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) && !ActivityCompat.shouldShowRequestPermissionRationale(
                            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) {
                        Log.d(TAG, "Permission denied permanently, prompting to enable location")
                        promptEnableLocation(context)
                    }
                }
            })

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            // Check permission before accessing location
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                hasLocationPermission = true
                Log.d(TAG, "Permission already granted")
            } else {
                // Launch permission request if not granted
                Log.d(TAG, "Requesting location permission")
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }

            // If permission is granted, proceed to fetch location
            if (hasLocationPermission) {
                Log.d(TAG, "Permission granted, checking location services")
                if (isLocationEnabled(context)) {
                    Log.d(TAG, "Location services enabled, fetching last location")
                    context.getLastLocation(fusedLocationClient) { location ->
                        lastLocation = location
                        Log.d(TAG, "Last location fetched: $location")
                    }
                } else {
                    Log.d(TAG, "Location services not enabled, prompting to enable location")
                    promptEnableLocation(context)
                }
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

fun Context.getLastLocation(
    fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (Location?) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        onLocationReceived(location)
        Log.d(TAG, "onLocationReceived: $location")
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

fun promptEnableLocation(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
    Log.d(TAG, "Prompting user to enable location settings")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LastLocationButton()
}
