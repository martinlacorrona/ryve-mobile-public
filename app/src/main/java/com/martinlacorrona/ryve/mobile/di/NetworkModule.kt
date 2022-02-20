package com.martinlacorrona.ryve.mobile.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.martinlacorrona.ryve.mobile.BuildConfig
import com.martinlacorrona.ryve.mobile.rest.*
import com.martinlacorrona.ryve.mobile.rest.util.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(authInterceptor)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.RYVE_API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideFuelTypeService(retrofit: Retrofit): FuelTypeService {
        return retrofit
            .create(FuelTypeService::class.java)
    }

    @Singleton
    @Provides
    fun provideStationServiceService(retrofit: Retrofit): StationServiceService {
        return retrofit
            .create(StationServiceService::class.java)
    }

    @Singleton
    @Provides
    fun provideFavouriteStationServiceService(retrofit: Retrofit): UserFavouriteStationServiceService {
        return retrofit
            .create(UserFavouriteStationServiceService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserPreferenceService(retrofit: Retrofit): UserPreferencesService {
        return retrofit
            .create(UserPreferencesService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit
            .create(UserService::class.java)
    }

    @Singleton
    @Provides
    fun provideNotificationService(retrofit: Retrofit): NotificationService {
        return retrofit
            .create(NotificationService::class.java)
    }

    @Singleton
    @Provides
    fun provideDirectionsService(retrofit: Retrofit): DirectionsService {
        return retrofit
            .create(DirectionsService::class.java)
    }
}