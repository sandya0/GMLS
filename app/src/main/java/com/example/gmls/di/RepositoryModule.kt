package com.example.gmls.di

import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.data.remote.LocationService
import com.example.gmls.data.repository.AdminRepositoryImpl
import com.example.gmls.data.repository.DisasterRepositoryImpl
import com.example.gmls.data.repository.UserRepositoryImpl
import com.example.gmls.data.mapper.UserFirebaseMapper
import com.example.gmls.domain.repository.AdminRepository
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides the User Repository implementation
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseService: FirebaseService,
        userMapper: UserFirebaseMapper
    ): UserRepository {
        return UserRepositoryImpl(firebaseService, userMapper)
    }

    /**
     * Provides the Disaster Repository implementation
     */
    @Provides
    @Singleton
    fun provideDisasterRepository(
        firebaseService: FirebaseService,
        locationService: LocationService
    ): DisasterRepository {
        return DisasterRepositoryImpl(firebaseService, locationService)
    }
    
    /**
     * Provides the Admin Repository implementation
     */
    @Provides
    @Singleton
    fun provideAdminRepository(
        firebaseService: FirebaseService,
        userMapper: UserFirebaseMapper
    ): AdminRepository {
        return AdminRepositoryImpl(firebaseService, userMapper)
    }
}
