package com.example.gmls.di

import android.content.Context
import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.data.remote.LocationService
import com.example.gmls.domain.model.DisasterFirebaseMapper
import com.example.gmls.domain.model.UserFirebaseMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides application context
     */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    /**
     * Provides the Firebase service
     */
    @Provides
    @Singleton
    fun provideFirebaseService(disasterMapper: DisasterFirebaseMapper): FirebaseService {
        return FirebaseService(disasterMapper)
    }

    /**
     * Provides the Location service
     */
    @Provides
    @Singleton
    fun provideLocationService(@ApplicationContext context: Context): LocationService {
        return LocationService(context)
    }

    /**
     * Provides the User Firebase Mapper
     */
    @Provides
    @Singleton
    fun provideUserFirebaseMapper(): UserFirebaseMapper {
        return UserFirebaseMapper()
    }

    /**
     * Provides the Disaster Firebase Mapper
     */
    @Provides
    @Singleton
    fun provideDisasterFirebaseMapper(): DisasterFirebaseMapper {
        return DisasterFirebaseMapper()
    }

    // Removed the duplicate provideDisasterRepository method
}