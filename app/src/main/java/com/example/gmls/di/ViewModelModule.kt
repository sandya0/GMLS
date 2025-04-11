package com.example.gmls.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Dagger Hilt module for providing ViewModel-scoped dependencies
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    // This module is currently empty because we're using constructor injection for ViewModels
    // If we need any specialized ViewModel-scoped dependencies, they would go here
}