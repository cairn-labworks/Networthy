package com.cairnlabworks.mywealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiabilityDao {

    @Query("SELECT * FROM liabilities WHERE portfolioId = :portfolioId ORDER BY updatedAt DESC")
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
}
