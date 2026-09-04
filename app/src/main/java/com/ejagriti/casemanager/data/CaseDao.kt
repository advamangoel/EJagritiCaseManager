package com.ejagriti.casemanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(caseEntities: List<CaseEntity>)

    @Update
    suspend fun updateCase(caseEntity: CaseEntity)

    @Query("SELECT * FROM cases ORDER BY createdAt DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query(
        """
        SELECT * FROM cases
        WHERE litigationId LIKE '%' || :query || '%'
           OR newCaseNumber LIKE '%' || :query || '%'
           OR oldCaseNumber LIKE '%' || :query || '%'
           OR partyName LIKE '%' || :query || '%'
           OR oppositeParty LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun searchCases(query: String): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :id LIMIT 1")
    suspend fun getCaseById(id: Long): CaseEntity?

    @Query(
        """
        SELECT * FROM cases
        WHERE (:litigationId != '' AND lower(trim(litigationId)) = lower(trim(:litigationId)))
           OR (:newCaseNumber != '' AND lower(trim(newCaseNumber)) = lower(trim(:newCaseNumber)))
           OR (:oldCaseNumber != '' AND lower(trim(oldCaseNumber)) = lower(trim(:oldCaseNumber)))
        ORDER BY id ASC
        """
    )
    suspend fun findPotentialDuplicates(
        litigationId: String,
        newCaseNumber: String,
        oldCaseNumber: String
    ): List<CaseEntity>

    @Query("DELETE FROM cases WHERE id = :id")
    suspend fun deleteCase(id: Long)

    @Query("SELECT COUNT(*) FROM cases")
    fun getCaseCount(): Flow<Int>
}