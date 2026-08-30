package com.cairnlabworks.mywealth.data.repository

import com.cairnlabworks.mywealth.data.local.dao.PortfolioDao
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val dao: PortfolioDao) {

    fun observeAll(): Flow<List<PortfolioEntity>> = dao.observeAll()

    fun observeById(id: Long): Flow<PortfolioEntity?> = dao.observeById(id)

    suspend fun getById(id: Long): PortfolioEntity? = dao.getById(id)

    suspend fun getDefault(): PortfolioEntity? = dao.getDefault()

    suspend fun getAll(): List<PortfolioEntity> = dao.getAll()

    /** Creates a portfolio. The first portfolio ever created becomes the default. */
    suspend fun create(name: String, makeDefault: Boolean = false): Long {
        val isFirst = dao.count() == 0
        val now = System.currentTimeMillis()
        val id = dao.insert(
            PortfolioEntity(
                name = name.trim(),
                isDefault = false,
                createdAt = now,
                updatedAt = now,
            ),
        )
        if (isFirst || makeDefault) dao.setDefault(id)
        return id
    }

    suspend fun rename(id: Long, name: String) {
        val current = dao.getById(id) ?: return
        dao.update(current.copy(name = name.trim(), updatedAt = System.currentTimeMillis()))
    }

    suspend fun setDefault(id: Long) = dao.setDefault(id)

    /**
     * Deletes a portfolio (cascading its assets/liabilities). Refuses to remove
     * the final portfolio. If the default is removed, the next one is promoted.
     */
    suspend fun delete(id: Long): Boolean {
        if (dao.count() <= 1) return false
        val target = dao.getById(id) ?: return false
        dao.deleteById(id)
        if (target.isDefault) {
            dao.getAll().firstOrNull()?.let { dao.setDefault(it.id) }
        }
        return true
    }

    /** Ensures at least one (default) portfolio exists; returns its id. */
    suspend fun ensureDefaultPortfolio(defaultName: String = "My Portfolio"): Long {
        dao.getDefault()?.let { return it.id }
        dao.getAll().firstOrNull()?.let {
            dao.setDefault(it.id)
            return it.id
        }
        return create(defaultName, makeDefault = true)
    }
}
