package com.example.gmls.data.local

import androidx.room.*

@Dao
interface DisasterDao {
    @Query("SELECT * FROM disasters ORDER BY timestamp DESC")
    suspend fun getAllDisasters(): List<DisasterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisasters(disasters: List<DisasterEntity>)

    @Query("DELETE FROM disasters")
    suspend fun clearDisasters()
} 
