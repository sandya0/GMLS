package com.example.gmls.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DisasterEntity::class, 
        UserEntity::class, 
        HouseholdMemberEntity::class
    ], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun disasterDao(): DisasterDao
    abstract fun userDao(): UserDao
} 
