package com.cairnlabworks.mywealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    @Query("SELECT * FROM portfolios ORDER BY isDefault DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolios WHERE id = :id")
    fun observeById(id: Long): Flow<PortfolioEntity?>

    @Query("SELECT * FROM portfolios WHERE id = :id")
    suspend fun getById(id: Long): PortfolioEntity?

    @Query("SELECT * FROM portfolios ORDER BY isDefault DESC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<PortfolioEntity>

    @Query("SELECT * FROM portfolios WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): PortfolioEntity?

    @Query("SELECT COUNT(*) FROM portfolios")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portfolio: PortfolioEntity): Long

    @Update
    suspend fun update(portfolio: PortfolioEntity)

    @Query("DELETE FROM portfolios WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE portfolios SET isDefault = 0")
    suspend fun clearDefault()

    @Query("UPDATE portfolios SET isDefault = 1 WHERE id = :id")
    suspend fun markDefault(id: Long)

    /** Atomically make [id] the one and only default portfolio. */
    @Transaction
    suspend fun setDefault(id: Long) {
        clearDefault()
        markDefault(id)
    }
}
