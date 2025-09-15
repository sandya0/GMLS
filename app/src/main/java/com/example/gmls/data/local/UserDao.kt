package com.example.gmls.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()
    
    // Household Members operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholdMembers(members: List<HouseholdMemberEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholdMember(member: HouseholdMemberEntity)
    
    @Update
    suspend fun updateHouseholdMember(member: HouseholdMemberEntity)
    
    @Delete
    suspend fun deleteHouseholdMember(member: HouseholdMemberEntity)
    
    @Query("SELECT * FROM household_members WHERE userId = :userId")
    suspend fun getHouseholdMembersForUser(userId: String): List<HouseholdMemberEntity>
    
    @Query("SELECT * FROM household_members WHERE userId = :userId")
    fun getHouseholdMembersForUserFlow(userId: String): Flow<List<HouseholdMemberEntity>>
    
    @Query("DELETE FROM household_members WHERE userId = :userId")
    suspend fun deleteAllHouseholdMembersForUser(userId: String)
    
    @Query("DELETE FROM household_members WHERE id = :memberId")
    suspend fun deleteHouseholdMemberById(memberId: String)
    
    // Transaction to get user with household members
    @Transaction
    @Query("SELECT * FROM user_profile WHERE id = :userId")
    suspend fun getUserWithHouseholdMembers(userId: String): UserWithHouseholdMembers?
    
    @Transaction
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserWithHouseholdMembersFlow(): Flow<UserWithHouseholdMembers?>
    
    /**
     * Transaction to save user with household members
     */
    @Transaction
    suspend fun saveUserWithHouseholdMembers(user: UserEntity, householdMembers: List<HouseholdMemberEntity>) {
        insertOrUpdateUser(user)
        deleteAllHouseholdMembersForUser(user.id)
        if (householdMembers.isNotEmpty()) {
            insertHouseholdMembers(householdMembers)
        }
    }
} 
