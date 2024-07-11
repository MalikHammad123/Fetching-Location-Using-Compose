package com.example.locationfetching.module

import android.content.Context
import com.example.locationfetching.LocationFetcher
import com.example.locationfetching.LocationPermissionCheckerImpl
import com.example.locationfetching.interfaces.LocationPermissionChecker
import com.example.locationfetching.interfaces.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    fun provideLocationPermissionChecker(@ApplicationContext context: Context): LocationPermissionChecker {
        return LocationPermissionCheckerImpl(context)
    }

    @Provides
    fun provideLocationProvider(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient
    ): LocationProvider {
        return LocationFetcher(context, fusedLocationProviderClient)
    }
}

