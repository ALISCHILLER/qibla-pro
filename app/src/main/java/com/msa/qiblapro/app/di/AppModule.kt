package com.msa.qiblapro.app.di

import android.content.Context
import android.hardware.SensorManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.msa.qiblapro.data.compass.CompassRepository
import com.msa.qiblapro.data.location.LocationRepository
import com.msa.qiblapro.data.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideFused(@ApplicationContext ctx: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(ctx)

    @Provides @Singleton
    fun provideSensorManager(@ApplicationContext ctx: Context): SensorManager =
        ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides @Singleton
    fun provideSettingsRepo(@ApplicationContext ctx: Context) = SettingsRepository(ctx)

    @Provides @Singleton
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
}
