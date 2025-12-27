package com.msa.qiblapro.app.di

import android.content.Context
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.SettingsRepository
import com.msa.qiblapro.data.settings.SettingsV1ToV2Migration
import com.msa.qiblapro.ui.map.MapPerformanceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFused(@ApplicationContext ctx: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(ctx)

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext ctx: Context): SensorManager =
        ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(
                SettingsV1ToV2Migration()
            ),
            produceFile = { ctx.preferencesDataStoreFile("qibla_settings") }
        )

    @Provides
    @Singleton
    fun provideSettingsRepo(dataStore: DataStore<Preferences>) = SettingsRepository(dataStore)

    @Provides
    @Singleton
    fun provideLocationRepo(
        @ApplicationContext ctx: Context,
        fused: FusedLocationProviderClient,
        settings: SettingsRepository
    ) = LocationRepository(ctx, fused, settings)

    @Provides
    @Singleton
    fun provideCompassRepo(
        @ApplicationContext ctx: Context,
        sm: SensorManager
    ): CompassRepository = CompassRepository(ctx, sm)

    @Provides
    @Singleton
    fun provideMapPerformanceManager(@ApplicationContext ctx: Context) = MapPerformanceManager(ctx)
}
