package com.cairnlabworks.mywealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiabilityDao {

    @Query("SELECT * FROM liabilities WHERE portfolioId = :portfolioId ORDER BY position ASC, updatedAt DESC")
    fun observeForPortfolio(portfolioId: Long): Flow<List<LiabilityEntity>>

    @Query("SELECT * FROM liabilities WHERE id = :id")
    fun observeById(id: Long): Flow<LiabilityEntity?>

    @Query("SELECT * FROM liabilities WHERE id = :id")
    suspend fun getById(id: Long): LiabilityEntity?

    @Query("SELECT * FROM liabilities WHERE portfolioId = :portfolioId")
    suspend fun getForPortfolio(portfolioId: Long): List<LiabilityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(liability: LiabilityEntity): Long

    @Update
    suspend fun update(liability: LiabilityEntity)

    @Query("DELETE FROM liabilities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(MAX(position), -1) FROM liabilities WHERE portfolioId = :portfolioId AND type = :type")
    suspend fun maxPosition(portfolioId: Long, type: com.cairnlabworks.mywealth.domain.model.LiabilityType): Int

    @Query("UPDATE liabilities SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    /** Persists a new order for the given liability ids (index becomes position). */
    @Transaction
    suspend fun updatePositions(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updatePosition(id, index) }
    }
}
