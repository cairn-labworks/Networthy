package com.cairnlabworks.mywealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cairnlabworks.mywealth.data.local.entity.FxRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FxRateDao {

    @Query("SELECT * FROM fx_rates")
    fun observeAll(): Flow<List<FxRateEntity>>

    @Query("SELECT * FROM fx_rates")
    suspend fun getAll(): List<FxRateEntity>

    @Query("SELECT * FROM fx_rates WHERE currency = :currency")
    suspend fun getByCurrency(currency: String): FxRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rates: List<FxRateEntity>)

    @Query("SELECT MAX(timestamp) FROM fx_rates")
    suspend fun latestTimestamp(): Long?
}
