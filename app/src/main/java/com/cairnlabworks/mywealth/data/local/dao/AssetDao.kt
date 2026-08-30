package com.cairnlabworks.mywealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Query("SELECT * FROM assets WHERE portfolioId = :portfolioId ORDER BY updatedAt DESC")
    fun observeForPortfolio(portfolioId: Long): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    fun observeById(id: Long): Flow<AssetEntity?>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getById(id: Long): AssetEntity?

    @Query("SELECT * FROM assets WHERE portfolioId = :portfolioId")
    suspend fun getForPortfolio(portfolioId: Long): List<AssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity): Long

    @Update
    suspend fun update(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE assets SET lastPrice = :price, lastPriceTimestamp = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun updatePrice(id: Long, price: Double, timestamp: Long)
}
