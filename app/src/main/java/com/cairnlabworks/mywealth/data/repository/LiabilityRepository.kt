package com.cairnlabworks.mywealth.data.repository

import com.cairnlabworks.mywealth.data.local.dao.LiabilityDao
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import kotlinx.coroutines.flow.Flow

class LiabilityRepository(private val dao: LiabilityDao) {

    fun observeForPortfolio(portfolioId: Long): Flow<List<LiabilityEntity>> =
        dao.observeForPortfolio(portfolioId)

    suspend fun getForPortfolio(portfolioId: Long): List<LiabilityEntity> =
        dao.getForPortfolio(portfolioId)

    fun observeById(id: Long): Flow<LiabilityEntity?> = dao.observeById(id)

    suspend fun getById(id: Long): LiabilityEntity? = dao.getById(id)

    suspend fun save(liability: LiabilityEntity): Long {
        val stamped = liability.copy(updatedAt = System.currentTimeMillis())
        return if (liability.id == 0L) dao.insert(stamped) else {
            dao.update(stamped)
            liability.id
        }
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
